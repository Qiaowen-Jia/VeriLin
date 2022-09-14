package ticketingsystem;

import java.util.concurrent.atomic.AtomicLong;

//a readwritelock for each seat
public class TicketingDS implements TicketingSystem {
	int routeNum;
	Route[] routes;
	static AtomicLong ticketCount = new AtomicLong(0);
	
	public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
		routeNum = routenum;
		routes = new Route[routeNum];
		for(int i=0;i<routeNum;i++) {
			routes[i] = new Route(coachnum,seatnum,stationnum);
		}
	}
	
	@Override
	public Ticket buyTicket(String passenger, int routeID, int departure, int arrival) {
		if(routeID<1 || routeID>routeNum) return null;
		Route route = routes[routeID-1];
		return route.buyTicket(passenger,routeID,departure,arrival);		
	}

	@Override
	public int inquiry(int routeID, int departure, int arrival) {	
		if(routeID<1 || routeID>routeNum) return 0;
		Route route = routes[routeID-1];
		return route.inquiry(departure,arrival);
	}
	
	@Override
	public boolean refundTicket(Ticket ticket) {
		if(ticket==null) return false;
		Route route = routes[ticket.route-1];
		return route.refundTicket(ticket);		
	}
}
