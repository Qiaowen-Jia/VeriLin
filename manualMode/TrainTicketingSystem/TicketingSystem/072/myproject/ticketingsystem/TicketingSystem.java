package ticketingsystem;

class Ticket {
	long tid;
	String passenger;
	int route;
	int coach;
	int seat;
	int departure;
	int arrival;

	@Override
	public int hashCode() {
		// Effective Java
		int result = 17;

		result = 31 * result + passenger.hashCode();
		result = 31 * result + (int) (tid ^ (tid >>> 32));
		result = 31 * result + route;
		result = 31 * result + coach;
		result = 31 * result + seat;
		result = 31 * result + departure;
		result = 31 * result + arrival;
		return result;
	}
}


public interface TicketingSystem {
	Ticket buyTicket(String passenger, int route, int departure, int arrival);
	int inquiry(int route, int departure, int arrival);
	boolean refundTicket(Ticket ticket);
}
