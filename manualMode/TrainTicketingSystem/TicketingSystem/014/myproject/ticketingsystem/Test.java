package ticketingsystem;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Random;

class TestResult {
	public double avgSingleBuyTicketTime;
	public double avgSingleRefundTime;
	public double avgSingleInquiryTime;
	public double throughput;
	public double successThroughPut;
	public double failedThroughPut;

	public TestResult() {
	}

	public TestResult(double avgSingleBuyTicketTime,
					  double avgRefundTime,
					  double avgInquiryTime,
					  double throughput,
					  double successThroughPut,
					  double failedThroughPut) {
		this.avgSingleBuyTicketTime = avgSingleBuyTicketTime;//测试中每个线程执行购票操作耗时
		this.avgSingleRefundTime = avgRefundTime;//测试中每个线程执行退票操作耗时
		this.avgSingleInquiryTime = avgInquiryTime;//测试中每个线程执行查询操作耗时
		this.throughput = throughput;//系统总吞吐率
		this.successThroughPut = successThroughPut;
		this.failedThroughPut = failedThroughPut;

	}

	@Override
	public String toString() {
//		BigDecimal b1 = new BigDecimal(avgSingleBuyTicketTime);
//		avgSingleBuyTicketTime = b1.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
//		BigDecimal b2 = new BigDecimal(avgSingleInquiryTime);
//		avgSingleInquiryTime = b2.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
//		BigDecimal b3 = new BigDecimal(avgSingleRefundTime);
//		avgSingleRefundTime = b3.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();

		return "TestResult{" +
				"avgSingleBuyTicketTime=" + avgSingleBuyTicketTime + "ms" +
				", avgSingleRefundTime=" + avgSingleRefundTime + "ms" +
				", avgSingleInquiryTime=" + avgSingleInquiryTime + "ms" +
				", throughput=" + String.format("%.2f", throughput) + "times/s" +
				", successThroughput=" + String.format("%.2f", successThroughPut) + "times/s" +
				", failedThroughput=" + String.format("%.2f", failedThroughPut) + "times/s" +
				'}';
	}
}

public class Test {
	private final static int[] thread_nums = {4,8, 16, 32, 64}; // concurrent thread number
	private final static int routenum = 20; // route is designed from 1 to 3
	private final static int coachnum = 15; // coach is arranged from 1 to 5
	private final static int seatnum = 100; // seat is allocated from 1 to 20
	private final static int stationnum = 10; // station is designed from 1 to 5

	private final static int testnum = 10000;
	private final static int inqpc = 100;
	private final static int retpc = 10;
	private final static int buypc = 40;

	private final static int BUY = 1;
	private final static int REFUND = 0;
	private final static int INQUERY = 2;


	private String passengerName(int testnum) {
		Random rand = new Random();
		long uid = rand.nextInt(testnum);
		return "passenger" + uid;
	}

	private TestResult test(final int threadnum, final int testnum, final int routenum, final int coachnum, final int seatnum, final int stationnum) throws Exception {
		Thread[] threads = new Thread[threadnum];

		// 用于计算单线程方法平均耗时
		final long[] functionStartTime = new long[threadnum];
		final long[][] functionCostTimeSum = new long[threadnum][3];
		final long[][] sucessFunctionCostTimeSum = new long[threadnum][3];
		final long[][] failedFunctionCostTimeSum = new long[threadnum][3];

		final int[][] executeCount = new int[threadnum][3];
		final int[][] goodCount = new int[threadnum][3];
		final int[][] badCount = new int[threadnum][3];


		long startTime = System.currentTimeMillis();
		final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);

