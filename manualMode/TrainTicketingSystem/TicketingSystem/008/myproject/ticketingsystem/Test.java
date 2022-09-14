package ticketingsystem;

import java.util.*;

import java.util.concurrent.atomic.AtomicInteger;

class ThreadId {
    // Atomic integer containing the next thread ID to be assigned
    private static final AtomicInteger nextId = new AtomicInteger(0);

    // Thread local variable containing each thread's ID
    private static final ThreadLocal<Integer> threadId =
        new ThreadLocal<Integer>() {
            @Override protected Integer initialValue() {
                return nextId.getAndIncrement();
        }
    };

    // Returns the current thread's unique ID, assigning it if necessary
    public static int get() {
        return threadId.get();
    }
}

public class Test {
	final static int[] threadnums = {4,8,16,32,64,96}; // concurrent thread number
	// final static int[] threadnums = {64}; // concurrent thread number
	final static int routenum = 20; // route is designed from 1 to 3
	final static int coachnum = 15; // coach is arranged from 1 to 5
	final static int seatnum = 100; // seat is allocated from 1 to 20
	final static int stationnum = 10; // station is designed from 1 to 5

	final static int testnum = 500000;
	final static int retpc = 5; // return ticket operation is 10% percent
	final static int buypc = 20; // buy ticket operation is 30% percent
	final static int inqpc = 100; //inquiry ticket operation is 60% percent

	final static int execution_count = 5;

	static String passengerName() {
		Random rand = new Random();
		long uid = rand.nextInt(testnum);
		return "passenger" + uid; 
	}

