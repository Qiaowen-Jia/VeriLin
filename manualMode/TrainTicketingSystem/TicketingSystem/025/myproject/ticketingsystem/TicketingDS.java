package ticketingsystem;

import java.util.concurrent.ConcurrentHashMap;

public class TicketingDS implements TicketingSystem {
    public int routenum;
    public int coachnum;
    public int seatnum;
    public int stationnum;
    public int threadnum;

    //
    public RouteDS[] allRoutes;
    // 维护一个已分配的tid的map
    public static ConcurrentHashMap<Long, Boolean> hashAllot;

    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
        this.routenum = (routenum == 0) ? 5 : routenum;
        this.coachnum = (coachnum == 0) ? 8 : coachnum;
        this.seatnum = (seatnum == 0) ? 100 : seatnum;
        this.stationnum = (stationnum == 0) ? 10 : stationnum;
        this.threadnum = (threadnum == 0) ? 16 : threadnum;
        this.allRoutes = new RouteDS[routenum + 1];
        for (int i = 1; i <= routenum; i++) {
            this.allRoutes[i] = new RouteDS(this.coachnum, this.seatnum, this.stationnum);
        }
        hashAllot = new ConcurrentHashMap<>(10000);
    }

    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        if (!check(route, departure, arrival))
            return null;
        int[] res = allRoutes[route].buyTicket(departure, arrival);
        if (res != null) {
            Ticket ticket = new Ticket();
            ticket.tid = res[0] * this.routenum + route;
            ticket.coach = res[1];
            ticket.seat = res[2];
            ticket.passenger = passenger;
            ticket.route = route;
            ticket.departure = departure;
            ticket.arrival = arrival;
            hashAllot.put(ticket.tid, true);
            return ticket;
        }
        return null;
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        if (!check(route, departure, arrival))
            return -1;
        return allRoutes[route].inquiry(departure, arrival);
    }

    @Override
    public boolean refundTicket(Ticket ticket) {
        // 存在票才可以退，remove保证了幂等性.
        if (hashAllot.remove(ticket.tid) != null) {
            return allRoutes[ticket.route].refund(ticket, ticket.departure, ticket.arrival);
        }
        return false;
    }

    private boolean check(int route, int departure, int arrival) {
        if (route < 1 || route > this.routenum || departure < 1 || departure >= this.stationnum || arrival <= 1
                || arrival > this.stationnum || arrival <= departure) {
            return false;
        }
        return true;
    }
}
