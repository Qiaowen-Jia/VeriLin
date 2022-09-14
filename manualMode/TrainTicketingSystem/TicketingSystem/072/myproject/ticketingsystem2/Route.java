package ticketingsystem;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class Route {
    int route;
    int coachNum;
    int seatNum;
    int stationNum;
    int totalSeatNum;
    private Seat[] seats;  // represent 2D coach-to-seat structure in 1D array
    AtomicLong ticketId;
    Set<Ticket> soldTickets;

    Route(int route, int coachNum, int seatNum, int stationNum) {
        this.route = route;
        this.coachNum = coachNum;
        this.seatNum = seatNum;
        this.stationNum = stationNum;
        this.totalSeatNum = coachNum * seatNum;
        this.seats = new Seat[totalSeatNum];
        this.ticketId = new AtomicLong(10_000_000 * (route - 1));
        this.soldTickets = ConcurrentHashMap.newKeySet();  // keySet is much faster than valueSet!

        for (int i = 0; i < totalSeatNum; ++i) {
            this.seats[i] = new Seat(stationNum);
        }
    }

    Seat getSeat(int coach, int seat) {
        int seatId = (coach - 1) * seatNum + seat - 1;
        // System.out.printf("Coach %d Seat %d ID %d\n", coach, seat, seatId);
        return seats[seatId];
    }

    Ticket buyTicket(String passenger, int departure, int arrival) {
        for (int i = 0; i < coachNum; ++i) {
            for (int j = 0; j < seatNum; ++j) {
                Seat seat = getSeat(i + 1, j + 1);
                if (seat.queryOccupied(departure, arrival)) {
                    if (seat.occupy(departure, arrival)) {
                        Ticket ticket = new Ticket(passenger, this.route, i + 1, j + 1, departure, arrival);
                        ticket.tid = ticketId.getAndIncrement();
                        soldTickets.add(ticket);
                        return ticket;
                    }
                }
            }
        }
        return null;
    }

    boolean refundTicket(Ticket ticket) {
        // two-phase approach: examine firstly, then delete
        // must treat modifying occupied as a transaction
        if (!this.soldTickets.contains(ticket)) {
            return false;
        }
        this.soldTickets.remove(ticket);
        Seat seat = getSeat(ticket.coach, ticket.seat);
        return seat.vacate(ticket.departure, ticket.arrival);
    }

    int inquiry(int departure, int arrival) {
        int count = 0;
        for (int i = 0; i < coachNum; ++i) {
            for (int j = 0; j < seatNum; ++j) {
                Seat seat = getSeat(i + 1, j + 1);
                if (seat.queryVacant(departure, arrival)) {
                    ++count;
                }
            }
        }
        return count;
    }
}
