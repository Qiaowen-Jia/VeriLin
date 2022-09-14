package ticketingsystem;

public class TicketingDS implements TicketingSystem {

	private int routenum = 5;
	private int coachnum = 8;
	private int seatnum = 100;
	private int stationnum = 10;
	private int threadnum = 16;

	final static Long INI = (long) 0x1 << 63;
	private int TrainSeatNum;
	private List L[];

	TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
		this.routenum = routenum;
		this.coachnum = coachnum;
		this.seatnum = seatnum;
		this.stationnum = stationnum;
		this.threadnum = threadnum;

		this.TrainSeatNum = coachnum * seatnum;
		this.L = new List[routenum];
		for (int i = 0; i < routenum; i++) {
			this.L[i] = new List(TrainSeatNum, stationnum, seatnum, routenum, threadnum);
		}
		return;
	}

	TicketingDS() {
		this.TrainSeatNum = coachnum * seatnum;
		this.L = new List[routenum];
		for (int i = 0; i < routenum; i++) {
			this.L[i] = new List(TrainSeatNum, stationnum, seatnum, routenum, threadnum);
		}
		return;
	}

	// mutual exclusion before add to node. 
	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
		if (departure < 1 || arrival <= departure || arrival > stationnum || route > routenum) {
			return null;
		}
		Ticket ticket = new Ticket();
		ticket.arrival = arrival;
		ticket.departure = departure;
		ticket.route = route;
		ticket.passenger = passenger;
		if(this.L[route - 1].f_add(ticket)){
			return ticket;
		}
		return null;
	}

	// directly invoke Node.remove(Ticket). 
	public boolean refundTicket(Ticket ticket) {
		return this.L[ticket.route - 1].f_remove(ticket);
	}

	public int inquiry(int route, int departure, int arrival) {
		int k = Node.f_cvt_idx(departure, arrival, this.stationnum);
		int rtn1 = L[route - 1].f_get_left(k);
		
		// long a = INI >> (departure - 1);
		// long b = INI >> (arrival - 1);
		// long c = a ^ b;
		// int rtn2 = L[route - 1].f_get_left(c);
		// if(rtn2 != rtn1){
		// 	System.out.println("Fuck!" + rtn1 + ' ' + rtn2);
		// }
		return rtn1;
	}
}