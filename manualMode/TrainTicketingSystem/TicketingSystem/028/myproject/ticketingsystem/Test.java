package ticketingsystem;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


public class Test {
	static int threadnum = 64;
	static int routenum = 20; // route is designed from 1 to 3
	static int coachnum = 10; // coach is arranged from 1 to 5
	static int seatnum = 100; // seat is allocated from 1 to 20
	static int stationnum = 16; // station is designed from 1 to 5

	final static int testnum = 100000;
	final static int retpc = 10; // return ticket operation is 10% percent
	final static int buypc = 30; // buy ticket operation is 30% percent
	final static int inqpc = 100; // inquiry ticket operation is 60% percent

	static String passengerName() {
		Random rand = new Random();
		long uid = rand.nextInt(testnum);
		return "passenger" + uid;
	}

	public static void main(String[] args) throws InterruptedException {
    final long pTime = System.nanoTime();
		threadnum = Integer.parseInt(args[0]);
		routenum = Integer.parseInt(args[1]);
		coachnum = Integer.parseInt(args[2]);
		seatnum = Integer.parseInt(args[3]);
		stationnum = Integer.parseInt(args[4]);

		Thread[] threads = new Thread[threadnum];

		final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
		AtomicInteger totalBuyCount = new AtomicInteger(0);
		AtomicLong totalBuyTime = new AtomicLong(0);
		AtomicInteger totalRefundCount = new AtomicInteger(0);
		AtomicLong totalRefundTime = new AtomicLong(0);
		AtomicInteger totalInquiryCount = new AtomicInteger(0);
		AtomicLong totalInquiryTime = new AtomicLong(0);
		for (int i = 0; i < threadnum; i++) {
			threads[i] = new Thread(new Runnable() {
				public void run() {
					int buyCount = 0;
					long buyTime = 0;
					int refundCount = 0;
					long refundTime = 0;
					int inquiryCount = 0;
					long inquiryTime = 0;
					Random rand = new Random();
					Ticket ticket = new Ticket();
					ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();
					for (int i = 0; i < testnum; i++) {
						int sel = rand.nextInt(inqpc);
						if (0 <= sel && sel < retpc && soldTicket.size() > 0) { // return ticket
							int select = rand.nextInt(soldTicket.size());
							if ((ticket = soldTicket.remove(select)) != null) {
								long preTime = System.nanoTime();
								tds.refundTicket(ticket);
								long postTime = System.nanoTime();
								refundTime += postTime - preTime;
								refundCount++;
							}
						} else if (retpc <= sel && sel < buypc) { // buy ticket
							String passenger = passengerName();
							int route = rand.nextInt(routenum) + 1;
							int departure = rand.nextInt(stationnum - 1) + 1;
							int arrival = departure + rand.nextInt(stationnum - departure) + 1;
							long preTime = System.nanoTime();
							if ((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null) {
								soldTicket.add(ticket);
							}
							long postTime = System.nanoTime();
							buyTime += postTime - preTime;
							buyCount++;
						} else if (buypc <= sel && sel < inqpc) { // inquiry ticket
							int route = rand.nextInt(routenum) + 1;
							int departure = rand.nextInt(stationnum - 1) + 1;
							int arrival = departure + rand.nextInt(stationnum - departure) + 1;
							long preTime = System.nanoTime();
							tds.inquiry(route, departure, arrival);
							long postTime = System.nanoTime();
							inquiryTime += postTime - preTime;
							inquiryCount++;
						}
					}
					totalBuyCount.addAndGet(buyCount);
					totalBuyTime.addAndGet(buyTime);
					totalRefundCount.addAndGet(refundCount);
					totalRefundTime.addAndGet(refundTime);
					totalInquiryCount.addAndGet(inquiryCount);
					totalInquiryTime.addAndGet(inquiryTime);
				}
			});
			threads[i].start();
		}

		for (int i = 0; i < threadnum; i++) {
			threads[i].join();
		}
    final long eTime = System.nanoTime();
		System.out.println("Buy    :\t" + (double)(totalBuyTime.get()) / totalBuyCount.get());
		System.out.println("Refund :\t" + (double)(totalRefundTime.get()) / totalRefundCount.get());
		System.out.println("Inquiry:\t" + (double)(totalInquiryTime.get()) / totalInquiryCount.get());
    System.out.println("Ops/s  :\t" + (double)(threadnum * testnum) * 1000000000.0d / (eTime - pTime));
	}
}
