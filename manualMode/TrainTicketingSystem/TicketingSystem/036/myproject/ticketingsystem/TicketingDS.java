package ticketingsystem;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class TicketingDS implements TicketingSystem {

    private AtomicLong[] seats;
    final private int seatnall;
    final private int seatnpr;
    final private int seatn;
    private AtomicLong ticketID;
    private ConcurrentHashMap<Long, Ticket> soldTicket;
    public TicketingDS(final int routenum, final int coachnum, final int seatnum, final int stationnum, final int threadnum){
        seatn = seatnum;
        seatnpr = coachnum * seatn;
        seatnall = routenum * seatnpr;
        seats = new AtomicLong[seatnall];
        ticketID = new AtomicLong(1);
        for(int i = 0; i < seatnall; i++){
            seats[i] = new AtomicLong(0);
        }
        soldTicket = new ConcurrentHashMap<Long, Ticket>();
    }

    public Ticket buyTicket(String passenger, final int route, final int departure, final int arrival) {
        int low = (route - 1) * seatnpr;
        int high = route * seatnpr;
        long mask = ((1 << (arrival - departure)) - 1) << (departure - 1);
        for (int i = low; i < high; i++) {
            long tmp = seats[i].get();
            while((mask & tmp) == 0){
                if(seats[i].compareAndSet(tmp, (tmp | mask))){
                    Ticket ticket = new Ticket();
                    ticket.tid = ticketID.getAndIncrement();
                    ticket.passenger = passenger;
                    ticket.route = route;
                    ticket.departure = departure;
                    ticket.arrival = arrival;
                    ticket.coach = (i - low) / seatn + 1;
                    ticket.seat = (i - low) % seatn + 1;
                    soldTicket.put(ticket.tid, ticket);
                    return ticket;
                }
                tmp = seats[i].get();
            }
        }
        return null;
    }

    public int inquiry(final int route, final int departure, final int arrival) {
        int rest = 0;
        int low = (route - 1) * seatnpr;
        int high = route * seatnpr;
        long mask = ((1 << (arrival - departure)) - 1) << (departure - 1);
        for(int i = low; i < high; i++){
            if((mask & seats[i].get()) == 0){
                rest++;
            }
        }
        return rest;
    }

    public boolean refundTicket(Ticket ticket) {
        if(!soldTicket.containsKey(ticket.tid) || !ticket.equals(soldTicket.get(ticket.tid))){
            return false;
        }
        int seat = (ticket.route - 1) * seatnpr + (ticket.coach - 1) * seatn + (ticket.seat - 1);
        long mask = ((1 << (ticket.arrival - ticket.departure)) - 1) << (ticket.departure - 1);
        while(true) {
            long tmp = seats[seat].get();
            if (seats[seat].compareAndSet(tmp, (tmp & ~mask))) {
                return soldTicket.remove(ticket.tid, ticket);
            }
        }
    }
}