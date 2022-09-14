package ticketingsystem;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Test {

	//benchmark
	private static final int routenum = 20;
	private static final int coachnum = 10;
	private static final int seatnum = 100;
	private static final int stationnum = 16;
	private static final int threadnum = 64;

	private static final long testNum = 100000;
	private static final int inquiryNum = 60; //inquiry ticket operation is 60% percent, 60
	private static final int buyTicketNum = 90; // buy ticket operation is 30% percent, 30 = 90 - 60
	private static final int refundTicketNum = 100; // return ticket operation is 10% percent, 10 = 100 - 90

	private static String passengerName(){
		Random random = new Random();
		long uid = random.nextLong() % testNum + 1;
		return "passenger" + uid;
	}

	public static void main(String[] args) throws InterruptedException {
		final int[] threadNum = {4, 8, 16, 32, 64};
		for(int i = 0; i < threadNum.length; i++){
			AtomicLong inquiryTime = new AtomicLong(0);
			AtomicLong inquiryCount = new AtomicLong(0);
			AtomicLong buyTicketTime = new AtomicLong(0);
			AtomicLong buyTicketCount = new AtomicLong(0);
			AtomicLong refundTicketTime = new AtomicLong(0);
			AtomicLong refundTicketCount = new AtomicLong(0);

			final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
			Thread[] threads = new Thread[threadNum[i]];
			for(int j = 0; j < threadNum[i]; j++){
				threads[j] = new Thread(new Runnable() {
					@Override
					public void run() {
						Random random = new Random();
						Ticket ticket = null;
						ArrayList<Ticket> soldTicket = new ArrayList<Ticket>(); //记录已售的票，方便退票
						for(int i = 0; i < testNum; i++){
							int choice = random.nextInt(refundTicketNum);
							if(choice < inquiryNum){ //inquiry
								int route = random.nextInt(routenum) + 1; //[1...21)
								int departure = random.nextInt(stationnum -1) + 1; //[1...16]
								int arrival = departure + random.nextInt(stationnum - departure) + 1;
								long startTime = System.currentTimeMillis();
								tds.inquiry(route, departure, arrival);
								long endTime = System.currentTimeMillis();
								inquiryTime.addAndGet(endTime - startTime);
								inquiryCount.getAndIncrement();
							}
							else if(choice < buyTicketNum){ //buy
								String passenger = passengerName();
								int route = random.nextInt(routenum) + 1; //[1...21)
								int departure = random.nextInt(stationnum -1) + 1; //[1...16]
								int arrival = departure + random.nextInt(stationnum - departure) + 1;
								long startTime = System.currentTimeMillis();
								if((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null){
									soldTicket.add(ticket);
								}
								long endTime = System.currentTimeMillis();
								buyTicketTime.addAndGet(endTime - startTime);
								buyTicketCount.getAndIncrement();
							}
							else { //refund
								if(soldTicket.size() > 0){
									int select = random.nextInt(soldTicket.size());
									long startTime = System.currentTimeMillis();
									if((ticket = soldTicket.remove(select)) != null){
										tds.refundTicket(ticket);
									}
									long endTime = System.currentTimeMillis();
									refundTicketTime.addAndGet(endTime - startTime);
									refundTicketCount.getAndIncrement();
								}
							}
						}
					}
				});
				threads[j].start();
			}
			for(int j = 0; j < threadNum[i]; j++){
				threads[j].join();
			}

			long throughPut = (long)(inquiryCount.get() * 1000.0 / inquiryTime.get() + buyTicketCount.get() * 1000.0 / buyTicketTime.get() + refundTicketCount.get() * 1000.0 / refundTicketTime.get());
			System.out.println("ThreadNumber: " + threadNum[i] + " ThroughPut = " + throughPut);
		}
	}
}
