package ticketingsystem;

import java.util.*;

public class Test {
	private final static int ROUTE_NUM = 5;
	private final static int COACH_NUM = 8;
	private final static int SEAT_NUM = 100;
	private final static int STATION_NUM = 10;

	private final static int TEST_NUM = 10000;
	private final static int refund = 10;
	private final static int buy = 40;
	private final static int query = 100;

	private static long buyTime=0;//it was used to test the average time for every method call
	private static long refundTime=0;//it was used to test the average time for every method call
	private static long queryTime=0;//it was used to test the average time for every method call
	private static int buyNumber=0;//it was used to test the average time for every method call
	private static int refundNumber=0;//it was used to test the average time for every method call
	private static int queryNumber=0;//it was used to test the average time for every method call

	static String passengerName() {
		Random rand = new Random();
		long uid = rand.nextInt(TEST_NUM);
		return "passenger" + uid;
	}

	public static void main(String[] args) throws InterruptedException {
		//ToDo
		final int[] threadNums = {4, 8, 16, 32, 64};
		for (int n = 0; n < 5; n++) {
			final TicketingDS tds = new TicketingDS(ROUTE_NUM, COACH_NUM, SEAT_NUM, STATION_NUM, threadNums[n]);
			Thread[] threads = new Thread[threadNums[n]];
			long start = System.currentTimeMillis();
			for (int i = 0; i < threadNums[n]; i++) {
				threads[i] = new Thread(new Runnable() {
					public void run() {
						Random rand = new Random();
						Ticket ticket = new Ticket();
						ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();
						for (int i = 0; i < TEST_NUM; i++) {
							int sel = rand.nextInt(query);
							if (0 <= sel && sel < refund && soldTicket.size() > 0) {
								int select = rand.nextInt(soldTicket.size());
								long startrefundTime = System.nanoTime();//it was used to test the average time for every method call
								if ((ticket = soldTicket.remove(select)) != null) {
									if (tds.refundTicket(ticket)) {
										long endrefundTime = System.nanoTime();//it was used to test the average time for every method call
										refundTime=refundTime+endrefundTime-startrefundTime;//it was used to test the average time for every method call
										refundNumber=refundNumber+1;//it was used to test the average time for every method call
									} else {
										System.out.println("ErrOfRefund1");
									}
								} else {
									System.out.println("ErrOfRefund2");
								}
							} else if (refund <= sel && sel < buy) {
								long startbuyTime = System.nanoTime();//it was used to test the average time for every method call
								String passenger = passengerName();
								int route = rand.nextInt(ROUTE_NUM) + 1;
								int departure = rand.nextInt(STATION_NUM - 1) + 1;
								int arrival = departure + rand.nextInt(STATION_NUM - departure) + 1;
								if ((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null) {
									soldTicket.add(ticket);
									long endbuyTime = System.nanoTime();//it was used to test the average time for every method call
									buyTime=buyTime+endbuyTime-startbuyTime;//it was used to test the average time for every method call
									buyNumber=buyNumber+1;//it was used to test the average time for every method call
								} else {}
							} else if (buy <= sel && sel < query) {
								int route = rand.nextInt(ROUTE_NUM) + 1;
								int departure = rand.nextInt(STATION_NUM - 1) + 1;
								int arrival = departure + rand.nextInt(STATION_NUM - departure) + 1;
								long startqueryTime = System.nanoTime();//it was used to test the average time for every method call
								int leftTicket = tds.inquiry(route, departure, arrival);
								long endqueryTime = System.nanoTime();//it was used to test the average time for every method call
								queryTime=queryTime+endqueryTime-startqueryTime;//it was used to test the average time for every method call
								queryNumber=queryNumber+1;//it was used to test the average time for every method call
							}
						}
					}
				});
				threads[i].start();
			}

			for (int i = 0; i < threadNums[n]; i++) {
				threads[i].join();
			}

			long time = System.currentTimeMillis() - start;
			long throughput = (long) (threadNums[n] * TEST_NUM / (double) (time)) * 1000;
			System.out.println(String.format("ThreadsNumber:%d TotalTime(ms):%d Throughput Rate(ops/s):%d queryAvgTime(ns):%d buyTicketAvgTime(ns):%d refundAvgTime(ns):%d", threadNums[n], time, throughput, queryTime / queryNumber, buyTime/buyNumber, refundTime/refundNumber));
			buyTime=0;
			refundTime=0;
			queryTime=0;
			buyNumber=0;
			refundNumber=0;
			queryNumber=0;
		}
	}
}