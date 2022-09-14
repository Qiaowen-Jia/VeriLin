package ticketingsystem;

public class Test {
	final static int threadnum = 4;
	final static int routenum = 3; // route is designed from 1 to 3
	final static int coachnum = 5; // coach is arranged from 1 to 5
	final static int seatnum = 10; // seat is allocated from 1 to 20
	final static int stationnum = 8; // station is designed from 1 to 5

	final static int testnum = 1000;
	final static int retpc = 30; // return ticket operation is 10% percent
	final static int buypc = 60; // buy ticket operation is 30% percent
	final static int inqpc = 100; //inquiry ticket operation is 60% percent
	public static void main(String[] args) throws InterruptedException {
		final TicketingSystem tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
		tds.buyTicket("passenger", 1, 1, 2);
	}
}
