package ticketingsystem;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class TicketingDS implements TicketingSystem {

	private final int routeNum;
	private ArrayList<Route> routeList;
	private final int stationNum;
	private AtomicLong tid;

	public TicketingDS(int routeNum, int coachNum,int seatNum, int stationNum, int threadNum) {
		this.routeNum = routeNum;
		this.stationNum = stationNum;
		this.routeList = new ArrayList<Route>(routeNum);
		for (int i = 0; i < this.routeNum; i++) {
			this.routeList.add(new Route(i + 1, coachNum, seatNum));
		}
		this.tid = new AtomicLong(0);
	}

	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
		if (route <= 0 || route > this.routeNum || arrival > this.stationNum || departure >= arrival) {
			return null;
		}
		Ticket ticket = this.routeList.get(route - 1).tryBuyTicket(passenger, departure, arrival);
		if(ticket==null) {
			return null;
		}else{
			ticket.tid = this.tid.getAndIncrement();
			return ticket;
		}
	}

	public int inquiry(int route, int departure, int arrival) {
		if (route <= 0 || route > this.routeNum || arrival > this.stationNum || departure >= arrival) {
			System.out.println("  error  ");
			return -1;
		}
		return this.routeList.get(route - 1).tryInqueryTicket(departure, arrival);
	}

	public boolean refundTicket(Ticket ticket) {
		if (ticket == null)
			return false;
		int routeId = ticket.route;
		if (routeId <= 0 || routeId > this.routeNum)
			return false;
		return this.routeList.get(routeId - 1).tryRefundTicket(ticket);
	}

}
