package ticketingsystem;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class Route {

	private final int routeId;
	private final int coachNum;
	private ArrayList<Coach> coachList;

	public Route(final int routeId, final int coachNum, final int seatNum) {
		this.routeId = routeId;
		this.coachNum = coachNum;
		this.coachList = new ArrayList<Coach>(this.coachNum);
		for (int i = 0; i < this.coachNum; i++) {
			this.coachList.add(new Coach(i + 1, seatNum));
		}
	}

	public Ticket tryBuyTicket(final String passenger, final int departure, final int arrival) {
		for (int i = 0; i < this.coachNum; i++) {
			Ticket ticket = this.coachList.get(i).tryBuyTicket(departure, arrival);
			if (ticket != null) {
				ticket.passenger = passenger;
				ticket.arrival = arrival;
				ticket.departure = departure;
				ticket.route = this.routeId;
				return ticket;
			} 
		}
		return null;
	}

	public int tryInqueryTicket(final int departure, final int arrival) {
		int result = 0;
		for (int i = 0; i < this.coachNum; i++) {
			result += this.coachList.get(i).tryInqueryTicket(departure, arrival);
		}

		return result;
	}
	
	public boolean tryRefundTicket(Ticket ticket) {
		return this.coachList.get(ticket.coach-1).tryRefundTicket(ticket.seat, ticket.departure, ticket.arrival);
	}

}
