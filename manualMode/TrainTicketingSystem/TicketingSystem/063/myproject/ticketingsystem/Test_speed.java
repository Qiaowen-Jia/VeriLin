package ticketingsystem;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

// class ThreadId {
// 	// Atomic integer containing the next thread ID to be assigned
// 	private static final AtomicInteger nextId = new AtomicInteger(0);

// 	// Thread local variable containing each thread's ID
// 	private static final ThreadLocal<Integer> threadId = new ThreadLocal<Integer>() {
// 		@Override
// 		protected Integer initialValue() {
// 			return nextId.getAndIncrement();
// 		}
// 	};

// 	// Returns the current thread's unique ID, assigning it if necessary
// 	public static int get() {
// 		return threadId.get();
// 	}
// }

public class Test_speed {
	static int threadnum = 3;
	final static int routenum = 10; // route is designed from 1 to 3
	final static int coachnum = 10; // coach is arranged from 1 to 5
	final static int seatnum = 10; // seat is allocated from 1 to 20
	final static int stationnum = 16; // station is designed from 1 to 5

	final static int testnum = 10000;

	final static int retpc = 30; // return ticket operation is 10% percent
	final static int buypc = 60; // buy ticket operation is 30% percent
	final static int inqpc = 100; // inquiry ticket operation is 60% percent

	static String passengerName() {
		Random rand = new Random(1);
		long uid = rand.nextInt(testnum);
		return "passenger" + uid;
	}

	public static void main(String[] args) throws InterruptedException {

		// ------check--------
		int[] test_thread_num = { 1, 2, 4, 8, 16, 32, 64, 128 };
		for (int y = 0; y < 8; y++) {

			threadnum = test_thread_num[y];

			// ------check--------

			Thread[] threads = new Thread[threadnum];

			final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);

			final long startTime = System.nanoTime();

			for (int i = 0; i < threadnum; i++) {
				threads[i] = new Thread(new Runnable() {
					public void run() {
						Random rand = new Random(1);
						Ticket ticket = new Ticket();
						ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();

						for (int i = 0; i < testnum; i++) {
							int sel = rand.nextInt(inqpc);
							if (0 <= sel && sel < retpc && soldTicket.size() > 0) { // return ticket
								int select = rand.nextInt(soldTicket.size());
								if ((ticket = soldTicket.remove(select)) != null) {
									long preTime = System.nanoTime() - startTime;
									if (tds.refundTicket(ticket)) {
										long postTime = System.nanoTime() - startTime;
										// System.out.println(preTime + " " + postTime + " " + ThreadId.get() + " "
										// + "TicketRefund" + " " + ticket.tid + " " + ticket.passenger + " "
										// + ticket.route + " " + ticket.coach + " " + ticket.departure + " "
										// + ticket.arrival + " " + ticket.seat);
										// System.out.flush();
									} else {
										// System.out.println(preTime + " " + String.valueOf(System.nanoTime() -
										// startTime)
										// + " " + ThreadId.get() + " " + "ErrOfRefund");
										// System.out.flush();
									}
								} else {
									long preTime = System.nanoTime() - startTime;
									// System.out.println(preTime + " " + String.valueOf(System.nanoTime() -
									// startTime)
									// + " " + ThreadId.get() + " " + "ErrOfRefund");
									// System.out.flush();
								}
							} else if (retpc <= sel && sel < buypc) { // buy ticket
								String passenger = passengerName();
								int route = rand.nextInt(routenum) + 1;
								int departure = rand.nextInt(stationnum - 1) + 1;
								int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always
																									// greater than
																									// departure
								long preTime = System.nanoTime() - startTime;
								if ((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null) {
									long postTime = System.nanoTime() - startTime;
									// System.out.println(preTime + " " + postTime + " " + ThreadId.get() + " "
									// + "TicketBought" + " " + ticket.tid + " " + ticket.passenger + " "
									// + ticket.route + " " + ticket.coach + " " + ticket.departure + " "
									// + ticket.arrival + " " + ticket.seat);
									soldTicket.add(ticket);
									// System.out.flush();
								} else {
									// System.out.println(preTime + " " + String.valueOf(System.nanoTime() -
									// startTime)
									// + " " + ThreadId.get() + " " + "TicketSoldOut" + " " + route + " "
									// + departure + " " + arrival);
									// System.out.flush();
								}
							} else if (buypc <= sel && sel < inqpc) { // inquiry ticket

								int route = rand.nextInt(routenum) + 1;
								int departure = rand.nextInt(stationnum - 1) + 1;
								int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always
																									// greater than
																									// departure
								long preTime = System.nanoTime() - startTime;
								int leftTicket = tds.inquiry(route, departure, arrival);
								long postTime = System.nanoTime() - startTime;
								// System.out
								// .println(preTime + " " + postTime + " " + ThreadId.get() + " " +
								// "RemainTicket"
								// + " " + leftTicket + " " + route + " " + departure + " " + arrival);
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

			final long endTime = System.nanoTime();
			long time_consume = endTime - startTime;
			long op_num = testnum * 1000000000L * threadnum / time_consume;
			System.out.println(
					"thread num: " + threadnum + " time consume: " + time_consume + " op num : " + op_num + "\n");
		}

	}

}