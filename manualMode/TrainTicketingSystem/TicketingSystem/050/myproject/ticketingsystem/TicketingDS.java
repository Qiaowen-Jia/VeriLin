package ticketingsystem;

public class TicketingDS implements TicketingSystem {
		
	public int routenum=5, coachnum=8, seatnum=100, stationnum=10, threadnum=16;
	//public static ConcurrentHashMap<Long, boolean> ticketPool;
	public RouteDS[] routes;

	public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
        this.routenum = (routenum == 0) ? 5 : routenum;
        this.coachnum = (coachnum == 0) ? 8 : coachnum;
        this.seatnum = (seatnum == 0) ? 100 : seatnum;
        this.stationnum = (stationnum == 0) ? 10 : stationnum;
        this.threadnum = (threadnum == 0) ? 16 : threadnum;
        this.routes = new RouteDS[routenum + 1];
        for (int i = 1; i <= routenum; i++) {
            this.routes[i] = new RouteDS(i, this.coachnum, this.seatnum, this.stationnum);
        }
        //hasAllot = new ConcurrentHashMap<>(5000000);
    }

	@Override
	public Ticket buyTicket(String passenger, int route, int departure, int arrival){
		if(!buyCheck(route, departure, arrival)) return null;		
		//System.out.println("[Buy]:"+"\t"+route+"\t"+departure+"\t"+arrival+"\t"+passenger);		
		int[] res = routes[route].buyTicket(departure, arrival);
		if(res == null) return null;
		
		Ticket newTicket = new Ticket();
		newTicket.passenger = passenger;
		newTicket.route = route;
		newTicket.coach = (res[0]-1) / seatnum + 1;
		newTicket.seat = (res[0]-1) % seatnum + 1;
		newTicket.departure = departure;
		newTicket.arrival = arrival;
		long stamp = routes[route].timeStamps[newTicket.coach].getAndIncrement();
		//System.out.println(res[0] + "\n" + res[1] * 10000 + "\n" + route * 1000 * 10000);
		newTicket.tid = res[0] + res[1] * 10000  + route * 1000 * 10000 + stamp * 100 * 1000 * 10000;
		routes[route].soldTickets.get(newTicket.coach).put(newTicket.tid, newTicket);
		return newTicket;
	}

	@Override
	public int inquiry(int route, int departure, int arrival){		
		//System.out.println("[Quiry]:"+"\t"+route+"\t"+departure+"\t"+arrival+"\t");		
		if(buyCheck(route, departure, arrival))
			return routes[route].inquiry(departure, arrival);
		else
			return 0;
	}


	@Override
	public boolean refundTicket(Ticket ticket){
		//System.out.println("[Refund]:"+"\t"+ticket.route+"\t"
		//			+ticket.departure+"\t"+ticket.arrival+"\t"+ticket.passenger);	
		Ticket realticket = routes[ticket.route].soldTickets.get(ticket.coach).get(ticket.tid);
		if(realticket == null || realticket.passenger != ticket.passenger || 
			realticket.route != ticket.route || realticket.coach != ticket.coach ||
			realticket.seat != ticket.seat || realticket.departure != ticket.departure ||
			realticket.arrival != ticket.arrival) return false;
		if(routes[ticket.route].soldTickets.get(ticket.coach).remove(ticket.tid) == null) return false;
		return routes[ticket.route].refundTicket(ticket);

	}

	private boolean buyCheck(int route, int departure, int arrival){
		if(route < 1 || route > this.routenum || departure >= arrival || 
			departure < 1 || arrival > this.stationnum)
			return false;
		else return true;
	}
}
