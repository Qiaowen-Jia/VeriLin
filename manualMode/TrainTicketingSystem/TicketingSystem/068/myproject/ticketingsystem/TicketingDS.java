package ticketingsystem;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class TicketingDS implements TicketingSystem {
	final int routeNum;
	final int coachNum;
	final int seatNum;
	final int totalSeats;
	final int stationNum;
	final int threadNum;

	Route[] Route;
	AtomicInteger idgenerator;
	ConcurrentHashMap<Integer, Ticket> soldTick;

	public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
		this.routeNum = routenum;
		this.coachNum = coachnum;
		this.seatNum = seatnum;
		this.threadNum = threadnum;
		this.totalSeats = coachNum * seatNum;
		this.stationNum = stationnum;
		this.idgenerator = new AtomicInteger(0);
		this.soldTick = new ConcurrentHashMap<>();
		this.Route = new Route[routeNum];
		for (int i = 0; i < routeNum; i++) {
			this.Route[i] = new Route(totalSeats, stationNum);
		}
	}

	@Override
	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
		int seatNO = Route[route - 1].buyThisTrain(totalSeats, departure, arrival);
		if (seatNO == -1)
			return null;
		Ticket tick = new Ticket(passenger, route, departure, arrival, seatNO / seatNum + 1, seatNO % seatNum + 1,
				(long) (route * 10000000 + idgenerator.getAndIncrement()));
		soldTick.put(tick.myhashCode(), tick);
		return tick;
	}

	@Override
	public int inquiry(int route, int departure, int arrival) {
		return Route[route - 1].inquiryThisTrain(totalSeats, departure, arrival);
	}

	@Override
	public boolean refundTicket(Ticket ticket) {
		if (soldTick.containsKey(ticket.myhashCode())) {
			Ticket boughtTick = soldTick.get(ticket.myhashCode());
			if (boughtTick != null) {
				if ((boughtTick.tickEquals(ticket))) {
					soldTick.remove(boughtTick.myhashCode());
					return Route[ticket.route - 1].refundThisTrain(ticket, seatNum);
				}
			}
		}
		return false;
	}
}

class Route {
	int totalseatsNum;
	Seat[] seats;

	class Seat {
		ReentrantLock lock;
		boolean[] seatstatus;

		public Seat(int stationNum) {
			lock = new ReentrantLock();
			seatstatus = new boolean[stationNum];
			for (int i = 0; i < stationNum; i++) {
				seatstatus[i] = false;
			}
		}
	}

	public Route(int totalSeats, int stationNum) {
		this.totalseatsNum = totalSeats;
		this.seats = new Seat[totalseatsNum];
		for (int i = 0; i < totalseatsNum; i++) {
			this.seats[i] = new Seat(stationNum);
		}
	}

	boolean validate(Seat seat, int departure, int arrival) {
		for (int i = departure - 1; i < arrival - 1; i++) {
			if (seat.seatstatus[i])
				return false;
		}
		return true;
	}

	void flipStatus(Seat seat, int departure, int arrival) {
		for (int i = departure - 1; i < arrival - 1; i++)
			seat.seatstatus[i] = !seat.seatstatus[i];
	}

	int buyThisTrain(int seatsNum, int departure, int arrival) {
		for (int i = 0; i < seatsNum; i++) {
			boolean flg = false;
			for (int j = departure - 1; j < arrival - 1; j++) {
				if (seats[i].seatstatus[j]) {
					flg = true;
					break;
				}
			}
			if (!flg) {
				seats[i].lock.lock();
				try {
					if (validate(seats[i], departure, arrival)) {
						flipStatus(seats[i], departure, arrival);
						return i;
					}
				} finally {
					seats[i].lock.unlock();
				}
			}
		}
		return -1;
	}

	int inquiryThisTrain(int total, int departure, int arrival) {
		int count = 0;
		for (int i = 0; i < total; i++) {
			boolean flg = false;
			for (int j = departure - 1; j < arrival - 1; j++) {
				if (seats[i].seatstatus[j]) {
					flg = true;
				}
			}
			if(!flg)
				count++;
		}
		return count;
	}

	boolean refundThisTrain(Ticket ticket, int seatnum) {
		int seatNO = (ticket.coach - 1) * seatnum + ticket.seat - 1;
		seats[seatNO].lock.lock();
		try{
			flipStatus(seats[seatNO], ticket.departure, ticket.arrival);
		}finally{
			seats[seatNO].lock.unlock();
		}
		return true;
	}
}
