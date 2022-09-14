package ticketingsystem;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class Route {
    final int route;
    final int coachNum;
    final int seatNum;
    final int stationNum;
    int totalSeatNum;
    private Seat[] seats;  // represent 2D coach-to-seat structure in 1D array
    long[] rangeMask;
    AtomicLong ticketId;
    Set<Ticket> soldTickets;

    Route(int route, int coachNum, int seatNum, int stationNum, long[] rangeMask) {
        this.route = route;
        this.coachNum = coachNum;
        this.seatNum = seatNum;
        this.stationNum = stationNum;
        this.totalSeatNum = coachNum * seatNum;
        this.rangeMask = rangeMask;
        this.seats = new Seat[totalSeatNum];
        this.ticketId = new AtomicLong(10_000_000 * (route - 1));  // should be enough for load
        this.soldTickets = ConcurrentHashMap.newKeySet();  // keySet is much faster than valueSet!

        for (int i = 0; i < totalSeatNum; ++i) {
            this.seats[i] = new Seat(stationNum);
        }
    }

//    Seat getSeat(int coach, int seat) {
//        int seatId = (coach - 1) * seatNum + seat - 1;
//        // System.out.printf("Coach %d Seat %d ID %d\n", coach, seat, seatId);
//        return seats[seatId];
//    }

    Ticket buyTicket(final String passenger, final int departure, final int arrival) {
        int randomCoach = ThreadLocalRandom.current().nextInt(this.coachNum);
        for (int i = 0; i < coachNum; ++i) {
            int randomSeat = ThreadLocalRandom.current().nextInt(this.seatNum);
            for (int j = 0; j < seatNum; ++j) {
//                Seat seat = getSeat(randomCoach + 1, randomSeat + 1);
                if (seats[randomCoach * seatNum + randomSeat].occupy(
                        rangeMask[(departure - 1) * stationNum + arrival - 1])) {
                    Ticket ticket = new Ticket();
                    ticket.passenger = passenger;
                    ticket.route = this.route;
                    ticket.coach = randomCoach + 1;
                    ticket.seat = randomSeat + 1;
                    ticket.departure = departure;
                    ticket.arrival = arrival;
                    ticket.tid = ticketId.getAndIncrement();
                    soldTickets.add(ticket);
                    return ticket;
                }
                randomSeat = (randomSeat + 1) % this.seatNum;
            }
            randomCoach = (randomCoach + 1) % this.coachNum;
        }
        return null;
    }

    boolean refundTicket(final Ticket ticket) {
//        Seat seat = getSeat(ticket.coach, ticket.seat);
        if (!this.soldTickets.contains(ticket)) {
            return false;
        }
        this.soldTickets.remove(ticket);
        return seats[(ticket.coach - 1) * seatNum + ticket.seat - 1].vacate(
                rangeMask[(ticket.departure - 1) * stationNum + ticket.arrival - 1]);
    }

    int inquiry(final int departure, final int arrival) {
        int count = 0;
        for (int i = 0; i < coachNum; ++i) {
            for (int j = 0; j < seatNum; ++j) {
//                Seat seat = getSeat(i + 1, j + 1);
                count += seats[i * seatNum + j].queryVacant(
                        rangeMask[(departure - 1) * stationNum + arrival - 1]);
            }
        }
        return count;
    }
}
