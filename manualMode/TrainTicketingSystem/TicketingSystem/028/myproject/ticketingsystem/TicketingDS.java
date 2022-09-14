package ticketingsystem;

public class TicketingDS implements TicketingSystem {
	int routenum;
	int coachnum;
	int seatnum;
	int stationnum;
	int threadnum;

	long[] threadTid;
	Route[] routes;

	TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
		while (true) {
			if (stationnum >= 32) {
				System.err.println("Not supportted: stationnum >= 32");
			} else {
				break;
			}
			System.exit(-1);
			break;
		}

		this.routenum = routenum;
		this.coachnum = coachnum;
		this.seatnum = seatnum;
		this.stationnum = stationnum;
		this.threadnum = threadnum;

		routes = new Route[routenum];
		for (int route = 0; route < routenum; route++) {
			routes[route] = new Route(route, coachnum, seatnum, stationnum);
		}
		threadTid = new long[threadnum];
		for (int threadId = 0; threadId < threadnum; threadId++) {
			threadTid[threadId] = threadId * (0x4000000000000000l / threadnum);
		}
	}

	@Override
	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
		// Check
		if (route <= 0 || route > routenum || departure <= 0 || departure >= stationnum || arrival <= 1
				|| arrival > stationnum)
			return null;
		// tid
		int threadId = ThreadId.get() % threadnum;
		long tid = threadTid[threadId]++;
		// buy
		return routes[route - 1].buyTicket(tid, passenger, departure - 1, arrival - 1);
	}

	@Override
	public int inquiry(int route, int departure, int arrival) {
		return routes[route - 1].inquiry(departure - 1, arrival - 1);
	}

	@Override
	public boolean refundTicket(Ticket ticket) {
		int route = ticket.route;
		if (route <= 0 || route > routenum)
			return false;
		return routes[route - 1].refundTicket(ticket);
	}
}
