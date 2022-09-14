package ticketingsystem;

import java.util.*;

import java.util.concurrent.atomic.AtomicInteger;


class ThreadId {
    // Atomic integer containing the next thread ID to be assigned
    private static final AtomicInteger nextId = new AtomicInteger(0);

    // Thread local variable containing each thread's ID
    private static final ThreadLocal<Integer> threadId =
        new ThreadLocal<Integer>() {
            @Override protected Integer initialValue() {
                return nextId.getAndIncrement();
        }
    };

    // Returns the current thread's unique ID, assigning it if necessary
    public static int get() {
        return threadId.get();
    }
}
class myInt {
    volatile int value;
}

public class GenerateHistory {
	static int threadnum;//input
	static int testnum;//input
	static boolean isSequential;//input
	static int msec = 0;
	static int nsec = 0;
    
	static  AtomicInteger rLock = new AtomicInteger(0); //Resource Lock
	static boolean[] fin;

	protected static boolean exOthNotFin(int tNum, int tid) {
		boolean flag = false;
		for (int k = 0; k < tNum; k++) {
			if (k == tid) continue;
			flag = (flag || !(fin[k])); 
		}
		return flag;
	}

    static void RLOCK_TAKE() {
    	while (rLock.compareAndSet(0, 1) == false) {}
    }

    static void RLOCK_GIVE() {
        rLock.set(0);
    }

    static boolean RLOCK_TRY() {
        return (rLock.get() == 0);
    }

/****************Manually Set Testing Information **************/

	final static int routenum = 3; // route is designed from 1 to 3
	final static int coachnum = 3; // coach is arranged from 1 to 5
	final static int seatnum = 3; // seat is allocated from 1 to 20
	final static int stationnum = 3; // station is designed from 1 to 5


	final static List<String> methodList = new ArrayList<String>();
	final static List<Integer> freqList = new ArrayList<Integer>();
	static TicketingDS tds;
	final static List<Ticket> currentTicket = new ArrayList<Ticket>();
    final static ArrayList<List<Ticket>> soldTicket = new ArrayList<List<Ticket>>();
	static int totalPc;
	static boolean initLock = false;
	final static Random rand = new Random();
	public static void initialization(){
	  for(int i = 0; i < threadnum; i++){
		List<Ticket> threadTickets = new ArrayList<Ticket>();
		soldTicket.add(threadTickets);
		currentTicket.add(null);
	  }
	  methodList.add("refundTicket");
	  freqList.add(10);
	  methodList.add("buyTicket");
	  freqList.add(30);
	  methodList.add("inquiry");
	  freqList.add(60);
	  totalPc = 100;
	}
	public static String getPassengerName() {
		long uid = rand.nextInt(testnum);
		return "passenger" + uid; 
	}

	public static void print(Ticket ticket, long preTime, long postTime, String actionName){
	  System.out.println(preTime + " " + postTime + " " +  ThreadId.get() + " " + actionName + " " + ticket.tid + " " + ticket.passenger + " " + ticket.route + " " + ticket.coach + " " + ticket.departure + " " + ticket.arrival + " " + ticket.seat);
	}

	public static boolean execute(int num, Ticket ticket){
	  int route, departure, arrival;
	  switch(num){
		case 0:
		  if(soldTicket.get(ThreadId.get()).size() == 0)
			return false;
		  int n = rand.nextInt(soldTicket.get(ThreadId.get()).size());
		  ticket = soldTicket.get(ThreadId.get()).remove(n);
		  currentTicket.set(ThreadId.get(), ticket);
		  if(ticket == null)
			return false;
		  boolean flag = tds.refundTicket(ticket);
		  return flag;
		case 1:
          String passenger = getPassengerName();
          route = rand.nextInt(routenum) + 1;
          departure = rand.nextInt(stationnum - 1) + 1;
          arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
		  ticket = tds.buyTicket(passenger, route, departure, arrival);
		  currentTicket.set(ThreadId.get(), ticket);
		  if(ticket == null)
			return false;
		  //TODO: do not print soldout
		  soldTicket.get(ThreadId.get()).add(ticket);
		  return true;
		case 2:
          ticket.route = rand.nextInt(routenum) + 1;
          ticket.departure = rand.nextInt(stationnum - 1) + 1;
          ticket.arrival = ticket.departure + rand.nextInt(stationnum - ticket.departure) + 1; // arrival is always greater than departure
		  ticket.seat = tds.inquiry(ticket.route, ticket.departure, ticket.arrival);
		  currentTicket.set(ThreadId.get(), ticket);
		  return true;
		default:
		  System.out.println("Error in execution.");
		  return false;
	  }
	}
// The standard output should be:
// preTime, postTime, threadId, operationName, tid, passenger, route, coach, departure, arrival, seat, num
/***********VeriLin***********/
  public static void main(String[] args) throws InterruptedException {
    if(args.length != 5){
	  System.out.println("The arguments of GenerateHistory is threadNum,  testNum, isSequential(0/1), delay(millionsec), delay(nanosec)");
	  return;
	}
	threadnum = Integer.parseInt(args[0]);
	testnum = Integer.parseInt(args[1]);
	if(args[2].equals("0")){
	  isSequential = false;
	}
	else if(args[2].equals("1")){
	  isSequential = true;
	}
	else{
	  System.out.println("The arguments of GenerateHistory is threadNum,  testNum, isSequential(0/1)");
	  return;
	}
	msec = Integer.parseInt(args[3]);
	nsec = Integer.parseInt(args[4]);
	Thread[] threads = new Thread[threadnum];
	myInt barrier = new myInt();
	fin = new boolean[threadnum];
	final long startTime = System.nanoTime();
	    
	for (int i = 0; i < threadnum; i++) {
	    	threads[i] = new Thread(new Runnable() {
                public void run() {
                	Ticket ticket = new Ticket();
					if(ThreadId.get() == 0){
					  initialization();
					  tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
					  initLock = true;
					}
					else{
					  while(!initLock);
					}
					for(int k = 0; k < testnum; k++){
					  int sel = rand.nextInt(totalPc);
					  int cnt = 0;
					  if(isSequential){
						while (ThreadId.get() != barrier.value && exOthNotFin(threadnum, ThreadId.get()) == true);
	                    RLOCK_TAKE();
						if(exOthNotFin(threadnum, ThreadId.get()) == true){
							barrier.value = rand.nextInt(threadnum);
							while (fin[barrier.value] == true) {
							  barrier.value = rand.nextInt(threadnum);
							}
						}
					  }

					  for(int j = 0; j < methodList.size(); j++){
						if(sel >= cnt && sel < cnt + freqList.get(j)){
						  if(msec != 0 || nsec != 0){
							try{
							  Thread.sleep(msec, nsec);
							}catch(InterruptedException e){
							  return;
							}
						  }
						  long preTime = System.nanoTime() - startTime;
						  boolean flag = execute(j, ticket);
						  long postTime = System.nanoTime() - startTime;
						  if(flag){
							print(currentTicket.get(ThreadId.get()), preTime, postTime, methodList.get(j));
						  }
						  cnt += freqList.get(j);
						}
					  }

					  if(isSequential)
						RLOCK_GIVE();
					}
					if (isSequential) {
						fin[ThreadId.get()] = true;
						if (exOthNotFin(threadnum, ThreadId.get()) == true) {
						  barrier.value = rand.nextInt(threadnum);
						  while (fin[barrier.value] == true) {
							  barrier.value = rand.nextInt(threadnum);
						  }
						}
					}
				}
            });
			threads[i].start();
	  }
	
	  for (int i = 0; i< threadnum; i++) {
		threads[i].join();
	  }	
	}
}