	public static void main(String[] args) throws InterruptedException {
        
		for(int thread = 0; thread < threadnums.length; thread++){
			final long[] totalTime = new long[execution_count];
			final long[][] buyUsedTime = new long[execution_count][threadnums[thread]];
			final long[][] refundUsedTime = new long[execution_count][threadnums[thread]];
			final long[][] inquiryUsedTime = new long[execution_count][threadnums[thread]];
			final int[][] buyExecuteNumber = new int[execution_count][threadnums[thread]];
			final int[][] refundExecuteNumber = new int[execution_count][threadnums[thread]];
			final int[][] inquiryExecuteNumber = new int[execution_count][threadnums[thread]];
			for(int e = 0; e < execution_count; e++){
				final int execute = e;
				final long[] singleStartTime = new long[threadnums[thread]];
				Thread[] threads = new Thread[threadnums[thread]];
				final long totalStartTime = System.currentTimeMillis();
				final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnums[thread]);		
				for (int i = 0; i< threadnums[thread]; i++) {
					final int threadId = i;
					threads[i] = new Thread(new Runnable() {
						public void run() {
							Random rand = new Random();
							Ticket ticket = new Ticket();
							ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();
							
							//System.out.println(ThreadId.get());
							for (int j = 0; j < testnum; j++) {
								int sel = rand.nextInt(inqpc);
								if (0 <= sel && sel < retpc && soldTicket.size() > 0) { // return ticket
									int select = rand.nextInt(soldTicket.size());
									if ((ticket = soldTicket.remove(select)) != null) {
										singleStartTime[threadId] = System.currentTimeMillis();
										if (tds.refundTicket(ticket)) {
											//System.out.println("TicketRefund" + " " + ticket.tid + " " + ticket.passenger + " " + ticket.route + " " + ticket.coach  + " " + ticket.departure + " " + ticket.arrival + " " + ticket.seat);
											//System.out.flush();
										} else {
											//System.out.println("ErrOfRefund");
											//System.out.flush();
										}
										refundExecuteNumber[execute][threadId]++;
										refundUsedTime[execute][threadId] += System.currentTimeMillis() - singleStartTime[threadId];
									} else {
										//System.out.println("ErrOfRefund");
										//System.out.flush();
									}
								} else if (retpc <= sel && sel < buypc) { // buy ticket
									String passenger = passengerName();
									int route = rand.nextInt(routenum) + 1;
									int departure = rand.nextInt(stationnum - 1) + 1;
									int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
									singleStartTime[threadId] = System.currentTimeMillis();
									if ((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null) {
										soldTicket.add(ticket);
										//System.out.println("TicketBought" + " " + ticket.tid + " " + ticket.passenger + " " + ticket.route + " " + ticket.coach + " " + ticket.departure + " " + ticket.arrival + " " + ticket.seat);
										//System.out.flush();
									} else {
										//System.out.println("TicketSoldOut" + " " + route+ " " + departure+ " " + arrival);
										//System.out.flush();
									}
									buyExecuteNumber[execute][threadId]++;
									buyUsedTime[execute][threadId] += System.currentTimeMillis() - singleStartTime[threadId];
								} else if (buypc <= sel && sel < inqpc) { // inquiry ticket
									int route = rand.nextInt(routenum) + 1;
									int departure = rand.nextInt(stationnum - 1) + 1;
									int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
									singleStartTime[threadId] = System.currentTimeMillis();
									int leftTicket = tds.inquiry(route, departure, arrival);
									//System.out.println("RemainTicket" + " " + leftTicket + " " + route+ " " + departure+ " " + arrival);
									//System.out.flush();  
									inquiryExecuteNumber[execute][threadId]++;
									inquiryUsedTime[execute][threadId] += System.currentTimeMillis() - singleStartTime[threadId];														
								}
							}

						}
					});
					threads[i].start();
				}// end for thread
			
				for (int i = 0; i< threadnums[thread]; i++) {
					threads[i].join();
				}
				long totalEndTime = System.currentTimeMillis();
				totalTime[execute] = totalEndTime - totalStartTime;
			}// end for execute count
			double throughput = 0, avgBuyTime = 0, avgRefundTime = 0, avgInquiryTime = 0;
			for(int execute = 0; execute < execution_count; execute++ ){
				int totalCount = 0;
				long totalBuyTime = 0, totalRefundTime = 0, totalInquiryTime = 0, totalUsedTime = 0;
				int totalBuyNumber = 0, totalRefundNumber = 0, totalInquiryNumber = 0;
				for(int i = 0; i < threadnums[thread]; i++){
					totalCount += buyExecuteNumber[execute][i] + refundExecuteNumber[execute][i] + inquiryExecuteNumber[execute][i];
					totalBuyTime += buyUsedTime[execute][i];
					totalRefundTime += refundUsedTime[execute][i];
					totalInquiryTime += inquiryUsedTime[execute][i];
					totalBuyNumber += buyExecuteNumber[execute][i];
					totalRefundNumber += refundExecuteNumber[execute][i];
					totalInquiryNumber += inquiryExecuteNumber[execute][i];
				}
				totalUsedTime = totalTime[execute];
				throughput += totalCount*1000.0 / totalUsedTime;
				avgBuyTime += totalBuyTime*1.0 / totalBuyNumber;
				avgRefundTime += totalRefundTime*1.0 / totalRefundNumber;
				avgInquiryTime += totalInquiryTime*1.0 / totalInquiryNumber;
			}
			// long avgTime = totalTime / execution_count;
			// double throughput = testnum*threadnums[thread]*1000.0 / avgTime;
			// System.out.printf("\ntotal execution count: %d total used time: %dms\n", totalCount, totalUsedTime);
			//double throughput = testnum*threadnums[thread]*execution_count*1000.0 / *totalUsedTime;
			// double throughput = totalCount*1000.0 / totalUsedTime;
			// double avgBuyTime = totalBuyTime*1.0 / (execution_count*totalBuyNumber);
			// double avgRefundTime = totalRefundTime*1.0 / (execution_count*totalRefundNumber);
			// double avgInquiryTime = totalInquiryTime*1.0 / (execution_count*totalInquiryNumber);
			System.out.printf("\n[Thread: %d  avgBuyTime: %.2fms avgRefundTime: %.2fms avgInquiryTime: %.2fms Throughput: %.2f req/s]\n", threadnums[thread], avgBuyTime/execution_count, avgRefundTime/execution_count, avgInquiryTime/execution_count, throughput/execution_count);	
			System.out.println();
		}// end for thread nums
	}
}
