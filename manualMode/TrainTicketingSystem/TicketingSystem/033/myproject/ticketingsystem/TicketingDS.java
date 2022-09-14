package ticketingsystem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;



public class TicketingDS implements TicketingSystem {
	int routenum;           //车次数目
	int coachnum;           //一趟车次的车厢数目
	int seatnum;            //一个车厢的座位数目
	int stationnum;         //车站数目
	int threadnum;          //线程数目  
	Train2[] trains;           
	ConcurrentHashMap<Long,Ticket>  soldTickets;
	static ThreadLocal<Long> countId = new ThreadLocal<Long>() {
	    protected Long initialValue() {
	        return (long)0;
	    }
	};
	
	public TicketingDS()
	{
		routenum = 5;
		coachnum = 8;
		seatnum = 100;
		stationnum = 10;
		threadnum = 16;
		trains = new Train2[routenum];
		for(int i = 0 ;i < routenum;i++)
			trains[i] = new Train2(coachnum,seatnum,stationnum,threadnum,i+1);
		soldTickets = new ConcurrentHashMap<Long,Ticket>();
	
	}
	public TicketingDS(int routenum)
	{
		this.routenum = routenum;
		coachnum = 8;
		seatnum = 100;
		stationnum = 10;
		threadnum = 16;
		trains = new Train2[routenum];
		for(int i = 0 ;i < routenum;i++)
			trains[i] = new Train2(coachnum,seatnum,stationnum,threadnum,i+1);
		soldTickets = new ConcurrentHashMap<Long,Ticket>();
	}
	public TicketingDS(int routenum, int coachnum)
	{
		this.routenum = routenum;
		this.coachnum = coachnum;
		seatnum = 100;
		stationnum = 10;
		threadnum = 16;
		trains = new Train2[routenum];
		for(int i = 0 ;i < routenum;i++)
			trains[i] = new Train2(coachnum,seatnum,stationnum,threadnum,i+1);
		soldTickets = new ConcurrentHashMap<Long,Ticket>();
	}
	public TicketingDS(int routenum, int coachnum, int seatnum)
	{
		this.routenum = routenum;
		this.coachnum = coachnum;
		this.seatnum = seatnum;
		stationnum = 10;
		threadnum = 16;
		trains = new Train2[routenum];
		for(int i = 0 ;i < routenum;i++)
			trains[i] = new Train2(coachnum,seatnum,stationnum,threadnum,i+1);
		soldTickets = new ConcurrentHashMap<Long,Ticket>();
	}
	public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum)
	{
		this.routenum = routenum;
		this.coachnum = coachnum;
		this.seatnum = seatnum;
		this.stationnum = stationnum;
		threadnum = 16;
		trains = new Train2[routenum];
		for(int i = 0 ;i < routenum;i++)
			trains[i] = new Train2(coachnum,seatnum,stationnum,threadnum,i+1);
		soldTickets = new ConcurrentHashMap<Long,Ticket>();
	}
	public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) 
	{
		// TODO Auto-generated constructor stub
		this.routenum = routenum;
		this.coachnum = coachnum;
		this.seatnum = seatnum;
		this.stationnum = stationnum;
		this.threadnum = threadnum;
		trains = new Train2[routenum];
		for(int i = 0 ;i < routenum;i++)
			trains[i] = new Train2(coachnum,seatnum,stationnum,threadnum,i+1);
		soldTickets = new ConcurrentHashMap<Long,Ticket>();
	}
	
	@Override
	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
		// TODO Auto-generated method stub
		//debug
		//System.out.println("buy");
		Ticket ticket = trains[route-1].buyticket(passenger, departure, arrival);
		if(ticket!=null)
		{
			soldTickets.put(ticket.tid, ticket);
		}
		return ticket;
	}

	@Override
	public int inquiry(int route, int departure, int arrival) {
		// TODO Auto-generated method stub
		int count = trains[route-1].getValidticketnum(departure,arrival);
		return count;

	}

	@Override
	public boolean refundTicket(Ticket ticket) {
		// TODO Auto-generated method stub
		//debug
		//System.out.println("refund");
		int route = ticket.route;
		int coach = ticket.coach;
		int seat = ticket.seat;
		int departure = ticket.departure;
		int arrival = ticket.arrival;
		long ticId = ticket.tid;
		boolean check_res = checkInvalid(ticket);
		if(check_res && soldTickets.remove(ticId)!=null)
		{
			trains[route-1].refundticket(coach, seat, departure, arrival);
			return true;
		}
		return false;
	}
	public boolean checkInvalid(Ticket ticket)
	{
		Ticket ticInSold = soldTickets.get(ticket.tid);
		return ticInSold.arrival == ticket.arrival &&
				ticInSold.coach == ticket.coach&&
				ticInSold.departure == ticket.departure&&
				ticInSold.passenger.equals(ticket.passenger) &&
				ticInSold.route == ticket.route &&
				ticInSold.seat == ticket.seat;
	}
	//ToDo
}
