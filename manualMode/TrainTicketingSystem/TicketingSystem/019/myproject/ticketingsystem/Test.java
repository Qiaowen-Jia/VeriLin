package ticketingsystem;

import java.util.*;
import java.io.*;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


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



//多线程性能测试

public class Test {
	final static int routenum = 5; // route is designed from 1 to 3
	final static int coachnum = 10; // coach is arranged from 1 to 5
	final static int seatnum = 100; // seat is allocated from 1 to 20
	final static int stationnum = 16; // station is designed from 1 to 5

	final static int testnum = 10000;//1000
	final static int retpc = 10; // return ticket operation is 10% percent
	final static int buypc = 40; // buy ticket operation is 30% percent
	final static int inqpc = 100; //inquiry ticket operation is 60% percent
	
	static String passengerName() {
		Random rand = new Random();
		long uid = rand.nextInt(testnum);
		return "passenger" + uid; 
	}
	static Thread []threads;

	public static void main(String[] args) throws InterruptedException {
        int threadnum = 4;
		final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
		final long startTime = System.nanoTime();

		System.out.println("Consumed time: Total time consumed for all operations.");
		System.out.println("Operation times: How many operations have been done.");
		System.out.println("ART: Average running time (nanoSecond) for every operation.");
		System.out.println("ops: Operations have been done per second.");

		for(int k=0;k<6;k++){
			threads = new Thread[threadnum];
			System.out.println("Test for "+threadnum+" Threads.");
			AtomicInteger buy_times = new AtomicInteger(0);
			AtomicInteger refund_times = new AtomicInteger(0);
			AtomicInteger inqury_times= new AtomicInteger(0);
			AtomicLong buy_time = new AtomicLong(0);
			AtomicLong refund_time = new AtomicLong(0);
			AtomicLong inqury_time = new AtomicLong(0);
			for (int t = 0; t< threadnum; t++){
				threads[t] = new Thread(new Runnable(){
					public void run() {
						Random rand = new Random();
						Ticket ticket = new Ticket();
						ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();
						for (int i = 0; i < testnum; i++) {
							int sel = rand.nextInt(inqpc);
							if (0 <= sel && sel < retpc && soldTicket.size() > 0) { // return ticket
								int select = rand.nextInt(soldTicket.size());
								long preTime = System.nanoTime() - startTime;
								if ((ticket = soldTicket.remove(select)) != null) {
									if (tds.refundTicket(ticket)){
										long postTime= System.nanoTime() - startTime;
										refund_times.getAndIncrement();
										refund_time.addAndGet(postTime-preTime);
									}
								}
							} else if (retpc <= sel && sel < buypc) { // buy ticket
								String passenger = passengerName();
								int route = rand.nextInt(routenum) + 1;
								int departure = rand.nextInt(stationnum - 1) + 1;
								int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
								long preTime = System.nanoTime() - startTime;

								if ((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null) {
									long postTime= System.nanoTime() - startTime;
									soldTicket.add(ticket);
									buy_times.getAndIncrement();
									buy_time.addAndGet(postTime-preTime);
								} 
							} else if (buypc <= sel && sel < inqpc) { // inquiry ticket
								int route = rand.nextInt(routenum) + 1;
								int departure = rand.nextInt(stationnum - 1) + 1;
								int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
								tds.inquiry(route, departure, arrival);
								long preTime = System.nanoTime() - startTime;
								inqury_times.getAndIncrement();   
								long postTime = System.nanoTime() - startTime;
								inqury_time.addAndGet(postTime-preTime);
							}
						}
					}
				});
				threads[t].start();
			}
			for (int i = 0; i< threadnum; i++) {
				threads[i].join();
			}
			double B = 1000000000;B = 10*B;
			long total_time = buy_time.get()+refund_time.get()+inqury_time.get();
			int total_times = buy_times.get()+refund_times.get()+inqury_times.get();
			System.out.println("       Consumed time:\t\tOperation times\t\tART(nanoSecond):\tOPS(Second):");
			System.out.format("Buy:   %10d\t\t%10d\t\t%10f\t\t%10f\n",buy_time.get(),buy_times.get(),(double)buy_time.get()/(buy_times.get()),B*buy_times.get()/(double)buy_time.get());
			System.out.format("Refund:%10d\t\t%10d\t\t%10f\t\t%10f\n",refund_time.get(),refund_times.get(),(double)refund_time.get()/(refund_times.get()),B*refund_times.get()/(double)refund_time.get());
			System.out.format("Inqury:%10d\t\t%10d\t\t%10f\t\t%10f\n",inqury_time.get(),inqury_times.get(),(double)inqury_time.get()/(inqury_times.get()),B*inqury_times.get()/(double)inqury_time.get());
			System.out.format("Total: %10d\t\t%10d\t\t%10f\t\t%10f\n",total_time,total_times,(double)total_time/(total_times),B*total_times/(double)total_time);
			threadnum*=2;
		}
		
		
	}
}
	




