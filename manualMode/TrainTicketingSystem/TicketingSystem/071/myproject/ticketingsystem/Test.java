
package ticketingsystem;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;


public class Test {
	//初始化列车信息
	private final static int routnum = 5;
	private final static int coachnum = 8;
	private final static int seatnum = 100;
	private final static int stationnum = 10;
//设置测试次数和测试占用比例
	private final static int testnum = 10000;
	private final static int refundticket = 10;
	private final static int buyticket = 40;
	private final static int queryticket = 100;
	private final static int threadnum = 128;
	//统计用时，个例
	private final static long[] buyTicketTime = new long[threadnum];
	private final static long[] refundTime = new long[threadnum];
	private final static long[] inquiryTime = new long[threadnum];
//统计用时，全部进行的操作次数
	private final static long[] buytotaltimes = new long[threadnum];
	private final static long[] refundtotaltimes = new long[threadnum];
	private final static long[] inquirytotaltimes = new long[threadnum];
//给线程标号
	private final static AtomicInteger threadId = new AtomicInteger(0);
	//随机生成乘客的信息
	static String passengerName() {
		Random rand = new Random();
		long pnum = rand.nextInt(testnum);
		return "passenger" + pnum;
	}
	//main函数开始调用并发数据结构，进行操作
	public static void main(String[] args) throws InterruptedException {
		final int[] threadnums = { 4, 8, 16, 32, 64 ,128/*,1024,2048,4096,5120*/};
		//生成线程
		int threadn;
		for (threadn = 0; threadn < threadnums.length; ++threadn) {
			final TicketingDS tds = new TicketingDS(routnum, coachnum, seatnum, stationnum, threadnums[threadn]);
		//按照数组要求开始生成线程数组
			Thread[] threads = new Thread[threadnums[threadn]];
			//线程初始化
			for (int i = 0; i < threadnums[threadn]; i++) {
				threads[i] = new Thread(new Runnable() {
					public void run() {
						Random rand = new Random();
						Ticket ticket = new Ticket();
						//线程标号唯一，使用原语
						int TID = threadId.getAndIncrement();
						//生存票组
						ArrayList<Ticket> soldTicket = new ArrayList<>();
						for (int i = 0; i < testnum; i++) {
							int sel = rand.nextInt(queryticket);
							//开始测试，根据随机数，判断执行的是何种操作
							//同时，没有票的时候，不能退票
							if (0 <= sel && sel < refundticket && soldTicket.size() > 0) { // refund ticket 0-10
								int select = rand.nextInt(soldTicket.size());
								if ((ticket = soldTicket.remove(select)) != null) {
									long s = System.nanoTime();
									tds.refundTicket(ticket);
									//对退票时间进行计时
									long e = System.nanoTime();
									refundTime[TID] += e - s;
									refundtotaltimes[TID] += 1;
									//退票次数和退票时间都要进行相应的统计
								} else {
									//退票失败，打印错误
									System.out.println("ErrOfRefund2");
								}
							} else if (refundticket <= sel && sel < buyticket) { // buy ticket 10-40
								//处于买票的情况，进行买票
								//随机生成乘客信息，选择买卖票的车次，站号
								String passenger = passengerName();
								int route = rand.nextInt(routnum) + 1;
								int departure = rand.nextInt(stationnum - 1) + 1;
								int arrival = departure + rand.nextInt(stationnum - departure) + 1;
								long s = System.nanoTime();
								//根据给定的乘客，车次，开始，结束地点进行买票
								ticket = tds.buyTicket(passenger, route, departure, arrival);
								long e = System.nanoTime();
								buyTicketTime[TID] += e - s;
								buytotaltimes[TID] += 1;
								//计时，买一张票所需的时间
								if (ticket != null) {
									soldTicket.add(ticket);
									//买的这张票存在，就压到数组里面
								}
							} else if (buyticket <= sel && sel < queryticket) { // inquiry ticket 40-100
								int route = rand.nextInt(routnum) + 1;
								//选择查询的车次，查询的车站起发站点，返回余票数量
								int departure = rand.nextInt(stationnum - 1) + 1;
								int arrival = departure + rand.nextInt(stationnum - departure) + 1;
								long s = System.nanoTime();
								tds.inquiry(route, departure, arrival);
								long e = System.nanoTime();
								inquiryTime[TID] += e - s;
								inquirytotaltimes[TID] += 1;
								//记录查票的时间
							}
						}
					}
				});
			}
			long start = System.currentTimeMillis();
			for (int i = 0; i < threadnums[threadn]; ++i)
				threads[i].start();
			
			//让各个线程都开始运行，同时把线程加入等待队列，join之后挂起。让主线程等待子线程全部结束

			for (int i = 0; i < threadnums[threadn]; i++) {
				threads[i].join();
			}
			long end = System.currentTimeMillis();
			//线程全部结束
			//使用求和函数，求所有线程的和在一起
			long buyTotalTime = calculateTotal(buyTicketTime, threadnums[threadn]);
			long refundTotalTime = calculateTotal(refundTime, threadnums[threadn]);
			long inquiryTotalTime = calculateTotal(inquiryTime, threadnums[threadn]);

			double bTotal = (double) calculateTotal(buytotaltimes, threadnums[threadn]);
			double rTotal = (double) calculateTotal(refundtotaltimes, threadnums[threadn]);
			double iTotal = (double) calculateTotal(inquirytotaltimes, threadnums[threadn]);

			long buyAvgTime = (long) (buyTotalTime / bTotal);
			long refundAvgTime = (long) (refundTotalTime / rTotal);
			long inquiryAvgTime = (long) (inquiryTotalTime / iTotal);
//计算平均的退票，买票，查票次数
			long time = end - start;//此处的时间是毫秒

			long t = (long) (threadnums[threadn] * testnum / (double) time) * 1000; //毫秒要变成秒
			System.out.println(String.format(
					"ThreadNum: %d BuyAvgTime(ns): %d RefundAvgTime(ns): %d InquiryAvgTime(ns): %d ThroughOut(t/s): %d",
					threadnums[threadn], buyAvgTime, refundAvgTime, inquiryAvgTime, t));
			clear();//清空当前并发情况下的数据，开始下一组并发多线程的统计
		}
	}
//求和函数
	private static long calculateTotal(long[] array, int threadNums) {
		long res = 0;
		for (int i = 0; i < threadNums; ++i)
			res += array[i];
		return res;
	}

	private static void clear() {
		threadId.set(0);
		long[][] arrays = { buyTicketTime, refundTime, inquiryTime, buytotaltimes, refundtotaltimes, inquirytotaltimes };
		for (int i = 0; i < arrays.length; ++i)
			for (int j = 0; j < arrays[i].length; ++j)
				arrays[i][j] = 0;
	}

}