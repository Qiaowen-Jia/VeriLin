package ticketingsystem;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Test {
	private final static int ROUTE_NUM = 5;
	private final static int COACH_NUM = 8;
	private final static int SEAT_NUM = 100;
	private final static int STATION_NUM = 10;

	private final static int testnum = 10000;
	private final static int retpc = 10;
	private final static int buypc = 40;
	private final static int inqpc = 100;
	private final static int thread = 128;
	private final static long[] buyTicketTime = new long[thread];
	private final static long[] retpcTime = new long[thread];
	private final static long[] inquiryTime = new long[thread];

	private final static long[] buyCount = new long[thread];
	private final static long[] retpcCount = new long[thread];
	private final static long[] inquiryCount = new long[thread];

	private final static AtomicInteger threadId = new AtomicInteger(0);

	static String passengerName() {
		Random rand = new Random();
		long uid = rand.nextInt(testnum);
		return "passenger" + uid;
	}

	public static void main(String[] args) throws InterruptedException {
		final int[] threadNums = { 4, 8, 16, 32, 64, 128 };
		int threadN;
		for (threadN = 0; threadN < threadNums.length; threadN++) {
			final TicketingDS tds = new TicketingDS(ROUTE_NUM, COACH_NUM, SEAT_NUM, STATION_NUM, threadNums[threadN]);
			Thread[] threads = new Thread[threadNums[threadN]];
			for (int i = 0; i < threadNums[threadN]; i++) {
				threads[i] = new Thread(new Runnable() {
					@Override
					public void run() {
						Random rand = new Random();
						Ticket ticket = new Ticket();
						int idOfThread = threadId.getAndIncrement();
						ArrayList<Ticket> soldTicket = new ArrayList<>();
						
						for (int i = 0; i < testnum; i++) {
							int sel = rand.nextInt(inqpc);
							if (0 <= sel && sel < retpc && soldTicket.size() > 0) { // retpc ticket 0-10
								int select = rand.nextInt(soldTicket.size());
								if ((ticket = soldTicket.remove(select)) != null) {
									long start = System.nanoTime();
									tds.refundTicket(ticket);
									long end = System.nanoTime();
									retpcTime[idOfThread] += end - start;
									retpcCount[idOfThread] += 1;
								} else {
									System.out.println("ErrOfRefund");
								}
							} else if (retpc <= sel && sel < buypc) { // buy ticket 10-40
								String passenger = passengerName();
								int route = rand.nextInt(ROUTE_NUM) + 1;
								int departure = rand.nextInt(STATION_NUM - 1) + 1;
								int arrival = departure + rand.nextInt(STATION_NUM - departure) + 1;
								long start = System.nanoTime();
								ticket = tds.buyTicket(passenger, route, departure, arrival);
								long end = System.nanoTime();
								buyTicketTime[idOfThread] += end - start;
								buyCount[idOfThread] += 1;
								if (ticket != null) {
									soldTicket.add(ticket);
								}
							} else if (buypc <= sel && sel < inqpc) { // inquiry ticket 40-100
								int route = rand.nextInt(ROUTE_NUM) + 1;
								int departure = rand.nextInt(STATION_NUM - 1) + 1;
								int arrival = departure + rand.nextInt(STATION_NUM - departure) + 1;
								long start = System.nanoTime();
								tds.inquiry(route, departure, arrival);
								long end = System.nanoTime();
								inquiryTime[idOfThread] += end - start;
								inquiryCount[idOfThread] += 1;
							}
						}
					}
				});
			}
			long startAll = System.currentTimeMillis();
			for (int i = 0; i < threadNums[threadN]; i++) {
				threads[i].start();
			}

			for (int i = 0; i < threadNums[threadN]; i++) {
				threads[i].join();
			}
			long endAll = System.currentTimeMillis();
			long buyCountTime = getSum(buyTicketTime, threadNums[threadN]);
			long retpcCountTime = getSum(retpcTime, threadNums[threadN]);
			long inquiryCountTime = getSum(inquiryTime, threadNums[threadN]);

			double bTotal = (double) getSum(buyCount, threadNums[threadN]);
			double rTotal = (double) getSum(retpcCount, threadNums[threadN]);
			double iTotal = (double) getSum(inquiryCount, threadNums[threadN]);

			long buyAvgTime = (long) (buyCountTime / bTotal);
			long retpcAvgTime = (long) (retpcCountTime / rTotal);
			long inquiryAvgTime = (long) (inquiryCountTime / iTotal);

			long time = endAll - startAll;

			long t = (long) (threadNums[threadN] * testnum / (double) time) * 1000;
			System.out.println(String.format(
					"ThreadNum: %d retpcAvgTime(ns): %d BuyAvgTime(ns): %d InquiryAvgTime(ns): %d ThroughOut(t/s): %d",
					threadNums[threadN],  retpcAvgTime, buyAvgTime, inquiryAvgTime, t));
			reset();
		}
	}

	private static long getSum(long[] array, int threadNums) {
		long res = 0;
		for (int i = 0; i < threadNums; ++i)
			res += array[i];
		return res;
	}

	private static void reset() {
		threadId.set(0);
		long[][] arrays = {buyTicketTime, retpcTime, inquiryTime, buyCount, retpcCount, inquiryCount};
		for (int i = 0; i < arrays.length; i++)
			for (int j = 0; j < arrays[i].length; j++)
				arrays[i][j] = 0;
	}

}
