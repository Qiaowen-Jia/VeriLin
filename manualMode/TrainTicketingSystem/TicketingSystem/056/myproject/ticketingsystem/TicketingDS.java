package ticketingsystem;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;

public class TicketingDS implements TicketingSystem {

	int routenum;//default:5
	int coachnum;//default:8
	int seatnum;//default:100
	int stationnum;//default:10
	int threadnum;//default:16

	Route routes[];

	AtomicLong tid;

	ConcurrentHashMap<Long, Ticket> ticketspool;


	TicketingDS () {
		this.routenum = 5;
		this.coachnum = 8;
		this.seatnum = 100;
		this.stationnum = 10;
		this.threadnum = 16;

		this.routes = new Route[routenum + 1];
		for (int i = 1; i <= routenum; i++) {
			routes[i] = new Route(i, coachnum, seatnum, stationnum);
		}

		this.tid = new AtomicLong(1);

		this.ticketspool = new ConcurrentHashMap<>();
	}


	TicketingDS (int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
		this.routenum = routenum;
		this.coachnum = coachnum;
		this.seatnum = seatnum;
		this.stationnum = stationnum;
		this.threadnum = threadnum;

		this.routes = new Route[routenum + 1];
		for (int i = 1; i <= routenum; i++) {
			routes[i] = new Route(i, coachnum, seatnum, stationnum);
		}

		this.tid = new AtomicLong(1);

		this.ticketspool = new ConcurrentHashMap<>();
	}


	public Ticket buyTicket (String passenger, int route, int departure, int arrival) {
		if (route < 1 || route > this.routenum) {
			return null;
		}
		if (departure < 1 || departure >= this.stationnum) {
			return null;
		}
		if (arrival <= 1 || arrival > this.stationnum) {
			return null;
		}
		if (departure >= arrival) {
			return null;
		}

		while (hasTicket(route, departure, arrival)) {
			for (int i = 1; i <= this.coachnum; i++) {
				for (int j = 1; j <= this.seatnum; j++) {
					if (!this.routes[route].coaches[i].seats[j].isOccupied(departure, arrival)) {
						if (this.routes[route].coaches[i].seats[j].buy(departure, arrival)) {
							Ticket newticket = new Ticket();
							newticket.tid = this.tid.getAndIncrement();
							newticket.passenger = passenger;
							newticket.route = route;
							newticket.coach = i;
							newticket.seat = j;
							newticket.departure = departure;	
							newticket.arrival = arrival;
							ticketspool.put(newticket.tid, newticket);
							return newticket;
						}
					}
				}
			}
		}
		return null;
	}


	public int inquiry (int route, int departure, int arrival) {
		if (route < 1 || route > this.routenum) {
			return 0;
		}
		if (departure < 1 || departure >= this.stationnum) {
			return 0;
		}
		if (arrival <= 1 || arrival > this.stationnum) {
			return 0;
		}
		if (departure >= arrival) {
			return 0;
		}

		int ticketnum = 0;

		for (int i = 1; i <= coachnum; i++) {
			for (int j = 1; j <= seatnum; j++) {
				if (!this.routes[route].coaches[i].seats[j].isOccupied(departure, arrival)) {
					ticketnum++;
				}
			}
		}

		return ticketnum;
	}


	public boolean refundTicket (Ticket ticket) {
		if (ticket == null) {
			return false;
		}
		Ticket pool_ticket = ticketspool.get(ticket.tid);
		if (pool_ticket != null) {
			if (pool_ticket.tid == ticket.tid &&
				pool_ticket.passenger == ticket.passenger &&
				pool_ticket.route == ticket.route &&
				pool_ticket.coach == ticket.coach &&
				pool_ticket.seat == ticket.seat &&
				pool_ticket.departure == ticket.departure &&
				pool_ticket.arrival == ticket.arrival) 
			{
				if (ticketspool.remove(ticket.tid) != null) {
					routes[ticket.route].coaches[ticket.coach].seats[ticket.seat].refund(ticket);
					return true;
				}
			}
		}
		return false;
	}

	public boolean hasTicket (int route, int departure, int arrival) {

		for (int i = 1; i <= coachnum; i++) {
			for (int j = 1; j <= seatnum; j++) {
				if (!this.routes[route].coaches[i].seats[j].isOccupied(departure, arrival)) {
					return true;
				}
			}
		}

		return false;
	}


}
