package ticketingsystem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TicketingDS implements TicketingSystem {
	private AtomicLong id;
	private ReadWriteLock[] locks;
	private ConcurrentHashMap<Long, Ticket> tickets;
	private int[] seats;
	private Route[] routes;
	private int routeNum;
	private int coachNum;
	private int seatNum;
	private int stationNum;
	private int threadNum;
	private int allSeatsNum;

	public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
		this.id = new AtomicLong();
		this.locks = new ReadWriteLock[routenum];
		this.routes = new Route[routenum];
		for (int i = 0; i < routenum; ++i) {
			locks[i] = new ReentrantReadWriteLock();
			routes[i] = new Route(coachnum, seatnum);
		}
		tickets = new ConcurrentHashMap<Long, Ticket>();
		this.seats = new int[routenum * stationnum];
		this.routeNum = routenum;
		this.coachNum = coachnum;
		this.seatNum = seatnum;
		this.stationNum = stationnum;
		this.threadNum = threadnum;
		this.allSeatsNum = seatnum * coachnum;
	}

	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
		if (route <= 0 || route > routeNum || departure <= 0 || arrival <= departure || arrival > stationNum) {
			return null;
		}
		--route;
		--departure;
		--arrival;
		Ticket ticket;
		locks[route].writeLock().lock();
		int[] coachAndSeat = routes[route].getAvaliableSeat(departure, arrival);
		if (coachAndSeat == null) {
			locks[route].writeLock().unlock();
			return null;
		}
		// System.out.println(coachAndSeat);

		locks[route].writeLock().unlock();
		ticket = createTicket(passenger, route+1, coachAndSeat[0]+1, coachAndSeat[1]+1, departure+1, arrival+1);
		tickets.put(ticket.tid, ticket);
		return ticket;
	}

	public int inquiry(int route, int departure, int arrival) {
		if (route <= 0 || route > routeNum || departure <= 0 || departure >= arrival || arrival > stationNum) {
			return 0;
		}
		--route;
		--departure;
		--arrival;
		locks[route].readLock().lock();
		try {
			// int max = 0;
			// for (int station = departure; station < arrival; ++station) {
			// 	int soldSeats = getSoldSeats(route, station);
			// 	if (soldSeats > max) {
			// 		max = soldSeats;
			// 	}
			// }
			// return allSeatsNum - max;
			return routes[route].getRemainingTickets(departure, arrival);
		} finally {
			locks[route].readLock().unlock();
		}
	}

	public boolean refundTicket(Ticket ticket) {
		if (ticket == null || ticket.route <= 0 || ticket.route > routeNum) {
			return false;
		}
		// debugTicket(ticket);

		locks[ticket.route-1].writeLock().lock();
		try {
			Ticket foundTicket = tickets.get(ticket.tid);
			if (foundTicket == null || !isTicketsEqual(foundTicket, ticket)) {
				return false;
			}
			// for (int coach = ticket.departure - 1; coach < ticket.arrival - 1; ++coach) {
			// 	decreaseSoldSeats(ticket.route-1, coach);
			// }
			routes[ticket.route-1].setSeatAvailable(ticket.coach-1, ticket.seat-1, ticket.departure-1, ticket.arrival-1);
			tickets.remove(ticket.tid);
			return true;
		} finally {
			locks[ticket.route-1].writeLock().unlock();
		}
	}

	private int getSoldSeats(int route, int station) {
		return seats[route * stationNum + station];
	}

	private int getAndIncreaseSoldSeats(int route, int station) {
		return seats[route * stationNum + station]++;
	}

	private void increaseSoldSeats(int route, int station) {
		++seats[route * stationNum + station];
	}

	private int getAndDecreaseSoldSeats(int route, int station) {
		return seats[route * stationNum + station]--;
	}

	private void decreaseSoldSeats(int route, int stataion) {
		--seats[route * stationNum + stataion];
	}

	private Ticket createTicket(String passenger, int route, int coach, int seat, int departure, int arrival) {
		Ticket ticket = new Ticket();
		ticket.tid = id.getAndIncrement();
		ticket.passenger = passenger;
		ticket.route = route;
		ticket.coach = coach;
		ticket.seat = seat;
		ticket.departure = departure;
		ticket.arrival = arrival;
		return ticket;
	}

	private static boolean isTicketsEqual(Ticket lhs, Ticket rhs) {
		return lhs.arrival == rhs.arrival && lhs.coach == rhs.coach && lhs.departure == rhs.departure
				&& lhs.passenger.equals(rhs.passenger) && lhs.route == rhs.route && lhs.seat == rhs.seat;
	}

	private void debugTicket(Ticket ticket) {
		System.out.println("Ticket: " + ticket.tid + " " + ticket.route + " " + ticket.coach + " "
			+ ticket.departure + " " + ticket.arrival);
	}

	public void changeTid(Ticket ticket, int newTid) {
		tickets.remove(ticket.tid);
		ticket.tid = newTid;
		id.set(newTid+1);
		tickets.put((long)newTid, ticket);
	}

}
