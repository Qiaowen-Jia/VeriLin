package ticketingsystem;

import java.util.concurrent.atomic.AtomicBoolean;

public class IdMsg
{
	public int route;
	public int coach;
	public int seat;
	public int departure;
	public int arrival;
	public AtomicBoolean bWait = new AtomicBoolean(false);
	public AtomicBoolean bFinish = new AtomicBoolean(false);
	public IdMsg(Ticket ticket)
	{
		route = ticket.route;
		coach = ticket.coach;
		seat = ticket.seat;
		departure = ticket.departure;
		arrival = ticket.arrival;
	}
}
