package ticketingsystem;

import java.util.ArrayList;
import java.util.Random;

public class Test {
	final static int[] threadnums = {1, 2, 4, 8, 16, 32, 64, 128};
	final static int routenum = 5;
	final static int coachnum = 8;
	final static int seatnum = 100;
	final static int stationnum = 10;
	final static int testnum = 50000;
	final static int retpc = 10;
	final static int buypc = 40;
	final static int inqpc = 100;

	static String passengerName() {
		Random rand = new Random();
		long uid = rand.nextInt(testnum);
		return "passenger" + uid;
	}

	public static void main(String[] args) throws InterruptedException{
		System.out.println("RouteNum: " + routenum + ", CoachNum: " + coachnum + ", SeatNum: " + seatnum + ", StationNum: " + stationnum + ", op per thread: " + testnum);
		for(int threadnum : threadnums){
			execute(threadnum);
		}
	}

	public static void execute(int threadnum) throws InterruptedException {
		final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
		Thread[] threads = new Thread[threadnum];
		for (int i = 0; i< threadnum; i++) {
			threads[i] = new Thread(new Runnable() {
				public void run() {
					Random rand = new Random();
					Ticket ticket;
					ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();
					for (int i = 0; i < testnum; i++) {
						int sel = rand.nextInt(inqpc);
						if (0 <= sel && sel < retpc && soldTicket.size() > 0) { // return ticket
							int select = rand.nextInt(soldTicket.size());
							if ((ticket = soldTicket.remove(select)) != null) {
								boolean result = tds.refundTicket(ticket);
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
							ticket = tds.buyTicket(passenger, route, departure, arrival);
							if (ticket != null) {
								soldTicket.add(ticket);
							}
						} else if (buypc <= sel && sel < inqpc) { // inquiry ticket
							int route = rand.nextInt(routenum) + 1;
							int departure = rand.nextInt(stationnum - 1) + 1;
							int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
							tds.inquiry(route, departure, arrival);
						}
					}
				}
			});
		}
		long beginTime = System.currentTimeMillis();
		for (int i = 0; i < threadnum; i++) threads[i].start();
		for (int i = 0; i < threadnum; i++) threads[i].join();
		long executionTime = System.currentTimeMillis() - beginTime;
		System.out.println("ThreadNum: " + threadnum + ", Execution Time: " + executionTime + "ms" + ", Throughput: " + (int)((double)(testnum * threadnum) * 1000 / executionTime) + "op/s");
	}
}