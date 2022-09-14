package ticketingsystem;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class Test {
	final static int[] threadnums = {1, 2, 4, 8, 16, 24, 32, 40, 48, 56, 64, 72, 80, 88, 96, 104, 112, 120, 128, 136, 144, 152, 160};
	final static int routenum = 5; 
	final static int coachnum = 8; 
	final static int seatnum = 100; 
	final static int stationnum = 64;

	final static int testnum = Math.min(routenum*coachnum*seatnum*stationnum, 1000000);
	final static int retpc = 10; 
	final static int buypc = 40; 
	final static int inqpc = 100; 
	

	static String passengerName() {
		long uid = ThreadLocalRandom.current().nextInt();
		return "passenger" + uid;
	}

	public static void main(String[] args) throws InterruptedException{
		System.out.println("routeNum: "+routenum+" stationNum: "+stationnum+" coachNum: "+coachnum+" seatNum: "+seatnum+" testNum: "+testnum+"/thread");
		for(int threadnum : threadnums){
			operationForTicketingDS(threadnum);
		}
	}
	
	
	public static void operationForTicketingDS(int threadnum) throws InterruptedException {
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
					Ticket ticket;
					ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();
					long retNum = 0, buyNum = 0, queryNum = 0;
					long retTime = 0, buyTime = 0, queryTime = 0;
					long begin;
					for (int i = 0; i < testnum; i++) {
						int sel = ThreadLocalRandom.current().nextInt(inqpc);
						if (0 <= sel && sel < retpc && soldTicket.size() > 0) { // refund ticket
							if ((ticket = soldTicket.remove(0)) != null) {
								begin = System.nanoTime();
								boolean result = tds.refundTicket(ticket);
								retTime += System.nanoTime() - begin;
								++retNum;
								if (!result) {
									System.out.println("ErrOfRefund1");
									System.out.flush();
								}
							} else {
								System.out.println("ErrOfRefund2");
								System.out.flush();
							}
						} else if (retpc <= sel && sel < buypc) { // buy ticket
							int departure = ThreadLocalRandom.current().nextInt(stationnum - 1) + 1;
							int arrival = departure + ThreadLocalRandom.current().nextInt(stationnum - departure) + 1; // arrival is always greater than departure
							begin = System.nanoTime();
							ticket = tds.buyTicket(passengerName(), ThreadLocalRandom.current().nextInt(routenum) + 1, departure, arrival);
							buyTime += System.nanoTime() - begin;
							++buyNum;
							if (ticket != null) {
								soldTicket.add(ticket);
							}
						} else if (buypc <= sel && sel < inqpc) { // inquiry ticket
							int departure = ThreadLocalRandom.current().nextInt(stationnum - 1) + 1;
							int arrival = departure + ThreadLocalRandom.current().nextInt(stationnum - departure) + 1; // arrival is always greater than departure
							begin = System.nanoTime();
							tds.inquiry(ThreadLocalRandom.current().nextInt(routenum) + 1, departure, arrival);
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
		double executionTime = (System.currentTimeMillis() - beginTime)/1000.0;
		double throughput = (threadnum * testnum / executionTime);
		System.out.printf("threadNum:%3d|buy:%6d ns/op|Ret:%6d ns/op|Query:%6d ns/op|time:%5.2f|Throughput:%8.0f\n",threadnum, (totalBuyTime.get() / totalBuyNum.get()), (totalRetTime.get() / totalRetNum.get()), (totalQueryTime.get() / totalQueryNum.get()), executionTime, throughput);
	}

}
