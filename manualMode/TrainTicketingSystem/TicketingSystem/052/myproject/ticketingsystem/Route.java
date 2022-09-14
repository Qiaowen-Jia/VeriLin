package ticketingsystem;
import java.util.concurrent.ConcurrentHashMap;

public class Route {
	
	int coachNum;
	int seatNum;
	int stationNum;
	Seat[][] seats;
	ConcurrentHashMap<Long,Ticket> ticketSet = new ConcurrentHashMap<Long, Ticket>();

	Route(int coachnum, int seatnum, int stationnum) {
		this.coachNum = coachnum;
		this.seatNum = seatnum;
		seats = new Seat[coachNum][seatNum];
		this.stationNum = stationnum;
		for(int i=0;i<coachNum;i++) {
			for(int j=0;j<seatNum;j++) {
				seats[i][j] = new Seat(stationnum);
			}
		}
	}
	
	Ticket buyTicket(String passenger, int routID, int departure, int arrival) {
		if(departure<1 || departure>stationNum-1 || 
				arrival<2 || arrival>stationNum || departure>=arrival) return null;	
		boolean isEmpty = false;
		for(int i=0;i<coachNum;i++) {
			for(int j=0;j<seatNum;j++) {
				Seat seat = seats[i][j];
				isEmpty = seat.canWrite(departure,arrival);
				if(isEmpty) {
					Ticket ticket = new Ticket();
					ticket.tid = TicketingDS.ticketCount.getAndIncrement();
					ticket.seat = j+1;
					ticket.route = routID;
					ticket.passenger = passenger;
					ticket.departure = departure;
					ticket.arrival = arrival;
					ticket.coach = i+1;
					ticketSet.put(ticket.tid, ticket);
					return ticket;
				}
			}
		}
		return null;
	}

	boolean refundTicket(Ticket ticket) {
		Ticket ticketInSet = ticketSet.get(ticket.tid);
		if(ticketInSet==null ||!ticketInSet.passenger.equals(ticket.passenger) || 
					ticketInSet.route!=ticket.route || 
					ticketInSet.coach!=ticket.coach || 
					ticketInSet.departure!=ticket.departure||
					ticketInSet.arrival!=ticket.arrival ||
					ticketInSet.seat!=ticket.seat) {
			return false;
		}
		
		if(ticketSet.remove(ticket.tid)!=null) {
			Seat seat = seats[ticket.coach-1][ticket.seat-1];
			return seat.releaseSeat(ticket.departure,ticket.arrival);
		}
		
		return false;
	}

	int inquiry(int departure, int arrival) {
		if(departure<1 || departure>stationNum-1 || 
				arrival<2 || arrival>stationNum || departure>=arrival) return 0;
		int emptySeat = 0;
		boolean isEmpty = false;;
		for(int i=0;i<coachNum;i++) {
			for(int j=0;j<seatNum;j++) {
				Seat seat = seats[i][j];
				isEmpty = seat.canRead(departure,arrival);
				if(isEmpty) emptySeat++;
			}
		}
		return emptySeat;
	}
}
