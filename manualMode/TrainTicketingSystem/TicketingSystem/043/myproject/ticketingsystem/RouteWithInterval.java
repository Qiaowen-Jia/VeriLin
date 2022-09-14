package ticketingsystem;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicStampedReference;
import java.util.concurrent.locks.ReentrantLock;

public class RouteWithInterval {
  public int seatTotal;
  public int coachTotal;
  public int stationTotal;
  public int routeNo;

  private ReentrantLock lock;
  private int seatPerCoach;

  public int [] interval;
  public AtomicLong [] stat;
  private ReentrantLock [] [] itvLocks;
  private ReentrantLock [] seatLocks;
  public ConcurrentHashMap<Integer,Integer> [] avaSeat;
  private AtomicStampedReference<int []> curInterval;
  public AtomicBoolean [] inUse;

  public ConcurrentHashMap<Long, Ticket> tickets;

  public RouteWithInterval(int routeno, int seatnum, int coachnum, int stationnum) {
    seatTotal = seatnum * coachnum;
    coachTotal = coachnum;
    stationTotal = stationnum;
    routeNo = routeno;
    seatPerCoach = seatnum;
    
    
    interval = new int [stationTotal * stationTotal];
    for (int i = 0; i< stationTotal; i++){
      for (int j = i+1; j< stationTotal; j++){
        interval[i*stationTotal+j] = seatTotal;
      }
    }

    stat = new AtomicLong [seatTotal];
    for (int i = 0 ; i < stat.length; i++) {
      stat[i]  = new AtomicLong(0);
    }

    itvLocks = new ReentrantLock [stationTotal] [stationTotal];
    for (int i = 0; i < itvLocks.length; i++) {
      for (int j = i ; j < itvLocks[i].length; j++ ){
        itvLocks [i] [j ] = new ReentrantLock();
      }
    }

    seatLocks = new ReentrantLock [seatTotal];
    for (int i = 0; i< seatLocks.length; i++) {
      seatLocks[i] = new ReentrantLock();
    }

    avaSeat = new ConcurrentHashMap [stationTotal*stationTotal];
    for (int i = 0; i< stationTotal; i++) {
      for (int j = i + 1;j < stationTotal; j ++) {
        avaSeat[pairIdx(i, j)] = new ConcurrentHashMap<Integer,Integer>();
        for (int k = 0 ; k < seatTotal; k++){
          avaSeat[pairIdx(i, j)].put(k,k);
        }
      }
      
    }

    inUse = new AtomicBoolean [seatTotal];
    for (int i = 0; i < inUse.length; i++){
      inUse[i] = new AtomicBoolean(false);
    }

    curInterval = new AtomicStampedReference<int[]>(interval, 0);

    lock = new ReentrantLock();
    tickets = new ConcurrentHashMap<Long,Ticket>();

  }

  private Long intervalMask (int departure, int arrival) {
    return (1l << arrival) - (1l << departure);
  }

  private int pairIdx (int start, int end){
    return start * stationTotal + end;
  }


  private void printErrInterval (int [] interval,String script) {
    // System.out.println("## " + script);
    // System.out.print("## ");
    // for (int  row : interval){
    //     System.out.print(row + " ");
    // }
    // System.out.println();
  }

  private synchronized void  printErrLong (int l, String script) {
    // System.out.println("## " + script);
    // System.out.print("## " +l);
    // System.out.println();
  }