		for (int i = 0; i < threadnum; i++) {
			final int finalI = i;
			threads[i] = new Thread(new Runnable() {
				public void run() {
					Random rand = new Random();
					Ticket ticket;
					ArrayList<Ticket> soldTicket = new ArrayList<>();

					for (int j = 0; j < testnum; j++) {
						int sel = rand.nextInt(inqpc);
						if (0 <= sel && sel < retpc && soldTicket.size() > 0) { //  refund ticket
							int select = rand.nextInt(soldTicket.size());
							if ((ticket = soldTicket.remove(select)) != null) {
								functionStartTime[finalI] = System.currentTimeMillis();
								tds.refundTicket(ticket);
								executeCount[finalI][REFUND]++;
								functionCostTimeSum[finalI][REFUND] += System.currentTimeMillis() - functionStartTime[finalI];
							}
						} else if (retpc <= sel && sel < buypc) { // buy ticket
							String passenger = passengerName(testnum);
							int route = rand.nextInt(routenum) + 1;
							int departure = rand.nextInt(stationnum - 1) + 1;
							int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
							functionStartTime[finalI] = System.currentTimeMillis();
							if ((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null) {
								soldTicket.add(ticket);
								executeCount[finalI][BUY]++;
								goodCount[finalI][BUY]++;
								double dur = System.currentTimeMillis() - functionStartTime[finalI];
								functionCostTimeSum[finalI][BUY] += dur;// though the action fails, the time counts
								sucessFunctionCostTimeSum[finalI][BUY] += dur;
							}
							else
							{
								executeCount[finalI][BUY]++;
								badCount[finalI][BUY]++;
								double dur = System.currentTimeMillis() - functionStartTime[finalI];
								functionCostTimeSum[finalI][BUY] += dur ;// though the action fails, the time counts
								failedFunctionCostTimeSum[finalI][BUY] += dur;
							}
						} else if (buypc <= sel && sel < inqpc) { // inquiry ticket
							int route = rand.nextInt(routenum) + 1;
							int departure = rand.nextInt(stationnum - 1) + 1;
							int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
							functionStartTime[finalI] = System.currentTimeMillis();
							int leftTicket = tds.inquiry(route, departure, arrival);
							executeCount[finalI][INQUERY]++;
							double dur = System.currentTimeMillis() - functionStartTime[finalI];
							functionCostTimeSum[finalI][INQUERY] += dur;
							sucessFunctionCostTimeSum[finalI][INQUERY] += dur;

						}
					}
				}
			});
			threads[i].start();
		}

		for (int i = 0; i < threadnum; i++) {
			threads[i].join();
		}
		long totalTime = System.currentTimeMillis() - startTime; //获取总时间

		double avgBuy = 0, avgRefund = 0, avgInqui = 0, totalCount = 0 , goodtotalCount = 0,
				badtotalCount = 0;
		for (int i = 0; i < threadnum; i++) {
			totalCount += executeCount[i][BUY] + executeCount[i][REFUND] + executeCount[i][INQUERY];
			goodtotalCount += goodCount[i][BUY] + goodCount[i][REFUND] + goodCount[i][INQUERY];
			badtotalCount += badCount[i][BUY] + badCount[i][REFUND] + badCount[i][INQUERY];

			avgRefund += functionCostTimeSum[i][REFUND] * 1.0 / executeCount[i][REFUND];
			avgBuy += functionCostTimeSum[i][BUY] * 1.0 / executeCount[i][BUY];
			avgInqui += functionCostTimeSum[i][INQUERY] * 1.0 / executeCount[i][INQUERY];
		}
		avgBuy /= threadnum;
		avgRefund /= threadnum;
		avgInqui /= threadnum;

		return new TestResult(avgBuy, avgRefund, avgInqui,
				1.0 * totalCount * 1000 / totalTime,
				1.0 * goodtotalCount * 1000 / totalTime,
				1.0 * badtotalCount * 1000 / totalTime
				);
	}

	public static void main(String[] args) throws Exception {
		int each = 5;//多次测试取平均，此处为5次取平均

		Test test = new Test();
		TestResult[] testResult = new TestResult[thread_nums.length];
		for (int i = 0; i < thread_nums.length; i++) {
			testResult[i] = new TestResult();
			for (int j = 0; j < each; j++) {
				TestResult tmp = test.test(thread_nums[i],testnum,routenum,coachnum,seatnum,stationnum);
				testResult[i].avgSingleBuyTicketTime += tmp.avgSingleBuyTicketTime;
				testResult[i].avgSingleInquiryTime += tmp.avgSingleInquiryTime;
				testResult[i].avgSingleRefundTime += tmp.avgSingleRefundTime;
				testResult[i].throughput += tmp.throughput;
				testResult[i].successThroughPut += tmp.successThroughPut;
				testResult[i].failedThroughPut += tmp.failedThroughPut;

			}
			testResult[i].avgSingleBuyTicketTime /= each;
			testResult[i].avgSingleRefundTime /= each;
			testResult[i].avgSingleInquiryTime /= each;
			testResult[i].throughput /= each;
			testResult[i].successThroughPut /= each;
			testResult[i].failedThroughPut /= each;

			System.out.println("Thread: " + thread_nums[i] + "\n" + testResult[i]);
		}
	}
}