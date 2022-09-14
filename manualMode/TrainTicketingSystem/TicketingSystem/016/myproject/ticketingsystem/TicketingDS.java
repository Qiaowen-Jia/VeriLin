package ticketingsystem;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class TicketingDS implements TicketingSystem {
    private Route[] routes;
    private AtomicLong idGenerator = new AtomicLong(1);
    private ConcurrentHashMap<Long, Ticket> soldTicket;

    /**
     * @param routenum 车次数量
     * @param coachnum 车厢数量
     * @param seatnum 座位数量
     * @param stationnum 站点数量
     * @param threadnum 线程数量
     */
    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
        routes = new Route[routenum + 1];
        for (int i = 1; i <= routenum; i ++) {
            routes[i] = new Route(i, coachnum, seatnum, stationnum);
        }
        soldTicket = new ConcurrentHashMap<>();
    }

    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        Ticket ticket = routes[route].buyTicket(departure, arrival);
        if (ticket != null) {
            ticket.tid = idGenerator.getAndIncrement();
            ticket.passenger = passenger;
            soldTicket.put(ticket.tid, ticket);
        }
        return ticket;
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        return routes[route].inquiry(departure, arrival);
    }

    @Override
    public boolean refundTicket(Ticket ticket) {
        if (!soldTicket.containsKey(ticket.tid)) {
            return false;
        }
        Ticket sold = soldTicket.get(ticket.tid);
        if (!sold.passenger.equals(ticket.passenger)
                || sold.route != ticket.route
                || sold.coach != ticket.coach
                || sold.departure != ticket.departure
                || sold.arrival != ticket.arrival
                || sold.seat != ticket.seat) {
            return false;
        }
        routes[ticket.route].refundTicket(ticket);
        soldTicket.remove(ticket.tid);
        return true;
    }
}
