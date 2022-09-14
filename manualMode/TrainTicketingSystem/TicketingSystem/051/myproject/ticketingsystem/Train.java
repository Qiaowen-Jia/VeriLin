package ticketingsystem;


/**
 * the train keeps the seats and coaches, identified by the routenum.
 * in trains, the integer numbers change the starting point between 1 and 0.
 * 
 * @author Firzen
 * 
 */
public final class Train {
	
	private final static int LOOP_SIZE = 8;
	
	public static int coachSum;
	
	public final Coach[] coaches;
	
	public Train(int coachSum, int seatSum) {
		
		this.coaches = new Coach[coachSum];
		for(int i = 0; i < coachSum; i++) {
			coaches[i] = new Coach(seatSum);
		}
	}
	
	/**
	 * this is a serial version for a train to get the empty seats number.
	 * @param departure
	 * @param arrival
	 * @return the sum of empty seats from all coaches
	 */
	public final int queryEmptySeats(int departure, int arrival) {
		
		int emptySeatSum = 0;
		int reminder = coachSum % LOOP_SIZE;//loop_size = 8.
		
		for(int i = 0; i < coachSum - reminder; i += LOOP_SIZE) {
			emptySeatSum += 
			      (coaches[i].sumAllEmptySeats(departure - 1, arrival - 1)
			 + coaches[i + 1].sumAllEmptySeats(departure - 1, arrival - 1)
			 + coaches[i + 2].sumAllEmptySeats(departure - 1, arrival - 1)
			 + coaches[i + 3].sumAllEmptySeats(departure - 1, arrival - 1)
			 + coaches[i + 4].sumAllEmptySeats(departure - 1, arrival - 1)
			 + coaches[i + 5].sumAllEmptySeats(departure - 1, arrival - 1)
			 + coaches[i + 6].sumAllEmptySeats(departure - 1, arrival - 1)
			 + coaches[i + 7].sumAllEmptySeats(departure - 1, arrival - 1));
		} 
		
		for (int i = coachSum - reminder; i < coachSum; i++) {
			emptySeatSum += coaches[i].sumAllEmptySeats(departure - 1, arrival - 1);
		}
		return emptySeatSum;
	}
	
	/**
	 * this is a serial version for a train to get a seat.
	 * @param departure
	 * @param arrival
	 * @return a ticket without tid and passenger. null means no empty seats.
	 */
	public final Ticket getOneEmptySeat(int departure, int arrival) {

		Ticket halfTicket;//half ticket has no tid, routeNum, and passenger.
		int seatNum = -1;
		
		for(int i = 0; i < coachSum; i++) {
			seatNum = coaches[i].getSeat(departure - 1, arrival - 1);//search and get seat at coach[i].
			if(seatNum == -1) {//if this coach have no empty seats.
				continue;
			}
			else {
				halfTicket = new Ticket();
				halfTicket.departure = departure;
				halfTicket.arrival = arrival;
				halfTicket.coach = i + 1;//add 1 because the number in system start at 0.
				halfTicket.seat = seatNum + 1;
				return halfTicket;
			}
		}
		return null;
	}
	
	/**
	 * the tickets must be managed in ticketing system.
	 * @param ticket
	 * @return nothing.
	 */
	public final void refundOneSeat(Ticket ticket) {
		coaches[ticket.coach - 1].resetSeat(ticket.seat - 1, ticket.departure - 1, ticket.arrival - 1);
	}
}
