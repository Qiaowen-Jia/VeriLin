package ticketingsystem;


public class TicketingDS implements TicketingSystem {

	final int routenum;
	final int coachnum;
	final int seatnum;
	final int stationnum;
	final int threadnum;

	RouteWithInterval[] routes;

	public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum){
		this.routenum = routenum;
		this.coachnum = coachnum;
		this.seatnum = seatnum;
		this.stationnum = stationnum;
		this.threadnum = threadnum;

		routes = new RouteWithInterval[routenum];
		for (int i = 0; i < routes.length; i ++) {
			routes[i] = new RouteWithInterval(i+1, seatnum, coachnum, stationnum);
		}
		
	}


	@Override
	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
		
			return this.routes[route-1].buyTicketInterval(passenger, departure, arrival);
		
	}

	@Override
	public int inquiry(int route, int departure, int arrival) {
	
			return this.routes[route-1].inquiryTicketInterval(departure, arrival);

	}

	@Override
	public boolean refundTicket(Ticket ticket) {
			return this.routes[ticket.route-1].refundTicketInterval(ticket);
	}
		

}
