package ticketingsystem;

class Ticket{
	long tid;
	String passenger;
	int route;
	int coach;
	int seat;
	int departure;
	int arrival;
	
	Ticket(){
		// empty ticket
	}

	Ticket(
		long tidIn, 
		String passengerIn, 
		int routeIn, 
		int coachIn, 
		int seatIn, 
		int departureIn, 
		int arrivalIn
	) {
		tid = tidIn;
		passenger = passengerIn;
		route = routeIn;
		coach = coachIn;
		seat = seatIn;
		departure = departureIn;
		arrival = arrivalIn;
	}
}


public interface TicketingSystem {
	Ticket buyTicket(String passenger, int route, int departure, int arrival);
	int inquiry(int route, int departure, int arrival);
	boolean refundTicket(Ticket ticket);
}
