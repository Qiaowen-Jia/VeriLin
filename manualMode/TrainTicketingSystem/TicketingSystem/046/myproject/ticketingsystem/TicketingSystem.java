package ticketingsystem;

class Ticket {
	long tid; 
	String passenger;
	int route;
	int coach;
	int seat;
	int departure;
	int arrival;
	
	public Ticket(){}

	public Ticket(long tid, String passenger, int route, int coach, int seat, int departure, int arrival) {
		this.tid = tid;
		this.passenger = passenger;
		this.route = route;
		this.coach = coach;
		this.seat = seat;
		this.departure = departure;
		this.arrival = arrival;
	}

	@Override
	public String toString() {
		return "Ticket [tid=0x" + Long.toHexString(tid) + ", passenger=" + passenger + ", route=" + route + ", coach=" + coach + ", seat="
				+ seat + ", departure=" + departure + ", arrival=" + arrival + "]";
	}
	
	@Override
	public boolean equals(Object o) {
		Ticket ticket = (Ticket)o;
		return tid == ticket.tid && passenger.equals(ticket.passenger) && route == ticket.route && coach == ticket.coach && seat == ticket.seat && departure == ticket.departure && arrival == ticket.arrival;

	}
}

public interface TicketingSystem {
	
	Ticket buyTicket(String passenger ,int route ,int departure ,int arrival);
	

	int inquiry(int route ,int departure ,int arrival);
	
	
	boolean refundTicket(Ticket ticket) ;
}
