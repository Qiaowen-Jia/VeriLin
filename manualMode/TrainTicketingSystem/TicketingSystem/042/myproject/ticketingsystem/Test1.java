package ticketingsystem;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class Test1 {

	final static int[] threadnums = {4, 8, 16, 32, 64}; // concurrent thread number
	final static int routenum = 5; // route is designed from 1 to 3
	final static int coachnum = 8; // coach is arranged from 1 to 5
	final static int seatnum = 100; // seat is allocated from 1 to 20
	final static int stationnum = 10; // station is designed from 1 to 5

	final static int testnum = 10000;
	final static int retpc = 10; // return ticket operation is 10% percent
	final static int buypc = 40; // buy ticket operation is 30% percent
	final static int inqpc = 100; //inquiry ticket operation is 60% percent

	static String passengerName() {
		Random rand = new Random();
		long uid = rand.nextInt(testnum);
		return "passenger" + uid;
	}

	public static void main(String[] args) throws InterruptedException{
        
		for(int i=1;i<=10;i++)
		{
			for(int threadnum : threadnums){
				work(threadnum);
			}
		}
	}

	public static void work(int threadnum)throws InterruptedException {
		final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);

		Thread[] threads = new Thread[threadnum];

		final AtomicLong totalRetNum = new AtomicLong(0);
		final AtomicLong totalBuyNum = new AtomicLong(0);
		final AtomicLong totalQueryNum = new AtomicLong(0);

		final AtomicLong totalRetTime = new AtomicLong(0);
		final AtomicLong totalBuyTime = new AtomicLong(0);
		final AtomicLong totalQueryTime = new AtomicLong(0);

		for (int i = 0; i< threadnum; i++) {
			threads[i] = new Thread(new Runnable() {
				public void run() {
					Random rand = new Random();
					Ticket ticket;
					ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();
					long retNum = 0, buyNum = 0, queryNum = 0;
					long retTime = 0, buyTime = 0, queryTime = 0;
					long begin;
					for (int i = 0; i < testnum; i++) {
						int sel = rand.nextInt(inqpc);
						if (0 <= sel && sel < retpc && soldTicket.size() > 0) {
							int select = rand.nextInt(soldTicket.size());
							if ((ticket = soldTicket.remove(select)) != null) {
								begin = System.nanoTime();
								boolean result = tds.refundTicket(ticket);
								retTime += System.nanoTime() - begin;
								++retNum;
								if (!result) {
									System.out.println("ErrOfRefund");
									System.out.flush();
								}
							} else {
								System.out.println("ErrOfRefund");
								System.out.flush();
							}
						} else if (retpc <= sel && sel < buypc) { // buy ticket
							String passenger = passengerName();
							int route = rand.nextInt(routenum) + 1;
							int departure = rand.nextInt(stationnum - 1) + 1;
							int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
							begin = System.nanoTime();
							ticket = tds.buyTicket(passenger, route, departure, arrival);
							buyTime += System.nanoTime() - begin;
							++buyNum;
							if (ticket != null) {
								soldTicket.add(ticket);
							}
						} else if (buypc <= sel && sel < inqpc) { // inquiry ticket
							int route = rand.nextInt(routenum) + 1;
							int departure = rand.nextInt(stationnum - 1) + 1;
							int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
							begin = System.nanoTime();
							tds.inquiry(route, departure, arrival);
							queryTime += System.nanoTime() - begin;
							++queryNum;
						}
					}
					totalRetNum.addAndGet(retNum);
					totalBuyNum.addAndGet(buyNum);
					totalQueryNum.addAndGet(queryNum);

					totalRetTime.addAndGet(retTime);
					totalBuyTime.addAndGet(buyTime);
					totalQueryTime.addAndGet(queryTime);
				}
			});
		}

		long beginTime = System.currentTimeMillis();
		for(int i = 0; i < threadnum; i++){
			threads[i].start();
		}

		for (int i = 0; i< threadnum; i++) {
			threads[i].join();
		}
		long executionTime = System.currentTimeMillis() - beginTime;
		System.out.println("Thread:" + threadnum  + " Time:" + (double)(executionTime)/1000 + " Throughout:" + testnum * threadnum*1000 /executionTime + "");
		/*
		System.out.println("==== ThreadNum: " + threadnum + ", " + testnum + " op per thread ====");
		System.out.println("Total op num: " + testnum * threadnum);
		System.out.println("RouteNum: " + routenum + ", CoachNum: " + coachnum +
				", SeatNum: " + seatnum + ", StationNum: " + stationnum);
		System.out.println("Total execution times: " + executionTime);
		System.out.println("RetNum: " + totalRetNum.get() + "\tBuyNum: " +
				totalBuyNum.get() + "\tQueryNum:" +
				totalQueryNum.get());
		System.out.println("Ret: "+ (totalRetTime.get() / totalRetNum.get()) + " ns/op" +
				"\tBuy: " + (totalBuyTime.get() / totalBuyNum.get()) + " ns/op" +
				"\tQuery: " + (totalQueryTime.get() / totalQueryNum.get()) + " ns/op");
		System.out.println("Throughput: " + (double)(totalRetNum.get() + totalBuyNum.get() + totalQueryNum.get()) /
				(totalRetTime.get() + totalBuyTime.get() + totalQueryTime.get()) * threadnum * 10e6 + " kop/s");
		System.out.println("Throughput: " + (double)(testnum * threadnum) / executionTime + " kop/s");
		System.out.println("\n\n");
		*/
	}
}
