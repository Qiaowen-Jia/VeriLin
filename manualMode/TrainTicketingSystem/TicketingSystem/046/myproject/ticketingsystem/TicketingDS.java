package ticketingsystem;

public class TicketingDS implements TicketingSystem {

	public static final int DEFAULT_ROUTENUM = 5;
	public static final int DEFAULT_COACHNUM = 8;
	public static final int DEFAULT_SEATNUM = 100;
	public static final int DEFAULT_STATIONNUM = 10;
	public static final int DEFUALT_THREADNUM = 16;
	
	private int routenum; 
	private int coachnum; 
	private int seatnum; 
	private int stationnum; 
	private int threadnum; 
	
	private final TicketManager[] ticketManagers;	
	
	public TicketingDS() {
		this(DEFAULT_ROUTENUM, DEFAULT_COACHNUM, DEFAULT_SEATNUM, DEFAULT_STATIONNUM);
	}
	
	public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
		this(routenum, coachnum, seatnum, stationnum);
	}

	public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum) {
		this.routenum = routenum;
		this.coachnum = coachnum;
		this.seatnum = seatnum;
		this.stationnum = stationnum;

		ticketManagers = new TicketManager[routenum];
		for(int i=0; i<routenum; ++i) {
			ticketManagers[i] = new TicketManager(i+1, coachnum, seatnum, stationnum);
		}
	}

	@Override
	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
		if(!isValid(route, departure, arrival)) {
			return null;
		}
		
		return ticketManagers[route-1].bugTicket(passenger, departure, arrival);
	}

	@Override
	public int inquiry(int route, int departure, int arrival) {
		if(!isValid(route, departure, arrival)) {
			return 0;
		}
		
		return ticketManagers[route-1].inquery(departure, arrival);
	}

	@Override
	public boolean refundTicket(Ticket ticket) {
		if(!isValid(ticket)){
			return false;
		}
		return ticketManagers[ticket.route -1].refundTicket(ticket);
	}

	public boolean isValid(int route, int departure, int arrival){
		if(route <=0 || route > routenum){
			return false;
		}
		if(departure <=0 || departure > stationnum){
			return false;
		}
		if(arrival <=0 || arrival > stationnum) {
			return false;
		}
		if(departure >= arrival){
			return false;
		}
		return true;
	}

	public boolean isValid(int route, int departure, int arrival, int coach, int seat){
		if(isValid(route, departure, arrival)){
			if(coach <=0 || coach > coachnum){
				return false;
			}
			if(seat <=0 || seat > seatnum){
				return false;
			}
			return true;
		}
		return false;
	}

	public boolean isValid(Ticket ticket){
		if(ticket == null){
			return false;
		}
		return isValid(ticket.route, ticket.departure, ticket.arrival, ticket.coach, ticket.seat);
	}

	public int getRoutenum() {
		return routenum;
	}

	public int getCoachnum() {
		return coachnum;
	}

	public int getSeatnum() {
		return seatnum;
	}

	public int getStationnum() {
		return stationnum;
	}

	
	public int getThreadnum() {
		return threadnum;
	}

	public TicketManager[] getTicketManagers() {
		return ticketManagers;
	}

}
