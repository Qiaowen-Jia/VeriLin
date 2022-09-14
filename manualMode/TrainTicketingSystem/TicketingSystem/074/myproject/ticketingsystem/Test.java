package ticketingsystem;

// public class Test {
// 	final static int routenum = 3; // route is designed from 1 to 3
// 	final static int coachnum = 3; // coach is arranged from 1 to 5
// 	final static int seatnum = 3; // seat is allocated from 1 to 20
// 	final static int stationnum = 3; // station is designed from 1 to 5
// 	//To do list
// 	//测mask 单线程通过
// 	//测查询 
// 	//测买票
// 	//测退票
// 	//多线程测性能
// 	public static void main(String[] args) throws InterruptedException {
// 		// final TicketingDS tds = new TicketingDS();
// 		final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, 1);
// 		//测试单线程mask
// 		// System.out.println(Integer.toBinaryString(tds.getMask(3, 5)));
// 		// System.out.println(Integer.toBinaryString(tds.getMaskForRefund(1, 5)));
// 		// System.out.println(Integer.toBinaryString(Integer.MAX_VALUE));
		

// 		//测试单线程查询
// 		// System.out.println(tds.inquiry(3, 3, 5));
// 		// System.out.println(tds.inquiry(5, 1, 5));

// 		//单线程买票
// 		Ticket t1=tds.buyTicket("passenger1", 2, 2, 3);
// 		System.out.println(t1.toString()); 
// 		System.out.println(tds.inquiry(2, 1, 3));

// 		// Ticket t2=tds.buyTicket("passenger2", 2, 6, 8);
// 		// System.out.println(t2.toString()); 
// 		// System.out.println(tds.inquiry(2, 6, 8));
// 		// System.out.println(tds.inquiry(2, 4, 8));
// 		// System.out.println(tds.inquiry(2, 4, 6));
// 		// //单线程退票
// 		// t1.coach=4;
// 		// if(tds.refundTicket(t1)){
// 		// 	System.out.println("退票成功，系统出错");
// 		// }
// 		// System.out.println(tds.inquiry(2, 6, 8));
// 		// System.out.println(tds.inquiry(2, 4, 8));
// 		// System.out.println(tds.inquiry(2, 4, 6));
// 		//ToDo
// 	}
// }

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class Test {

	private static int threadnum = 32; // concurrent thread number
	private static int routenum = 20; // route is designed from 1 to 3
	private static int coachnum = 15; // coach is arranged from 1 to 5
	private static int seatnum = 100; // seat is allocated from 1 to 20
	private static int stationnum = 10; // station is designed from 1 to 5

	private static int testnum = 100000;
	private static int retpc = 5; // return ticket operation is 10% percent
	private static int buypc = retpc + 15; // buy ticket operation is 30% percent
	private static int inqpc = buypc + 80; //inquiry ticket operation is 60% percent

	public static void main(String[] args) throws InterruptedException {
		// Read thread_num
        if (args.length > 0) {
        	threadnum = Integer.parseInt(args[0]);
		}
		final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);

		final AtomicLong[] time = new AtomicLong[3];
		final AtomicLong[] count = new AtomicLong[3];
		for (int i = 0; i< 3; i++) {
			time[i] = new AtomicLong(0);
			count[i] = new AtomicLong(0);
		}

		Thread[] threads = new Thread[threadnum];
		for (int i = 0; i< threadnum; i++) {
			threads[i] = new Thread(() -> {
				Random rand = new Random();
				Ticket t;
				long start, end;
				int sel;

				ArrayList<Ticket> soldTicket = new ArrayList<>();

				for (int i1 = 0; i1 < testnum; i1++) {
					sel = rand.nextInt(inqpc);
					if (0 <= sel && sel < retpc && soldTicket.size() > 0) {
						// Refund
						start = System.nanoTime();
						{
							tds.refundTicket(soldTicket.remove(0));
						}
						end = System.nanoTime();
						time[0].addAndGet(end - start);
						count[0].incrementAndGet();
					} else if (retpc <= sel && sel < buypc) {
						// Buy
						int route = rand.nextInt(routenum) + 1;
						int departure = rand.nextInt(stationnum - 1) + 1;
						int arrival = departure + rand.nextInt(stationnum - departure) + 1;
						start = System.nanoTime();
						{
							t = tds.buyTicket("p", route, departure, arrival);
						}
						end = System.nanoTime();
						time[1].addAndGet(end - start);
						count[1].incrementAndGet();

						if (t != null) soldTicket.add(t);
					} else if (buypc <= sel && sel < inqpc) {
						// Inquiry
						int route = rand.nextInt(routenum) + 1;
						int departure = rand.nextInt(stationnum - 1) + 1;
						int arrival = departure + rand.nextInt(stationnum - departure) + 1;
						start = System.nanoTime();
						{
							tds.inquiry(route, departure, arrival);
						}
						end = System.nanoTime();
						time[2].addAndGet(end - start);
						count[2].incrementAndGet();
					}
				}

			});
		}

		long total_start, total_end;
		total_start = System.nanoTime();
		{
			for (int i = 0; i< threadnum; i++) {
				threads[i].start();
			}

			for (int i = 0; i< threadnum; i++) {
				threads[i].join();
			}
		}
		total_end = System.nanoTime();

		// Output
		long total_count = count[0].longValue() + count[1].longValue() + count[2].longValue();
		long total_time = total_end - total_start;
		System.out.println("Refund         " + time[0].longValue() / count[0].longValue() + " ns");
		System.out.println("Buy            " + time[1].longValue() / count[1].longValue() + " ns");
		System.out.println("Inquiry        " + time[2].longValue() / count[2].longValue() + " ns");
		System.out.println("Total_time     " + total_time / 1000000 + " ms");
		System.out.println("Total_count    " + total_count);
		System.out.println("Total          " + (total_count / ((float)total_time / 1000000000)) + " op/s");
	}
}
