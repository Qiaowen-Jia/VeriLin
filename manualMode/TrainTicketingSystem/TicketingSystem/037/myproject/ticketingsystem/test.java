package ticketingsystem;

import java.util.*;

//Test.java实现多线程性能测试。
public class test {
	/*
	 * 系统中同时存在threadnum个线程（缺省为16个），每个线程是一个票务代理
	 * 按照60%查询余票，30%购票和10%退票的比率反复调用TicketingDS类的三种方法若干次（缺省为总共10000次）。 //负载为
	 * 20个车次，每个车次15节车厢，每节车厢100个座位，途经10个车站。共有96个线程并发执行，
	 * 按照线程数为4，8，16，32，64个的情况分别给出每种方法调用的平均执行时间。 同时计算系统的总吞吐率（单位时间内完成的方法调用总数）。
	 * 正式测试每个线程的操作改为500000次
	 */
	static int routenum = 20;// 车次数为5
	static int coachnum = 15;// 车厢数为8
	static int seatnum = 100;// 座位数为100
	static int stationnum = 10;// 车站数为10，则出发地点1~9，目的地点2~10
	static int threadnum = 4;// 并发购票的线程数
	static int testnumber = 500000;
	static int retpc = 10; // 10% 退票操作
	final static int buypc = 40; // 30%购买操作
	final static int inqpc = 100; // 60%查询操作

	static String passengerName() {
		Random rand = new Random();
		long uid = rand.nextInt(testnumber);
		return "passenger" + uid;
	}

	public static void run(TicketingDS tds, int threadnum) throws InterruptedException {
		Thread[] threads = new Thread[threadnum];
		// 开始计时
		long begin = System.currentTimeMillis();
		for (int i = 0; i < threadnum; i++) {
			threads[i] = new Thread(new Runnable() {
				public void run() {
					Random rand = new Random();
					Ticket ticket = new Ticket();
					ArrayList<Ticket> sellTicket = new ArrayList<Ticket>();

					// 利用随机函数生成0~99的随机数，0~retpc执行退票操作，retpc~buypc执行购买操作，buypc到99执行查询操作
					// System.out.println(ThreadId.get());
					for (int i = 0; i < testnumber; i++) {
						int sel = rand.nextInt(inqpc);
						if (0 <= sel && sel < retpc && sellTicket.size() > 0) {// 退票
							int select = rand.nextInt(sellTicket.size());
							if ((ticket = sellTicket.remove(select)) != null) {
								tds.refundTicket(ticket);
								// System.out.println("退票成功");
							}
							// else System.out.println("退票失败");
						} else if (retpc <= sel && sel < buypc) {// 购买
							String passenger = passengerName();
							int route = rand.nextInt(routenum) + 1;
							int departure = rand.nextInt(stationnum - 1) + 1;
							int arrival = departure + rand.nextInt(stationnum - departure) + 1;
							if ((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null) {
								sellTicket.add(ticket);
								// System.out.println("购买成功");
							}
							// else System.out.println("购买失败");
						} else if (buypc <= sel && sel < inqpc) { // 查询
							int route = rand.nextInt(routenum) + 1;
							int departure = rand.nextInt(stationnum - 1) + 1;
							int arrival = departure + rand.nextInt(stationnum - departure) + 1;
							int leftTicket = tds.inquiry(route, departure, arrival);
							/*
							 * System.out.println("RemainTicket" + " " + leftTicket + " " + route + " " +
							 * departure + " " + arrival); System.out.flush();
							 */
						}
					}

				}
			});
			threads[i].start();// 开始执行run方法，
		}

		for (int i = 0; i < threadnum; i++) {
			threads[i].join();
		}
		// 结束计时
		long end = System.currentTimeMillis();
		System.out.print("总时间=" + (end - begin) + "毫秒");
		System.out.println("   线程数=" + threadnum + "   吞吐量=" + threadnum * testnumber / ((double) (end - begin) / 1000) + "个方法调用每秒");

	}

	public static void main(String[] args) throws InterruptedException {

		TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
		run(tds, 4);
		tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
		run(tds, 8);
		tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
		run(tds, 16);
		tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
		run(tds, 32);
		tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
		run(tds, 64);
		tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
		run(tds, 96);
	}
}