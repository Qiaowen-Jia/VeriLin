package ticketingsystem;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;


/**
 * in TicketingSystem, all integer number start at 1.
 * @author Firzen
 *
 */
public final class TicketingDS implements TicketingSystem {
	
	final static int MAXCONCURRENTLEVEL = 64;
	final int routeSum;
	final int stationSum;
	final int threadSum;
	
	public final Train[] trains;
	
	private final ConcurrentHashMap<Ticket, Integer>[][][] soldedTickets;
	
	
	/**
	 * these xxnum means xxSum.
	 * @param routenum
	 * @param coachnum
	 * @param seatnum
	 * @param stationnum
	 * @param threadnum
	 */
	@SuppressWarnings("unchecked")
	public TicketingDS (int routenum, int coachnum, int seatnum, int stationnum, int threadnum){
		
		Train.coachSum = coachnum;
		Coach.seatSum = seatnum;
		
		this.routeSum = routenum;
		this.stationSum = stationnum;
		if(threadnum > MAXCONCURRENTLEVEL) {
			this.threadSum = MAXCONCURRENTLEVEL;
		}
		else {
			this.threadSum = threadnum;
		}
		this.soldedTickets = new ConcurrentHashMap[routenum][coachnum][seatnum];
		
		this.trains = new Train[routenum];
		for (int i = 0; i < routenum; i++) {
			trains[i] = new Train(coachnum, seatnum);
			for(int j = 0; j < coachnum; j++) {
				for(int k = 0; k < seatnum; k++) {
					this.soldedTickets[i][j][k] = new ConcurrentHashMap<Ticket, Integer>(stationnum, 1f, this.threadSum);
				}
			}
		}
	}
	
	/**
	 * 
	 * the uniqueness of tid will be supported for the uniqueness of seats and a random integer number.
	 * 
	 * hope: routes and seats less than 7bits;
	 *       coaches, departure, and arrival less than 6bits;
	 *       passenger hash code will be 32bits;
	 * @param route
	 * @param coach
	 * @param seat
	 * @param departure
	 * @param arrival
	 * @param passenger
	 * @param randomNum
	 * @return the tid.
	 */
	public final static long getTicketTid(int route, int coach, int seat, int departure, int arrival, String passenger, int randomNum) {
		randomNum += passenger.hashCode();
		if(randomNum < 0) {//avoid borrow from higher bits.
			randomNum = -randomNum;
		}
		return ((long)(route-1)<<57)+((long)(coach-1)<<51)+((long)(seat-1)<<44)+((long)(departure-1)<<38)+((long)(arrival-1)<<32)+randomNum;
	}

	/**
	 * verify the input data.
	 * @param route
	 * @param departure
	 * @param arrival
	 * @return true means data is right, false means wrong data.
	 */
	private final boolean inputVerify(int route, int departure, int arrival) {
		if (route < 1 || route > routeSum || departure < 1 || departure >= arrival || arrival > stationSum) {
			return false;
		}
		else {
			return true;
		}
	}
	
	/**
	 * buy ticket.
	 */
	public final Ticket buyTicket(String passenger, int route, int departure, int arrival) {
		if(!inputVerify(route, departure, arrival)) {
			return null;
		}
		if(passenger == null) {
			return null;
		}
		Ticket ticket = trains[route - 1].getOneEmptySeat(departure, arrival);
		if(ticket == null) {
			return null;
		}
		else {
			/**
			 * the uniqueness of tid will be supported for the uniqueness of seats.
			 * hope: routes and seats less than 7bits;
			 *       coaches, departure, and arrival less than 6bits;
			 *       passenger hash code will be 32bits;
			 *       
			 */
			int randomNum = ThreadLocalRandom.current().nextInt();//get a random integer number to generate a tid.

			ticket.tid = getTicketTid(route, ticket.coach, ticket.seat, departure, arrival, passenger, randomNum);
			ticket.route = route;
			ticket.passenger = passenger;
			
			if(soldedTickets[route - 1][ticket.coach - 1][ticket.seat - 1].putIfAbsent(ticket, (Integer)randomNum) == null) {//if ticket not exist before.
				return ticket;
			}
			else {
				return null;
			}
		}
	}

	/**
	 * query tickets.
	 */
	public final int inquiry(int route, int departure, int arrival) {
		if(!inputVerify(route, departure, arrival)) {
			return 0;
		}
		return trains[route - 1].queryEmptySeats(departure, arrival);
	}

	/**
	 * refund tickets. once the ticket information has been modified, it will be invalid.
	 * @param ticket 
	 * @return false if the ticket not exist or has been modified, 
	 */
	public final boolean refundTicket(Ticket ticket) {
		if(ticket == null) {
			return false;
		}
		Integer preRandomNum;
		if((preRandomNum = soldedTickets[ticket.route - 1][ticket.coach - 1][ticket.seat - 1].remove(ticket)) != null){//if the ticket exists, get it out.
			if(ticket.tid == getTicketTid(ticket.route, ticket.coach, ticket.seat, ticket.departure, ticket.arrival, ticket.passenger, preRandomNum)){//and if the ticket is real.
				
				//want a new thread to reset the seat.
				trains[ticket.route - 1].refundOneSeat(ticket);
				return true;
			}
		}
		return false;
	}
}
