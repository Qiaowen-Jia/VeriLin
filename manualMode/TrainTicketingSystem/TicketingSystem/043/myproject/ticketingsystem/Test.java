package ticketingsystem;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Test {

	final static  int routenum = 5;
	final static int coachnum = 8;
	final static int seatnum = 100;
	final static int stationnum = 10;


	final static int testnum = 50000;

	final static int refund = 10;
	final static int buy = 40;
	final static int query = 100;
	final static int thread = 64;

	final static long [] buyTicketTime = new long [thread];
	final static long [] refundTicketTime = new long [thread];
	final static long [] inquriryTime = new long [thread];

	final static long [] buyTicketTotal = new long [thread];
	final static long [] refundTotal = new long [thread];
	final static long [] inquiryTotal = new long [thread];

			
	final static AtomicInteger tid = new AtomicInteger(0);

	static String passengerName() {
		Random rand = new Random();
		long uid = rand.nextInt(testnum);
		return "passenger" + uid; 
	}
	

	public static void main(String[] args) throws InterruptedException {
		final int[] threadNums = {1,4,8,16,32,64};
		int k;
	for (k = 0; k < threadNums.length; k++){
		int threadnum = threadNums[k];

		Thread[] threads = new Thread[threadnum];
		
		final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
		
		final long startTime = System.nanoTime();
	    
	for (int i = 0; i< threadnum; i++) {
	    	threads[i] = new Thread(new Runnable() {
                public void run() {
            		Random rand = new Random();
									Ticket ticket = new Ticket();
								int id = tid.getAndIncrement();
            		ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();
            		
             		for (int i = 0; i < testnum; i++) {
            			int sel = rand.nextInt(query);
            			if (0 <= sel && sel < refund && soldTicket.size() > 0) { // return ticket
            				int select = rand.nextInt(soldTicket.size());
           				if ((ticket = soldTicket.remove(select)) != null) {
											long preTime = System.nanoTime() - startTime;
            					if (tds.refundTicket(ticket)) {
												long postTime = System.nanoTime() - startTime;
												refundTicketTime[id] += postTime - preTime;
												refundTotal[id] += 1;
            					} else {
            						System.out.println(preTime + " " + String.valueOf(System.nanoTime()-startTime) + " " + ThreadId.get() + " " + "ErrOfRefund");
            						System.out.flush();
            					}
            				} else {
											long preTime = System.nanoTime() - startTime;
            					System.out.println(preTime + " " + String.valueOf(System.nanoTime()-startTime) + " " + ThreadId.get() + " " + "ErrOfRefund");
        						System.out.flush();
            				}
            			} else if (refund <= sel && sel < buy) { // buy ticket
            				String passenger = passengerName();
            				int route = rand.nextInt(routenum) + 1;
            				int departure = rand.nextInt(stationnum - 1) + 1;
            				int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
										long preTime = System.nanoTime() - startTime;
            				if ((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null) {
											long postTime = System.nanoTime() - startTime;
											buyTicketTime[id] += postTime - preTime;
											buyTicketTotal[id] += 1;
											soldTicket.add(ticket);
        					
            				} else {
            					// empty
            				}
            			} else if (buy <= sel && sel < query) { // inquiry ticket
            				
            				int route = rand.nextInt(routenum) + 1;
            				int departure = rand.nextInt(stationnum - 1) + 1;
            				int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
										long preTime = System.nanoTime() - startTime;
            				int leftTicket = tds.inquiry(route, departure, arrival);
										long postTime = System.nanoTime() - startTime;
										inquriryTime[id] += postTime - preTime;
										inquiryTotal[id] += 1;   			
            			}
            		}

                }
            });
 	    }
	
			long start = System.currentTimeMillis();
			for (int i = 0; i< threadnum; i++) {
				threads[i].start();
			}
	    for (int i = 0; i< threadnum; i++) {
	    	threads[i].join();
			}		
			
			long end = System.currentTimeMillis();

			long buyTotalTime = accumulateAll(buyTicketTime, threadnum);
			long refundTotalTime = accumulateAll(refundTicketTime, threadnum);
			long inquiryTotalTime = accumulateAll(inquriryTime, threadnum);
			
			double buyTotalCount = accumulateAll(buyTicketTotal, threadnum);
			double refundTotalCount = accumulateAll(refundTotal, threadnum);
			double inquiryTotalCount = accumulateAll(inquiryTotal, threadnum);

			long buyAvgTime = (long) (buyTotalTime/buyTotalCount);
			long refundAvgTime = (long) (refundTotalTime/refundTotalCount);
			long inquiryAvgTime = (long) (inquiryTotalTime/inquiryTotalCount);

			long timeTotal = end - start;

			long through = (long) (threadnum * testnum / (double)timeTotal) * 1000;

			System.out.println(String.format(
					"ThreadNum: %d BuyAvgTime(ns): %d RefundAvgTime(ns): %d InquiryAvgTime(ns): %d ThroughOut(t/s): %d",
					threadnum, buyAvgTime, refundAvgTime, inquiryAvgTime, through));

			tid.set(0);
			clean(buyTicketTime);
			clean(refundTicketTime);
			clean(inquriryTime);
			clean(buyTicketTotal);
			clean(refundTotal);
			clean(inquiryTotal);
			
	}
	    
	}

	private static long accumulateAll (long [] array, int threadNums) {
		long result = 0;
		for (int k = 0; k < threadNums; k++){
			result += array[k];
		}
		return result;
	}

	private static void clean (long [] array ) {
		for (int i = 0; i< array.length; i++) {
			array[i] = 0;
		}
	}

}



