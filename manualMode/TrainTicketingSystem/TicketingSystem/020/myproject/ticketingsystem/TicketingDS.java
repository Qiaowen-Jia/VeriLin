package ticketingsystem;

import java.util.concurrent.atomic.AtomicLong;

public class TicketingDS implements TicketingSystem {
    private int routeNum;
    private int stationNum;

    private AtomicLong tid;
    private Train[] trains;
    private TicketDepository ticketDepository;

    public TicketingDS() {
        this(5, 8, 100, 10, 16);
    }

    public TicketingDS(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
        this.routeNum = routeNum;
        this.stationNum = stationNum;
        trains = new Train[routeNum + 1];
        for (int i = 1; i < trains.length; i++) {
            trains[i] = new Train3(coachNum, seatNum, stationNum);
        }
        tid = new AtomicLong(1);
        ticketDepository = new TicketDepositoryHashSet(routeNum, coachNum, seatNum);
    }

    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        if (notChecked(route, departure, arrival)) {
            return null;
        }
        int[] coach = new int[1];
        int[] seat = new int[1];
        if (trains[route].get(departure, arrival, coach, seat)) {
            long id = tid.getAndIncrement();
            Ticket ticket = newTicket(id, passenger, route, coach[0], seat[0], departure, arrival);
            ticketDepository.add(ticket);
            return ticket;
        }
        return null;
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        if (notChecked(route, departure, arrival)) {
            return 0;
        }
        return trains[route].inquiry(departure, arrival);
    }

    @Override
    public boolean refundTicket(Ticket ticket) {
        if (ticketDepository.remove(ticket)) {
            trains[ticket.route].put(ticket.departure, ticket.arrival, ticket.coach, ticket.seat);
            return true;
        }
        return false;
    }

    private Ticket newTicket(long tid, String passenger, int route, int coach, int seat, int departure, int arrival) {
        Ticket ticket = new Ticket();
        ticket.tid = tid;
        ticket.passenger = passenger;
        ticket.route = route;
        ticket.coach = coach;
        ticket.seat = seat;
        ticket.departure = departure;
        ticket.arrival = arrival;
        return ticket;
    }

    private boolean notChecked(int route, int departure, int arrival) {
        return 1 > route || route > this.routeNum
                || 1 > departure || departure > this.stationNum
                || 1 > arrival || arrival > this.stationNum
                || departure >= arrival;
    }
}
