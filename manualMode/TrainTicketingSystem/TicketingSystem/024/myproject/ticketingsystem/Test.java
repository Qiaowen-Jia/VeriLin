package ticketingsystem;

import java.util.*;

import java.util.concurrent.atomic.AtomicInteger;

public class Test {
	public static int routenum = 20;
	public static int coachnum = 10;
	public static int seatnum = 100;
	public static int stationnum = 16;
	public static int threadnum = 64;

	final static int testnum = 100000;
	final static int retpc = 10; // return ticket operation is 10% percent
	final static int buypc = 40; // buy ticket operation is 30% percent
	final static int inqpc = 100; //inquiry ticket operation is 60% percent

	private final static int thread = 128;

	private final static long[] buyTicketTime = new long[thread];
	private final static long[] refundTime = new long[thread];
	private final static long[] inquiryTime = new long[thread];

	private final static long[] buyTotal = new long[thread];
	private final static long[] refundTotal = new long[thread];
	private final static long[] inquiryTotal = new long[thread];

	private final static AtomicInteger threadId = new AtomicInteger(0);

	static String passengerName() {
		Random rand = new Random();
		long uid = rand.nextInt(testnum);
		return "passenger" + uid;
	}

	public static void main(String[] args) throws InterruptedException {
		final int[] threadNums = {4, 8, 16, 32, 64, 128};
		int test;
		for (test = 0; test < threadNums.length; ++test) {
			final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadNums[test]);
			Thread[] threads = new Thread[threadNums[test]];
			for (int i = 0; i < threadNums[test]; i++) {
				threads[i] = new Thread(new Runnable() {
					public void run() {
						Random rand = new Random();
						Ticket ticket = new Ticket();
						int id = threadId.getAndIncrement();
						ArrayList<Ticket> soldTicket = new ArrayList<>();
						for (int i = 0; i < testnum; i++) {
							int sel = rand.nextInt(inqpc);
							if (sel < retpc && soldTicket.size() > 0) { // refund ticket 0-10
								int select = rand.nextInt(soldTicket.size());
								if ((ticket = soldTicket.remove(select)) != null) {
									long s = System.nanoTime();
									tds.refundTicket(ticket);
									long e = System.nanoTime();
									refundTime[id] += e - s;
									refundTotal[id] += 1;
								} else {
									System.out.println("ErrOfRefund2");
								}
							} else if (retpc <= sel && sel < buypc) { // buy ticket 10-40
								String passenger = passengerName();
								int route = rand.nextInt(routenum) + 1;
								int departure = rand.nextInt(stationnum - 1) + 1;
								int arrival = departure + rand.nextInt(stationnum - departure) + 1;
								long s = System.nanoTime();
								ticket = tds.buyTicket(passenger, route, departure, arrival);
								long e = System.nanoTime();
								buyTicketTime[id] += e - s;
								buyTotal[id] += 1;
								if (ticket != null) {
									soldTicket.add(ticket);
								}
							} else if (buypc <= sel && sel < inqpc) { // inquiry ticket 40-100
								int route = rand.nextInt(routenum) + 1;
								int departure = rand.nextInt(stationnum - 1) + 1;
								int arrival = departure + rand.nextInt(stationnum - departure) + 1;
								long s = System.nanoTime();
								tds.inquiry(route, departure, arrival);
								long e = System.nanoTime();
								inquiryTime[id] += e - s;
								inquiryTotal[id] += 1;
							}
						}
					}
				});
			}

			long start = System.currentTimeMillis();
			for (int i = 0; i < threadNums[test]; ++i)
				threads[i].start();
			for (int i = 0; i < threadNums[test]; i++) {
				threads[i].join();
			}
			long end = System.currentTimeMillis();
			long buyTotalTime = calculateTotal(buyTicketTime, threadNums[test]);
			long refundTotalTime = calculateTotal(refundTime, threadNums[test]);
			long inquiryTotalTime = calculateTotal(inquiryTime, threadNums[test]);

			double bTotal = (double) calculateTotal(buyTotal, threadNums[test]);
			double rTotal = (double) calculateTotal(refundTotal, threadNums[test]);
			double iTotal = (double) calculateTotal(inquiryTotal, threadNums[test]);

			long buyAvgTime = (long) (buyTotalTime / bTotal);
			long refundAvgTime = (long) (refundTotalTime / rTotal);
			long inquiryAvgTime = (long) (inquiryTotalTime / iTotal);

			long time = end - start;

			long t = (long) (threadNums[test] * testnum / (double) time) * 1000; // 1000是从ms转换为s
			System.out.println(String.format(
					"ThreadNum: %d BuyAvgTime(ns): %d RefundAvgTime(ns): %d InquiryAvgTime(ns): %d ThroughOut(t/s): %d",
					threadNums[test], buyAvgTime, refundAvgTime, inquiryAvgTime, t));
			clear();
		}
	}

	private static long calculateTotal(long[] array, int threadNums) {
		long res = 0;
		for (int i = 0; i < threadNums; ++i)
			res += array[i];
		return res;
	}

	private static void clear() {
		threadId.set(0);
		long[][] arrays = { buyTicketTime, refundTime, inquiryTime, buyTotal, refundTotal, inquiryTotal };
		for (int i = 0; i < arrays.length; ++i)
			for (int j = 0; j < arrays[i].length; ++j)
				arrays[i][j] = 0;
	}
}
