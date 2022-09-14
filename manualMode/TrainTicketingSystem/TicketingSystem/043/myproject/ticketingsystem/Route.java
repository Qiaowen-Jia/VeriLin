package ticketingsystem;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Route {
  public boolean [] [] seat; // seat[i][j] == true means the ticket of seat i and interval [j,j+1) is sold.
  public int seatTotal;
  public int coachTotal;
  public int stationTotal;
  public int routeNo;
  private ReentrantLock lock;
  private int seatNum;

  
  public ConcurrentHashMap<Long,Ticket> tickets ;
  

  public Route (int routeno, int seatnum, int coachnum, int stationnum) {
    seatTotal = seatnum * coachnum;
    coachTotal = coachnum;
    stationTotal = stationnum;
    routeNo = routeno;
    seatNum = seatnum;
    
    seat = new boolean [seatTotal][stationTotal];
    for (int i = 0 ; i < seat.length; i++){
      for (int j = 0; j < seat[i].length; j++) {
        seat[i] [j]= false;
      }
    }

    lock = new ReentrantLock();

    tickets = new ConcurrentHashMap<Long,Ticket>();

  }

  private boolean verify (int seatno, int departure, int arrival, boolean soldState) {
    for (int i = departure; i < arrival; i++) {
      if (seat[seatno][i] == !soldState) return false;
    }
    return true;
  }


  public Ticket buyTicketRoute (String passenger,  int departure, int arrival) {
    lock.lock();
    try {
      // tranverse seat to find an interval contains [departure, arrival)
      while (true) {
        
        int idx;
        boolean ava;
        for (idx = 0; idx < seat.length; idx++) {
          ava = true;
          for (int i = departure; i < arrival ; i ++) {
            if(seat[idx][i]) ava = false;
          }
          if(ava) break;
        }
        if (idx >= seat.length) break;

        if (verify(idx,departure,arrival,false)){
          // the seat idx is avalible for interval [departure, arrival)
          // arrange a ticket number to it and creat an object.
          
          Ticket t = new Ticket();
          t.tid = TicketIdArranger.arrangeTid();
          t.passenger = passenger;
          t.route = routeNo;
          t.coach = idx / seatNum + 1;
          t.seat = idx % seatNum + 1;
          t.departure =departure;
          t.arrival = arrival;

          // set seat idx w/ interval [departure, arrival) as sold and insert t into ConcurHashMap.
          if (!tickets.containsKey(t.tid)){
            for (int i = departure; i<arrival; i++) {
              seat[idx][i] = true;
            }
            tickets.put(t.tid, t);
            return t;
          }
        }

      }
      return null;

    } finally {
      lock.unlock();
    }
  }

  public boolean refundTicketRoute(Ticket ticket) {
    lock.lock();
    try {
      if (tickets.containsKey(ticket.tid)){
        Ticket tLocal = tickets.get(ticket.tid);
        if(tLocal.equals(ticket)){ // Is it nessecery to do this check?
          int idx = (ticket.coach - 1)* seatNum + (ticket.seat -1 ) ;
          
          if (verify( idx, ticket.departure, ticket.arrival, true)){

            // seat idx w/ interval [ticket.departure, ticket.arrival) can be sold again.
            for (int i = ticket.departure; i< ticket.arrival ; i++) {
              seat[idx][i] = false;
            }

            tickets.remove(ticket.tid);

            return true;
          }

        }
      } 

      
      return false;
    } finally {
      lock.unlock();
    }
  }

  public int inquiryTicketRoute (int departure, int arrival) {
    lock.lock();
    try {
      //tranverse seat array.
      int cnt = 0;
      boolean ava;
      for (int i = 0; i < seat.length ; i++) {
        ava = true;
        for (int j = departure; j < arrival ; j++) {
          if (seat[i][j]) ava = false;
        }
        if (ava) cnt++;
      }
      return cnt;
    }finally {
      lock.unlock();
    }
  }

}
