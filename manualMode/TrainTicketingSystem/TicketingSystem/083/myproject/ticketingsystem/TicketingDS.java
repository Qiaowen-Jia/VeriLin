package ticketingsystem;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;

final class SeatSelectResult {
	boolean isSucceed = false;
	int coach;
	int seat;

	SeatSelectResult(boolean succeedIn, int coachIn, int seatIn) {
		isSucceed = succeedIn;
		coach = coachIn;
		seat = seatIn;
	}

	SeatSelectResult(boolean succeedIn) {
		isSucceed = succeedIn;
	}

	static SeatSelectResult fail() {
		return new SeatSelectResult(false);
	}

	static SeatSelectResult succeed(int coachIn, int seatIn) {
		return new SeatSelectResult(true, coachIn, seatIn);
	}
}

final class StationLock {
	public ReentrantReadWriteLock[] lockArray;

	StationLock(int stationNum) {
		lockArray = new ReentrantReadWriteLock[stationNum];
		for (int i = 0; i < stationNum; i++) {
			lockArray[i] = new ReentrantReadWriteLock();
		}
	}

	public boolean rlock(int departure, int arrival) {
		if (departure >= arrival) {
			throw new IllegalArgumentException("Invalid departure " + departure + " >= arrival " + arrival);
		}
		for (int i = departure; i < arrival; i++) {
			lockArray[i].readLock().lock();
		}
		return true;
	}

	public boolean runlock(int departure, int arrival) {
		if (departure >= arrival) {
			throw new IllegalArgumentException("Invalid departure " + departure + " >= arrival " + arrival);
		}
		for (int i = arrival - 1; i >= departure; i--) {
			lockArray[i].readLock().unlock();
		}
		return true;
	}

	public boolean wlock(int departure, int arrival) {
		if (departure >= arrival) {
			throw new IllegalArgumentException("Invalid departure " + departure + " >= arrival " + arrival);
		}
		for (int i = departure; i < arrival; i++) {
			lockArray[i].writeLock().lock();
		}
		return true;
	}

	public boolean wunlock(int departure, int arrival) {
		if (departure >= arrival) {
			throw new IllegalArgumentException("Invalid departure " + departure + " >= arrival " + arrival);
		}
		for (int i = arrival - 1; i >= departure; i--) {
			lockArray[i].writeLock().unlock();
		}
		return true;
	}
}

final class Train {
	boolean[][][] bitmap;
	int coachnum;
	int seatnum;
	int stationnum;
	int threadnum;
	final StationLock trainLock;

	Train(int coachnumIn, int seatnumIn, int stationnumIn, int threadnumIn) {
		bitmap = new boolean[coachnumIn][seatnumIn][stationnumIn];
		coachnum = coachnumIn;
		seatnum = seatnumIn;
		stationnum = stationnumIn;
		threadnum = threadnumIn;
		for (int i = 0; i < coachnum; i++) {
			for (int j = 0; j < seatnum; j++) {
				for (int k = 0; k < stationnum; k++) {
					bitmap[i][j][k] = false;
				}
			}
		}
		trainLock = new StationLock(stationnumIn);
	}

	public SeatSelectResult buyTicket(int departure, int arrival) {
		trainLock.wlock(departure, arrival);
		for (int i = 0; i < coachnum; i++) {
			seatSelect: for (int j = 0; j < seatnum; j++) {
				for (int k = departure; k < arrival; k++) {
					if (bitmap[i][j][k]) { // if seat has been allocated
						continue seatSelect; // try next seat
					}
				}
				// current seat is valid
				for (int k = departure; k < arrival; k++) {
					bitmap[i][j][k] = true; // allocate seat
				}
				// System.err.printf("seat select coach %d seat %d\n", i, j);
				trainLock.wunlock(departure, arrival);
				return SeatSelectResult.succeed(i, j);
			}
		}
		// System.err.println("FAIL");
		trainLock.wunlock(departure, arrival);
		return SeatSelectResult.fail();
	}

	public int inquiry(int departure, int arrival) {
		trainLock.rlock(departure, arrival);
		int cnt = 0;
		for (int i = 0; i < coachnum; i++) {
			seatSelect: for (int j = 0; j < seatnum; j++) {
				for (int k = departure; k < arrival; k++) {
					if (bitmap[i][j][k]) { // if seat has been allocated
						continue seatSelect;
					}
				}
				// current seat is valid
				cnt++;
			}
		}
		trainLock.runlock(departure, arrival);
		return cnt;
	}

	public boolean refundTicket(int coach, int seat, int departure, int arrival) {
		// for (int k = departure; k < arrival; k++) {
		// if (!bitmap[coach][seat][k]) { // if seat has been allocated
		// assert(false); // fatal error! refund should always success
		// }
		// }
		if (departure >= arrival) {
			throw new IllegalArgumentException("Invalid departure " + departure + " >= arrival " + arrival);
		}
		for (int k = departure; k < arrival; k++) {
			bitmap[coach][seat][k] = false;
		}
		return true;
	}
}

public class TicketingDS implements TicketingSystem {
	Train[] train;
	ConcurrentHashMap<Long, Ticket> ticketHash = new ConcurrentHashMap<>();

	TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
		train = new Train[routenum];
		for (int i = 0; i < routenum; i++) {
			train[i] = new Train(coachnum, seatnum, stationnum, threadnum);
		}
	}

	private static AtomicLong nextTid = new AtomicLong(0);

	private long newTID() {
		return nextTid.getAndIncrement();
	}

	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
		SeatSelectResult result = train[route - 1].buyTicket(departure - 1, arrival - 1);
		if (result.isSucceed) {
			Ticket tkt = new Ticket(newTID(), passenger, route, result.coach + 1, result.seat + 1, departure, arrival);
			ticketHash.put(tkt.tid, tkt);
			return tkt;
		} else {
			return null;
		}
	}

	public int inquiry(int route, int departure, int arrival) {
		return train[route - 1].inquiry(departure - 1, arrival - 1);
	}

	public boolean ticketEqual(Ticket ticket1, Ticket ticket2) {
		if (ticket1.arrival != ticket2.arrival)
			return false;
		if (ticket1.coach != ticket2.coach)
			return false;
		if (ticket1.departure != ticket2.departure)
			return false;
		if (ticket1.passenger != ticket2.passenger)
			return false;
		if (ticket1.route != ticket2.route)
			return false;
		if (ticket1.seat != ticket2.seat)
			return false;
		return true;
	}

	public boolean refundTicket(Ticket ticket) {
		if (ticketHash.containsKey(ticket.tid)) {
			train[ticket.route - 1].trainLock.wlock(ticket.departure - 1, ticket.arrival - 1);
			Ticket ticketInHashTable = ticketHash.get(ticket.tid);
			// check if that ticket is still valid
			if (ticketInHashTable == null) // other thread refund successfully
				return false;
			if (!ticketEqual(ticket, ticketInHashTable)) { // check if that ticket is valid
				train[ticket.route - 1].trainLock.wunlock(ticket.departure - 1, ticket.arrival - 1);
				return false;
			}
			ticketHash.remove(ticket.tid);
			boolean retval = train[ticket.route - 1].refundTicket(ticket.coach - 1, ticket.seat - 1,
					ticket.departure - 1, ticket.arrival - 1);
			train[ticket.route - 1].trainLock.wunlock(ticket.departure - 1, ticket.arrival - 1);
			return retval;
		} else {
			return false;
		}
	}
}
