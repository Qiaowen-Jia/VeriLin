package ticketingsystem;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class TicketingDS implements TicketingSystem {

	//Recode the Last ticketID
	private AtomicLong The_Last_Ticket_ID= new AtomicLong(0);
	//Thread-safe
	private TicketBase ticketbase;
	
	//Route Number ,default is 5
	private int routenum;
	//Coach Number ,default is 8
	private int coachnum;
	//Seat Number in One Coach ,default is 100
	private int seatnum;
	// Default is 10, include start and arrive
	private int stationnum;
	//Sell Windows (thread),default is 16
	private int threadnum;
	
	private Train Trains[];

	
	public int getRouteNum() {
		return routenum;
	}

	public int getCoachNum() {
		return coachnum;
	}

	public int getSeatNum() {
		return seatnum;
	}

	public int getStaionNum() {
		return stationnum;
	}

	public int getThreadNum() {
		return threadnum;
	}
	
	public int getLastIDNum(){
		return (int) The_Last_Ticket_ID.intValue();
	}

	//default
	public TicketingDS() {
		this(5, 10, 100, 10, 16);
	}

	public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
		this.routenum = routenum;
		this.coachnum = coachnum;
		this.seatnum = seatnum;
		this.stationnum = stationnum;
		this.threadnum = threadnum;
		
		this.Initialize();
	}

	//Initialize
	private void Initialize() {
		Trains = new Train[routenum];
        ticketbase = new TicketBase(); 
        int i;
		for (i = 0; i < routenum; i++) {
			Trains[i]=new Train(coachnum,seatnum,stationnum);
		}
	}

	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
		int seat = Trains[route - 1].lockSeat(departure - 1, arrival - 1);
		
		if(seat < 0){
            return null;
        }
		
		Ticket newticket = new Ticket();
		newticket.tid=The_Last_Ticket_ID.getAndIncrement();
		newticket.route=route;
		newticket.departure=departure;
		newticket.arrival=arrival;
		
		newticket.coach = (seat / seatnum ) + 1;
		newticket.seat = (seat % seatnum ) + 1;
		//System.out.println(seat + "," + newticket.coach+ "," + newticket.seat);
		//find seat;add lock
        newticket.passenger=passenger;
		ticketbase.AddTicket(newticket); //Buy ticket
		return newticket;
	}
	

	//inquiry
	public int inquiry(int route, int departure, int arrival) {
		return Trains[route - 1].querySeat(departure - 1, arrival - 1);
	}

	public boolean refundTicket(Ticket ticket) {
		
		if(!ticketbase.Tickets.containsKey(ticket.tid) || !ticket.equals(ticketbase.get(ticket.tid))){
            return false;
		}
            
		int Route = ticket.route-1;
		
		int Coach = ticket.coach-1;
		int Seat = ticket.seat-1;
		
		int Departure = ticket.departure-1;
		int Arrival = ticket.arrival-1;
		
		int SeatNum = Coach*seatnum + Seat;
		
		//System.out.println("REFUND  " + " ID:" + tick.tid+",Route:"+tick.route+",SeatNum:" +SeatNum+ ",Coach:"+tick.coach+",Seat:"+tick.seat+",Departure:"+tick.departure+",Arrival:"+tick.arrival+",Passenger:"+tick.passenger+ ")");
		if(Trains[Route].unlockSeat(SeatNum, Departure, Arrival))
		{
			ticketbase.RefundTicket(ticket);
			//System.out.println("REFUND  " + " (REFUND - ID:" + tick.tid+",Route:"+tick.route+",Coach:"+tick.coach+",Seat:"+tick.seat+",Departure:"+tick.departure+",Arrival:"+tick.arrival+",Passenger:"+tick.passenger+ ")");
			return true;
		}
		else
		{
			return false;
		}
	}
}
