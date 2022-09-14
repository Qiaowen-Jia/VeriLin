package ticketingsystem;

import java.util.concurrent.atomic.AtomicLong;

public class Seat {
    final int stationNum;
    AtomicLong seats;

    Seat(int stationNum) {
        this.stationNum = stationNum;
        seats = new AtomicLong(0);
    }

//    private long getMask(int departure, int arrival) {
//        return rangeMask[(departure - 1) * stationNum + arrival - 1];
//    }

    int queryVacant(final long mask) {
//        long mask = 0;
//        for (int i = departure - 1; i < arrival - 1; ++i) {
//            mask |= (1 << i);
//        }
        long result = seats.get() & mask ; // getMask(departure, arrival);
        return (result == 0) ? 1 : 0;
    }

    boolean occupy(final long mask) {
        long oldSeats;
        long newSeats;
//        long mask = getMask(departure, arrival);
//        long mask = 0;
//        for (int i = departure - 1; i < arrival - 1; ++i) {
//            mask |= (1 << i);
//        }
        do {
            oldSeats = this.seats.get();
            long result = oldSeats & mask;
            if (result != 0) { // already occupied!
                return false;
            }
            newSeats = oldSeats | mask;
        } while (!this.seats.compareAndSet(oldSeats, newSeats));
        return true;
    }

    boolean vacate(final long mask) {
        long oldSeats;
        long newSeats;
//        long mask = getMask(departure, arrival);
//        long mask = 0;
//        for (int i = departure - 1; i < arrival - 1; ++i) {
//            mask |= (1 << i);
//        }
        do {
            oldSeats = this.seats.get();
            long result = oldSeats & mask;
            if (result == 0) { // already vacant!
                return false;
            }
            newSeats = oldSeats & (~mask);
        } while (!this.seats.compareAndSet(oldSeats, newSeats));
        return true;
    }
}
