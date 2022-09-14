package ticketingsystem;

import java.util.ArrayList;
import java.util.Random;




public class Test2 {

	static String passengerName() {
		int testnum = 4000;
		Random rand = new Random();
		long uid = rand.nextInt(testnum);
		return "passenger" + uid;
	}

	public static void main(String[] args) throws InterruptedException {
		int routenum, coachnum, seatnum, stationnum, threadnum;
		Random rand = new Random();
		routenum = 4;
		coachnum = 5;
		seatnum = 50;
		stationnum = 8;
		threadnum = 10;


		int departure = 3;
		int arrival = stationnum - 1;
		final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
		Thread[] threads = new Thread[threadnum];
		ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();
		for (int i = 0; i < threadnum; i++) {
			threads[i] = new Thread(new Runnable() {
				public void run() {
					int route = 1;
					int departure = 3;
					int arrival = stationnum - 1;
					for (int j = 0; j < 20; j++) {
						Ticket ticket = new Ticket();
						String passenger = passengerName();
						if ((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null) {
							soldTicket.add(ticket);
							System.out.println("TicketBought" + " " + ticket.tid + " " + ticket.passenger + " " + ticket.route + " " + ticket.coach + " " + ticket.departure + " " + ticket.arrival + " " + ticket.seat);
							System.out.flush();

						} else {
							System.out.println("TicketSoldOut" + " " + route + " " + departure + " " + arrival);
							System.out.flush();
						}
					}
					route = 1;
					departure = 3;
					arrival = stationnum - 2;
					for (int j = 0; j < 4; j++) {
						Ticket ticket = new Ticket();
						String passenger = passengerName();
						if ((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null) {
							System.out.println("TicketBought" + " " + ticket.tid + " " + ticket.passenger + " " + ticket.route + " " + ticket.coach + " " + ticket.departure + " " + ticket.arrival + " " + ticket.seat);
							System.out.flush();

						} else {
							System.out.println("TicketSoldOut" + " " + route + " " + departure + " " + arrival);
							System.out.flush();
						}
					}
//					int leftTicket = tds.inquiry(route, departure, arrival);
//					System.out.println("RemainTicket" + " " + leftTicket + " " + route + " " + departure + " " + arrival);
//					System.out.flush();
				}
			});
			threads[i].start();
		}

		for (int i = 0; i < threadnum; i++) {
			threads[i].join();
		}
		int route = 1;
		int leftTicket = tds.inquiry(route, departure, arrival);
		System.out.println("RemainTicket" + " " + leftTicket + " " + route + " " + departure + " " + arrival);
		System.out.flush();
		route = 2;
		departure = stationnum - 1;
		arrival = stationnum;

//		int select = rand.nextInt(tds.tickets_sold.size());
//		Ticket ticket = new Ticket();
//		if ((ticket = tds.tickets_sold.get(select)) != null) {
//			if (tds.refundTicket(ticket)) {
//				System.out.println("TicketRefund" + " " + ticket.tid + " " + ticket.passenger + " " + ticket.route + " " + ticket.coach + " " + ticket.departure + " " + ticket.arrival + " " + ticket.seat);
//				System.out.flush();
//			} else {
//				System.out.println("ErrOfRefund");
//				System.out.flush();
//			}
//
//			//ToDo
//
//		}
		Ticket ticket = new Ticket();
		ticket.tid = soldTicket.get(3).tid;
//		ticket.passenger = "123";
//		ticket.route = 1;
//		ticket.seat = 3;
//		ticket.arrival = 1;
//		ticket.departure = 6;
//		ticket.coach = 3;
		System.out.println(ticket.route);
		if (tds.refundTicket(ticket)) {
			System.out.println("TicketRefund" + " " + ticket.tid + " " + ticket.passenger + " " + ticket.route + " " + ticket.coach + " " + ticket.departure + " " + ticket.arrival + " " + ticket.seat);
			System.out.flush();
		} else {
			System.out.println("ErrOfRefund");
			System.out.flush();
		}
		if ((ticket = tds.buyTicket("passenger", 1, 3, 3)) != null) {
			System.out.println("TicketBought" + " " + ticket.tid + " " + ticket.passenger + " " + ticket.route + " " + ticket.coach + " " + ticket.departure + " " + ticket.arrival + " " + ticket.seat);
			System.out.flush();

		} else {
			System.out.println("TicketSoldOut" + " " + route + " " + departure + " " + arrival);
			System.out.flush();
		}


//		leftTicket = tds.inquiry(route, departure, arrival);
//		System.out.println("RemainTicket" + " " + leftTicket + " " + route + " " + departure + " " + arrival);
//		System.out.flush();
		route =1;
		departure = 3;
		arrival = 8;
		leftTicket = tds.inquiry(route, departure, arrival);
		System.out.println("RemainTicket" + " " + leftTicket + " " + route + " " + departure + " " + arrival);
		System.out.flush();

		route =1;
		departure = 6;
		arrival = 7;
		leftTicket = tds.inquiry(route, departure, arrival);
		System.out.println("RemainTicket" + " " + leftTicket + " " + route + " " + departure + " " + arrival);
		System.out.flush();

		route =1;
		departure = 7;
		arrival = 8;
		leftTicket = tds.inquiry(route, departure, arrival);
		System.out.println("RemainTicket" + " " + leftTicket + " " + route + " " + departure + " " + arrival);
		System.out.flush();

		route =1;
		departure = 6;
		arrival = 8;
		leftTicket = tds.inquiry(route, departure, arrival);
		System.out.println("RemainTicket" + " " + leftTicket + " " + route + " " + departure + " " + arrival);
		System.out.flush();

		route =1;
		departure = 1;
		arrival = 8;
		leftTicket = tds.inquiry(route, departure, arrival);
		System.out.println("RemainTicket" + " " + leftTicket + " " + route + " " + departure + " " + arrival);
		System.out.flush();

		route =1;
		departure = 1;
		arrival = 2;
		leftTicket = tds.inquiry(route, departure, arrival);
		System.out.println("RemainTicket" + " " + leftTicket + " " + route + " " + departure + " " + arrival);
		System.out.flush();


	}
}