  public Ticket buyTicketInterval (String passenger, int departure, int arrival) {
    
    lock.lock();
    try {

    Long mask = (1l << arrival) - (1l << departure);
    int idleSeatIdx = -1;
    Long cur = 0l;

    for(Iterator<Map.Entry<Integer,Integer>> it = avaSeat[pairIdx(departure-1, arrival-1)].entrySet().iterator();it.hasNext();){
      Map.Entry<Integer,Integer> item = it.next();
      int idx = item.getValue();

      //check whether stat[idx] contains the avalible interval.
      cur = stat[idx].get();
        if ((cur & mask) == 0l){
          // if (inUse[idx].compareAndSet(false, true)) {
          //  seatLocks[idx].lock();
            {
              if (stat[idx].compareAndSet(cur, cur | mask)){
                idleSeatIdx = idx;
              break;
            }
            // seatLocks[idx].unlock();
          }
        }
    }

    if (idleSeatIdx == -1) return null;


    //update all the avaSeat intervals that have intersection with our interval.
    for (int i = 0; i < stationTotal; i++){
      for (int j = i+1 ;j < stationTotal;j ++){
        if (i < (arrival-1) && j > (departure-1)){
          if((cur & intervalMask(i+1, j+1)) == 0l){
            avaSeat[pairIdx(i, j)].remove(idleSeatIdx);
          }
        }
      }
    }

  
    //creat Ticket object .
    Ticket t = new Ticket();
    t.tid = TicketIdArranger.arrangeTid();
    t.passenger = passenger;
    t.route = routeNo;
    t.coach = idleSeatIdx / seatPerCoach + 1;
    t.seat = idleSeatIdx % seatPerCoach + 1;
    t.departure = departure;
    t.arrival = arrival;

    
  
    while (true) {
      
      //get Stamp;
      int [] myInterval = curInterval.getReference();
      int myStamp = curInterval.getStamp();
      
      int [] newInterval = new int [myInterval.length];
      newInterval = myInterval.clone();
    
      //modify newInterval.
      for (int i = 0; i < stationTotal; i++){
        for (int j = i+1; j < stationTotal; j ++){
          if (i < (arrival - 1) && j > (departure - 1) ){
            if((cur & intervalMask(i+1, j+1)) == 0l){
              newInterval[pairIdx(i, j)] --;
            }
          }
        }
      }
      
  
      //compare and set curInterval as newInterval. 
      // if failed, get curInterval again and reset the value.
      if (curInterval.compareAndSet(myInterval, newInterval, myStamp, myStamp+1)){
      // curInterval.set(newInterval, myStamp+1);{
        //Add ticket object into tickets.
        // seatLocks[idleSeatIdx].unlock();
        tickets.put(t.tid,t);
        return t;
      }

      // myInterval = curInterval.getReference();
      // myStamp = curInterval.getStamp();

    } 
  } finally {
    lock.unlock();
    // inUse[idleSeatIdx].set(false);
  }
  }

  private Boolean verifyTicket (Ticket t){
    if(tickets.containsKey(t.tid)){
      Ticket tLocal = tickets.get(t.tid);
      if(tLocal.departure == t.departure &&
        tLocal.arrival == t.arrival && 
        tLocal.passenger.equals(t.passenger) &&
        tLocal.coach == t.coach &&
        tLocal.seat == t.seat &&
        tLocal.route == t.route )
        return true;
    }
    return false;
  }

  public boolean refundTicketInterval (Ticket ticket) {

    lock.lock();
    try {

    if(!verifyTicket(ticket)) return false;
    int departure = ticket.departure;
    int arrival = ticket.arrival;
    Long mask = (1l << arrival) - (1l << departure);
    int idx = (ticket.coach - 1) * seatPerCoach + (ticket.seat - 1);

    Long cur = stat[idx].get();
    Long alt = cur & (~mask);
    if((cur & mask) == mask) {
      // while (inUse[idx].compareAndSet(false, true) == false);
      // seatLocks[idx].lock();
      {
        if (!stat[idx].compareAndSet(cur, alt)){
          return false;
        }
      }
    }


    //update all the avaSeat
    for (int i = 0; i< stationTotal; i++){
      for (int j = i+1; j <stationTotal; j ++){
        if (i < (arrival -1) && j > (departure - 1)){
          if ((alt & intervalMask(i+1, j+1)) == 0l){
            avaSeat[pairIdx(i, j)].putIfAbsent(idx, idx);
          }
        }
      }
    }
   
      while (true) {
        
      int [] myInterval = curInterval.getReference();
      int myStamp = curInterval.getStamp();
        
      int[] newInterval =  new int [myInterval.length];
      
      newInterval = myInterval.clone();

      //modify newInterval
      for (int i =0;i < stationTotal; i++){
        for (int j = i+1 ; j<stationTotal;j++){
          if(i < (arrival -1) && j > (departure - 1)){
            if ((alt & intervalMask(i+1, j+1)) == 0l){
              newInterval[pairIdx(i, j)] ++;
            }
          }
        }
      }

      if (curInterval.compareAndSet(myInterval, newInterval, myStamp, myStamp+1)){
        // curInterval.set(newInterval,myStamp+1);{
        // seatLocks[idx].unlock();
        tickets.remove(ticket.tid);
        return true;
      }

      // myInterval = curInterval.getReference();
      // myStamp = curInterval.getStamp();

    }
  }finally {
    lock.unlock();
    // inUse[idx].set(false);
  }
    

  }

  public int inquiryTicketInterval (int departure, int arrival){
    int [] myInterval;
    int myStamp;
    int res;
    do {
      myInterval = curInterval.getReference();
      myStamp = curInterval.getStamp();
      res =  myInterval[pairIdx(departure-1, arrival-1)];
    } while (myStamp != curInterval.getStamp());

    return res;
  }

}
