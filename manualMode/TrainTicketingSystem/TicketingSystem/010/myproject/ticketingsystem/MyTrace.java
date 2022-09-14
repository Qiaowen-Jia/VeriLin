package ticketingsystem;

import java.util.ArrayList;
import java.util.Random;

public class MyTrace {
	private final static int threadNum = 4; // concurrent thread number
	private final static int routeNum = 3; // route is designed from 1 to 3
	private final static int coachNum = 3; // coach is arranged from 1 to 5
	private final static int seatNum = 3; // seat is allocated from 1 to 20
	private final static int stationNum = 3; // station is designed from 1 to 5

	private final static int testNum = 300;
	private final static int refundPercent = 30; // return ticket operation is 10% percent
	private final static int buyPercent = 60; // buy ticket operation is 30% percent
	private final static int inquiryPercent = 100; //inquiry ticket operation is 60% percent


	
	private static String passengerName() {
		Random rand = new Random();
		long uid = rand.nextInt(testNum);
		return "passenger" + uid; 
	}

	public static void main(String[] args) throws InterruptedException {

		System.out.println("Ticketing System SingleThreadTest Start!");
		System.out.println("threadNum: " + threadNum + " routeNum: " + routeNum
						+ " stationNum: "+ " coachNum: " + coachNum
						+ " seatNum: " + seatNum + "\ntestNum: " + testNum
				+ " refundPercent: " + refundPercent + " buyPercent: "
				+ buyPercent + " inquiryPercent: " + inquiryPercent

		);

		Thread[] threads = new Thread[threadNum];
		
		final TicketingDS tds = new TicketingDS(routeNum, coachNum, seatNum, stationNum, threadNum);
		

	    for (int i = 0; i< threadNum; i++) {
	    	threads[i] = new Thread(() -> {
				Random rand = new Random();
				Ticket ticket;
				ArrayList<Ticket> soldTicket = new ArrayList<>();

				//System.out.println(MyThreadId.get());
				for (int i1 = 0; i1 < testNum; i1++) {
					int sel = rand.nextInt(inquiryPercent);
					if (0 <= sel && sel < refundPercent && soldTicket.size() > 0) { // return ticket
						int select = rand.nextInt(soldTicket.size());
					   if ((ticket = soldTicket.remove(select)) != null) {
							if (tds.refundTicket(ticket)) {
								if (printRouteInfo(ticket.route)) {
									printlnRefundInfo(ticket);
								}

							} else {
								System.out.println("ErrOfRefund");
								System.out.flush();
							}
						} else {
						   System.out.println("ErrOfRefund");
						   System.out.flush();
						}
					} else if (refundPercent <= sel && sel < buyPercent) { // buy ticket
						String passenger = passengerName();
						int route = rand.nextInt(routeNum) + 1;
						int departure = rand.nextInt(stationNum - 1) + 1;
						int arrival = departure + rand.nextInt(stationNum - departure) + 1; // arrival is always greater than departure
						if ((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null) {
							soldTicket.add(ticket);
							if (printRouteInfo(ticket.route)) {
								printlnBoughtInfo(ticket);
							}

						} else {
						    if (printRouteInfo(route)) {
								System.out.println("<SoldOut>" + ", route: " + route+ ", departure: " + departure+ ", arrival:" + arrival);
								System.out.flush();
                            }

						}
					} else if (buyPercent <= sel && sel < inquiryPercent) { // inquiry ticket

						int route = rand.nextInt(routeNum) + 1;
						int departure = rand.nextInt(stationNum - 1) + 1;
						int arrival = departure + rand.nextInt(stationNum - departure) + 1; // arrival is always greater than departure
						int leftTicket = tds.inquiry(route, departure, arrival);
						if (printRouteInfo(route)) {
							System.out.println("<Remain>" + " Number: " + leftTicket + ", route: " + route+ ", departure: " + departure+ ", arrival: " + arrival);
							System.out.flush();

						}

					}
				}

			});
              threads[i].start();
 	    }
	
	    for (int i = 0; i< threadNum; i++) {
	    	threads[i].join();
	    }		
	}

	private static void printlnBoughtInfo(Ticket ticket) {
		System.out.println("<Bought>" + " tid: " + ticket.tid + ", passenger: " + ticket.passenger + ", route: " + ticket.route + ", coach: " + ticket.coach + ", departure: " + ticket.departure + ", arrival: " + ticket.arrival + ", seat: " + ticket.seat);
		System.out.flush();
	}

	private static void printlnRefundInfo(Ticket ticket) {
		System.out.println("<Refund>" + " tid: " + ticket.tid + ", passenger: " + ticket.passenger + ", route: " + ticket.route + ", coach: " + ticket.coach + ", departure: " + ticket.departure + ", arrival: " + ticket.arrival + ", seat: " + ticket.seat);
		System.out.flush();
	}

	private static boolean printRouteInfo(int route) {
		return route == 3;
	}
}
