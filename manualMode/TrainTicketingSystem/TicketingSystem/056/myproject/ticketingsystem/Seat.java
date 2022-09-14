package ticketingsystem;

import java.util.concurrent.atomic.AtomicBoolean;

public class Seat {
	int seatid;
	boolean stations_occupy[];
	AtomicBoolean lock;

	Seat (int seatid, int stationnum) {
		this.seatid = seatid;
		this.stations_occupy = new boolean[stationnum];
		for (int i = 1; i < stationnum; i++) {
			this.stations_occupy[i] = false;
		}
		this.lock = new AtomicBoolean(false);
	}

	boolean isOccupied (int departure, int arrival) {
		for (int i = departure; i < arrival; i++) {
			if (this.stations_occupy[i]) {
				return true;
			}
		}
		return false;
	}

	boolean buy (int departure, int arrival) {
		if(lock.get()) {
			return false;
		}
		if(lock.getAndSet(true)) {
			return false;
		}
		for (int i = departure; i < arrival; i++) {
			if (this.stations_occupy[i]) {
				lock.set(false);
				return false;
			}
		}
		for (int i = departure; i < arrival; i++) {
			this.stations_occupy[i] = true;
		}
		lock.set(false);
		return true;
	}

	boolean refund (Ticket ticket) {
		while(lock.getAndSet(true)) {}
		for (int i = ticket.departure; i < ticket.arrival; i++) {
			this.stations_occupy[i] = false;
		}
		lock.set(false);
		return true;
	}
}