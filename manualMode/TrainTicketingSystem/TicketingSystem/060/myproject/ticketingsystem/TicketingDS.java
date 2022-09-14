package ticketingsystem;

import java.util.concurrent.ConcurrentHashMap;

public class TicketingDS implements TicketingSystem {

	private TrainResources trainResources;
	private CounterNet counterNet;
	private ConcurrentHashMap<Integer,Ticket> ticketInfoTable;
	private int routeNum;
	private int coachNum;
	private int seatNum;
	private int stationNum;
	private int threadNum;

	public TicketingDS(int routenum , int coachnum , int seatnum , int stationnum , int threadnum )
	{
		trainResources = new TrainResources(routenum,coachnum,seatnum,stationnum);
		counterNet = new CounterNet(threadnum);
		ticketInfoTable = new ConcurrentHashMap<Integer,Ticket>();
		routeNum = routenum;
		coachNum = coachnum;
		seatNum = seatnum;
		stationNum = stationnum;
		threadNum = threadnum;
	}
	public Ticket buyTicket(String passenger, int route, int departure, int arrival)
	{
		Ticket res = null;
		SeatInfo seatInfo = trainResources.book(route, departure, arrival);
		if(seatInfo != null)
		{
			res = new Ticket();
			Long tid = counterNet.getAndStep();
			res.tid = tid;
			res.passenger = passenger;
			res.route = seatInfo.train;
			res.arrival = seatInfo.stop;
			res.departure = seatInfo.start;
			res.seat = seatInfo.seat;
			res.coach = seatInfo.carriage;
			ticketInfoTable.put(tid.hashCode(),res);
		}
		return res;
	}
	public int inquiry(int route, int departure, int arrival)
	{
		return trainResources.inquiry(route,departure,arrival);
	}
	public boolean refundTicket(Ticket ticket)
	{
		Long tid = ticket.tid;
		boolean flag;
		flag = ticketInfoTable.containsKey(tid.hashCode());
		if(flag)
		{	Ticket savedTicket = ticketInfoTable.get(tid.hashCode());
			if(savedTicket == ticket)
			{
				ticketInfoTable.remove(tid.hashCode());
				trainResources.cancel(ticket.route, ticket.coach, ticket.seat, ticket.departure, ticket.arrival);
			}
			else
			{
				return false;
			}
		}
		return flag;
	}

}
