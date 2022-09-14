package ticketingsystem;

class Ticket{
	long tid;
	String passenger;
	int route;
	int coach;
	int seat;
	int departure;
	int arrival;

	public Ticket(){}

	public Ticket(String passenger, int route, int departure, int arrival, int coach,int seat, long tid)
	{
		this.passenger = passenger;
		this.route = route;
		this.departure = departure;
		this.arrival = arrival;
		this.coach = coach;
		this.seat = seat;
		this.tid = tid;
	}

	public int myhashCode()
	{
		int hash;
		hash = (int)(this.tid % 10000000);
		return hash;
	}

	public boolean hashEquals(Ticket tick)
	{
		return this.myhashCode() == tick.myhashCode();
	}

	public boolean tickEquals(Ticket tick)
	{
		boolean flg = true;
		flg &= (this.tid == tick.tid);
		flg &= (this.route == tick.route);
		flg &= (this.coach == tick.coach);
		flg &= (this.seat == tick.seat);
		flg &= (this.departure == tick.departure);
		flg &= (this.arrival == tick.arrival);
		flg &= (this.passenger.equals(tick.passenger));
		return flg;
	}
}


public interface TicketingSystem {
	Ticket buyTicket(String passenger, int route, int departure, int arrival);
	int inquiry(int route, int departure, int arrival);
	boolean refundTicket(Ticket ticket);
}
