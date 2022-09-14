package ticketingsystem;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;

public class TicketManager {

	private int route; 
	private int coachnum; 
	private int seatnum; 
	
	private AtomicLong counter;
	
	private AtomicLong[][] seatStates;

	private ConcurrentHashMap<Ticket, Long> soldTickets;

	public TicketManager(int route, int coachnum, int seatnum, int stationnum) {
		this.route = route;
		this.coachnum = coachnum;
		this.seatnum = seatnum;
	
		counter = new AtomicLong(0);
		
		seatStates = new AtomicLong[coachnum][seatnum];
		for (int i=0; i<coachnum; i++) {
			for(int j=0; j<seatnum; ++j) {
				seatStates[i][j] = new AtomicLong(0);
			}
		}

		soldTickets = new ConcurrentHashMap<Ticket, Long>();
	}
	
	public int inquery(int departure, int arrival) {
		long bitInterval = getBitInterval(departure, arrival);
		int cnt = 0;
		for (int i=0; i<coachnum; i++) {
			for(int j=0; j<seatnum; ++j) {
				long seatState = seatStates[i][j].get();
				if((seatState & bitInterval) == 0) {
					cnt ++;
				}
			}
		}
		return cnt;
	}
	
	
	public Ticket bugTicket(String passenger, int departure ,int arrival) {
		long bitInterval = getBitInterval(departure, arrival);
		Ticket ticket = null;
		for (int i=0; i<coachnum; i++) {
			for(int j=0; j<seatnum; ++j) {
				AtomicLong seat = seatStates[i][j]; 
				long seatState = seat.get();
				while((seatState & bitInterval) == 0) {
					if(seat.compareAndSet(seatState, seatState | bitInterval)){
						long tid = generatedTID(); 
						ticket = new Ticket(tid, passenger, route, i + 1, j + 1, departure, arrival);
						soldTickets.put(ticket,  bitInterval);
						return ticket;
					}
					seatState = seat.get();
				}
			}
		}
		return ticket;
	}
	
	public boolean refundTicket(Ticket ticket) {
		if(!soldTickets.containsKey(ticket)){
			return false;
		}
		long bitInterval = getBitInterval(ticket.departure, ticket.arrival);
		long notBitInterval = ~bitInterval;
		AtomicLong seat = seatStates[ticket.coach - 1][ticket.seat - 1];
		long seatState = seat.get();
		while((seatState & bitInterval) == bitInterval) {
			if(seat.compareAndSet(seatState, seatState & notBitInterval)){
				soldTickets.remove(ticket);
				return true;
			}
			seatState = seat.get();
		}
		return false;
	}

	public long generatedTID() {
		long cnt = counter.getAndIncrement();
		return ((long)route)<<48L | (cnt & 0xffffffffffffL);
	}
	
	public long getBitInterval(int departure, int arrival) {
		int nums = arrival - departure;
		return ((1 << nums) - 1) << (departure - 1);
	}
	
}
