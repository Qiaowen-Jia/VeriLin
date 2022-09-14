package ticketingsystem;

import java.util.ArrayList;
import java.util.Random;

public class Test {
	private static int routeNum = 20;
	private static int coachNum = 10;
	private static int seatNum = 100;
	private static int stationNum = 16;
	private static int threadNum = 128;

	final static int testnum = 100000;
	final static int retpc = 10; // return ticket operation is 10% percent
	final static int buypc = 30; // buy ticket operation is 30% percent
	final static int inqpc = 100; //inquiry ticket operation is 60% percent

	static String passengerName() {
		Random rand = new Random();
		long uid = rand.nextInt(testnum);
		return "passenger" + uid;
	}
	public static void main(String[] args) throws InterruptedException {
		Thread[] threads = new Thread[threadNum];

		final TicketingDS tds = new TicketingDS(routeNum, coachNum, seatNum, stationNum, threadNum);

		final long startTime = System.nanoTime();

		for (int i = 0; i< threadNum; i++) {
			threads[i] = new Thread(new Runnable() {
				public void run() {
					Random rand = new Random();
					Ticket ticket = new Ticket();
					ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();

					for (int i = 0; i < testnum; i++) {
						int sel = rand.nextInt(inqpc);
						if (0 <= sel && sel < retpc && soldTicket.size() > 0) { // return ticket
							int select = rand.nextInt(soldTicket.size());
							if ((ticket = soldTicket.remove(select)) != null) {
								//long preTime = System.nanoTime() - startTime;
								if (tds.refundTicket(ticket)) {
									//long postTime = System.nanoTime() - startTime;
									//System.out.println(preTime + " " + postTime + " " + ThreadId.get() + " " + "TicketRefund" + " " + ticket.tid + " " + ticket.passenger + " " + ticket.route + " " + ticket.coach  + " " + ticket.departure + " " + ticket.arrival + " " + ticket.seat);
									//System.out.flush();
								} else {
									//System.out.println(preTime + " " + String.valueOf(System.nanoTime()-startTime) + " " + ThreadId.get() + " " + "ErrOfRefund");
									//System.out.flush();
								}
							} else {
								//long preTime = System.nanoTime() - startTime;
								//System.out.println(preTime + " " + String.valueOf(System.nanoTime()-startTime) + " " + ThreadId.get() + " " + "ErrOfRefund");
								//System.out.flush();
							}
						} else if (retpc <= sel && sel < buypc) { // buy ticket
							String passenger = passengerName();
							int route = rand.nextInt(routeNum) + 1;
							int departure = rand.nextInt(stationNum - 1) + 1;
							int arrival = departure + rand.nextInt(stationNum - departure) + 1; // arrival is always greater than departure
							long preTime = System.nanoTime() - startTime;
							if ((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null) {
								//long postTime = System.nanoTime() - startTime;
								//System.out.println(preTime + " " + postTime + " " + ThreadId.get() + " " + "TicketBought" + " " + ticket.tid + " " + ticket.passenger + " " + ticket.route + " " + ticket.coach + " " + ticket.departure + " " + ticket.arrival + " " + ticket.seat);
								soldTicket.add(ticket);
								//System.out.flush();
							} else {
								//System.out.println(preTime + " " + String.valueOf(System.nanoTime()-startTime) + " " + ThreadId.get() + " " + "TicketSoldOut" + " " + route + " " + departure+ " " + arrival);
								//System.out.flush();
							}
						} else if (buypc <= sel && sel < inqpc) { // inquiry ticket

							int route = rand.nextInt(routeNum) + 1;
							int departure = rand.nextInt(stationNum - 1) + 1;
							int arrival = departure + rand.nextInt(stationNum - departure) + 1; // arrival is always greater than departure
							//long preTime = System.nanoTime() - startTime;
							int leftTicket = tds.inquiry(route, departure, arrival);
							//long postTime = System.nanoTime() - startTime;
							//System.out.println(preTime + " " + postTime + " " + ThreadId.get() + " " + "RemainTicket" + " " + leftTicket + " " + route+ " " + departure+ " " + arrival);
							//System.out.flush();

						}
					}

				}
			});
			threads[i].start();
		}

		for (int i = 0; i< threadNum; i++) {
			threads[i].join();
		}

		final long endTime = System.nanoTime();
		double usetime = (endTime - startTime)*Math.pow(10, -9);
		//System.out.println(tds.inquiry(2,8, 9));
		System.out.println("吞吐量：" + testnum * threadNum / usetime);
	    
	}
}
