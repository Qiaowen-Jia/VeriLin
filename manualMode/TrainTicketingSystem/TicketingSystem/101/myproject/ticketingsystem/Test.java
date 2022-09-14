package ticketingsystem;



import java.util.*;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class Test {
	static AtomicReference<Double> buyTime = new AtomicReference<Double>();
	static AtomicReference<Double> inquiryTime = new AtomicReference<Double>();
	static AtomicReference<Double> refundTime = new AtomicReference<Double>();
	static AtomicLong buyCount = new AtomicLong();
	static AtomicLong inquiryCount = new AtomicLong();
	static AtomicLong refundCount = new AtomicLong();

	static int testnum = 10000;

	static String passengerName() {
		Random rand = new Random();
		long uid = rand.nextInt(testnum);
		return "passenger" + uid;
	}

	public static void testTicketSystemWithThreadNum(int threadnum) throws InterruptedException {

		int routenum = 5; // route is designed from 1 to 3
		int coachnum = 8; // coach is arranged from 1 to 5
		int seatnum = 100; // seat is allocated from 1 to 20
		int stationnum = 10; // station is designed from 1 to 5

		int testnum = 10000;
		int retpc = 30; // return ticket operation is 10% percent
		int buypc = 60; // buy ticket operation is 30% percent
		int inqpc = 100; // inquiry ticket operation is 60% percent
		Thread[] threads = new Thread[threadnum];

		final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);

		final long startTime = System.nanoTime();
		// long preTime = startTime;
		buyTime.set(0.0);
		inquiryTime.set(0.0);
		refundTime.set(0.0);
		buyCount.set(0);
		inquiryCount.set(0);
		refundCount.set(0);

		for (int i = 0; i < threadnum; i++) {
			threads[i] = new Thread(new Runnable() {
				public void run() {
					Random rand = new Random();
					Ticket ticket = new Ticket();
					ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();

					// System.out.println(ThreadId.get());
					for (int i = 0; i < testnum; i++) {
						int sel = rand.nextInt(inqpc);
						if (0 <= sel && sel < retpc && soldTicket.size() > 0) { // return ticket
							int select = rand.nextInt(soldTicket.size());
							if ((ticket = soldTicket.remove(select)) != null) {
								refundCount.getAndIncrement();
								long preTime = System.nanoTime();
								tds.refundTicket(ticket);
								long postTime = System.nanoTime();
								refundTime.set(refundTime.get() + (double)(postTime - preTime));
							} else {
								long postTime = System.nanoTime();
								System.out
										.println(ThreadId.get() + " " + "ErrOfRefund");
								// System.out.flush();
							}
						} else if (retpc <= sel && sel < buypc) { // buy ticket
							String passenger = passengerName();
							int route = rand.nextInt(routenum) + 1;
							int departure = rand.nextInt(stationnum - 1) + 1;
							int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always
																								// greater than
																								// departure
							buyCount.getAndIncrement();
							long preTime = System.nanoTime(), postTime;
							if ((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null) {
								postTime = System.nanoTime();
								soldTicket.add(ticket);
							} else {
								postTime = System.nanoTime();
							}
							buyTime.set(buyTime.get() + (double)(postTime - preTime));
						} else if (buypc <= sel && sel < inqpc) { // inquiry ticket

							int route = rand.nextInt(routenum) + 1;
							int departure = rand.nextInt(stationnum - 1) + 1;
							int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always
																								// greater than
																								// departure
							inquiryCount.getAndIncrement();
							long preTime = System.nanoTime();
							int leftTicket = tds.inquiry(route, departure, arrival);
							long postTime = System.nanoTime();
							inquiryTime.set(inquiryTime.get() + (double)(postTime - preTime));
							// System.out.println(preTime + " " + ThreadId.get() + " " + "RemainTicket" + "
							// " + leftTicket + " " + route+ " " + departure+ " " + arrival);
							// System.out.flush();

						}
					}

				}
			});
			threads[i].start();
		}

		for (int i = 0; i < threadnum; i++) {
			threads[i].join();
		}

		long count = inquiryCount.get() + buyCount.get() + refundCount.get();
		double time = inquiryTime.get() + buyTime.get() + refundTime.get();
		System.out.println(threadnum + "\t" + 
			inquiryCount + "\t" + inquiryTime + "\t" + (inquiryTime.get() / (double)inquiryCount.get()) + "\t" +
			buyCount + "\t"     + buyTime     + "\t" + (buyTime.get() / (double)buyCount.get()) + "\t" + 
			refundCount + "\t"  + refundTime  + "\t" + (refundTime.get() / (double)refundCount.get()) + "\n");
	}
	public static void main(String[] args) throws InterruptedException {
		System.out.println("thread num\t" + 
			"inquiry count\tinquiry time\tinquiry rate\t" +
			"buy count\tbuy time\tbuy rate" +
			"refund count\trefund time\trefund rate");
		testTicketSystemWithThreadNum(4);
		testTicketSystemWithThreadNum(8);
		testTicketSystemWithThreadNum(16);
		testTicketSystemWithThreadNum(32);
		testTicketSystemWithThreadNum(64);
	}
}
