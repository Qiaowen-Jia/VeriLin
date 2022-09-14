package ticketingsystem;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//Thread-safe
public class TicketBase {
	
	ConcurrentHashMap<Long,Ticket> Tickets=new ConcurrentHashMap<>();
	
	public TicketBase()
	{
		this.Initialize();
	}
	
	private void Initialize() {

	}
	
	public int size()
	{
		return Tickets.size();
	}
	
	public boolean containsKey(Long tid)
	{
		return Tickets.containsKey(tid);
	}
	
	public Ticket get(Long tid)
	{
		return Tickets.get(tid);
	}
	
	public Ticket AddTicket(Ticket t)
	{
		return Tickets.putIfAbsent(t.tid, t);
	}
	public Ticket FindTicket(Long select)
	{
		return Tickets.get(select);
	}
	//refund,add a tag
	public boolean RefundTicket(Ticket t)
	{
		return Tickets.remove(t.tid,t);
	}
}
