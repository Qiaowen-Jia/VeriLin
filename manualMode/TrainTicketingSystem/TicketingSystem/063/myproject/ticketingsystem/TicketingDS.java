package ticketingsystem;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentHashMap;

public class TicketingDS implements TicketingSystem {

    AtomicInteger ticketID;
    private Train[] trains;
    private ConcurrentHashMap<Long, Ticket> ticket_sold;
    private int seat_num;

    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
        trains = new Train[routenum];
        for (int i = 0; i < routenum; i++) {
            trains[i] = new Train();
            trains[i].Train(coachnum, seatnum, stationnum);
        }
        ticketID = new AtomicInteger(1);
        ticket_sold = new ConcurrentHashMap<Long, Ticket>();
        seat_num = seatnum;
    }

    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        int tmp = trains[route - 1].try_lock(departure, arrival);
        int seat;
        Ticket ticket = new Ticket();
        while (tmp != -1) {
            trains[route - 1].try_unlock(departure, tmp);
            tmp = trains[route - 1].try_lock(departure, arrival);
        }
        seat = trains[route - 1].buy_ticket(departure, arrival);

        trains[route - 1].release_lock(departure, arrival - 1);
        if(seat == -1){
            ticket = null;
            return ticket;
        }
        ticket.tid = ticketID.getAndIncrement();
            ticket.passenger = passenger;
            ticket.route = route;
            ticket.departure = departure;
            ticket.arrival = arrival;
            int seat_tmp = seat % trains[route - 1].seat;
            ticket.seat = (seat_tmp == 0) ? trains[route - 1].seat : seat_tmp;
            int coach_tmp = seat / trains[route - 1].seat;
            ticket.coach = (seat_tmp == 0) ? coach_tmp : coach_tmp + 1;
            ticket_sold.put(ticket.tid, ticket);
            return ticket;

        
    }

    public int inquiry(int route, int departure, int arrival) {
        int tmp = trains[route - 1].try_lock(departure, arrival);
        while (tmp != -1) {
            trains[route - 1].try_unlock(departure, tmp);
            tmp = trains[route - 1].try_lock(departure, arrival);
        }
        int num = trains[route - 1].inquiry(departure, arrival);
        trains[route - 1].release_lock(departure, arrival - 1);
        return num;
    }

    public boolean refundTicket(Ticket ticket) {
        if (!ticket_sold.containsKey(ticket.tid)) {
            return false;
        }
        if (!ticket.equals(ticket_sold.get(ticket.tid))) {
            return false;
        }
        int seat = seat_num * (ticket.coach - 1) + ticket.seat;
        int departure = ticket.departure;
        int arrival = ticket.arrival;
        int route = ticket.route;
        int tmp = trains[route - 1].try_lock(departure, arrival);

        while (tmp != -1) {
            trains[route - 1].try_unlock(departure, tmp);

            tmp = trains[route - 1].try_lock(departure, arrival);
        }
        trains[route - 1].refund_ticket(departure, arrival, seat);
        ticket_sold.remove(ticket.tid, ticket);
        trains[route - 1].release_lock(departure, arrival - 1);
        return true;
    }

}
