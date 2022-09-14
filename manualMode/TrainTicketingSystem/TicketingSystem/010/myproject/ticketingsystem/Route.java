package ticketingsystem;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Jianyong Feng
 **/
public class Route {
    private AtomicLong ticketId;
    private final Station station;
    private int routeId;
    private int seatNum;
    private Object[] objects;

    public Route(int routeId, int coachNum, int seatNum, BitSet[] seatStatus) {
        this.routeId = routeId;
        ticketId =new AtomicLong(1L);
        this.seatNum = seatNum;
        int totalSeatNum = coachNum * seatNum;
        int wordInUse = totalSeatNum / 64 + 1;
        objects = new Object[wordInUse];
        for (int i = 0; i < wordInUse; i++)
            objects[i] = new Object();
        this.station = new Station(seatNum, totalSeatNum, seatStatus, objects);
    }

    public Ticket trySealTicket(String passenger, int departure, int arrival) {
        int seatIndex = station.getSeatIndex(departure, arrival);
        while (seatIndex != -1) {
            int wordIndex = BitSet.getWordIndex(seatIndex);
            synchronized (objects[wordIndex]) {
                if (station.occupyOneSeat(departure, arrival, seatIndex)) {
                    int coach = (seatIndex - 1) / seatNum + 1;
                    int seat = (seatIndex - 1) % seatNum + 1;
                    long tid = routeId * 100000000L + ticketId.getAndIncrement();
                    return generateTicket(tid, passenger, routeId, departure, arrival, coach, seat);
                }
                seatIndex = station.getSeatIndex(departure, arrival);
            }
        }
        return null;
    }

    public boolean tryRefundTicket(Ticket ticket) {
        boolean refund;
        int seatIndex = (ticket.coach - 1) * seatNum + ticket.seat;
        int wordIndex = BitSet.getWordIndex(seatIndex);
        synchronized (objects[wordIndex]) {
            refund = station.freeOneSeat(ticket.departure, ticket.arrival, seatIndex);
            return refund;
        }
    }

    public int inquiryTicket(int departure, int arrival) {
        return station.inquiryLeftTicket(departure, arrival);
    }

    private static Ticket generateTicket(
            long tid,
            String passenger,
            int route,
            int departure,
            int arrival,
            int coach,
            int seat
    ) {
        Ticket ticket = new Ticket();
        ticket.tid = tid;
        ticket.passenger = passenger;
        ticket.route = route;
        ticket.departure = departure;
        ticket.arrival = arrival;
        ticket.coach = coach;
        ticket.seat = seat;

        return ticket;
    }
}
