package ticketingsystem;

import java.util.concurrent.locks.*;

public class TicketingDS implements TicketingSystem {

    // private final Lock lock = new ReentrantLock();
    // private RouteManager [] routes;
    private RouteManagerBitmap [] routes;
    private int routenum;

    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
        routes = new RouteManagerBitmap[routenum];
        for(int i = 0; i < routenum; i++ ) {
            routes[i] = new RouteManagerBitmap(coachnum, seatnum, stationnum, i);
        }
        this.routenum = routenum;
    }

    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        LogUtil.Log(String.format("buyTicket begin: passenger:%s route:%d departure:%d arrival:%d",
            passenger, route, departure, arrival));

        if (departure == arrival) {
            assert false: "Error: departure is equal to arrival, please check your Tracer";
            return null;
        }

        Ticket ticket;
        if((ticket = routes[route-1].buyTicket(passenger, departure, arrival)) != null) {
            ticket.route = route;
            LogUtil.Log(String.format("buyTicket end: tid:%d passenger:%s route:%d coach:%d seat:%d",
                ticket.tid, ticket.passenger, ticket.route, ticket.coach, ticket.seat));
            return ticket;
        };

        LogUtil.Log(String.format("buyTicket end: failed"));
        return null;
    }

    public int inquiry(int route, int departure, int arrival) {
        LogUtil.Log(String.format("inquiry begin: route:%d departure:%d arrival:%d", route, departure, arrival));
        int rest = routes[route-1].inquiry(departure, arrival);
        LogUtil.Log(String.format("inquiry end: %d", rest));
        return rest;
    }

    public boolean refundTicket(Ticket ticket) {
        LogUtil.Log(String.format("refundTicket tid:%d, passenger:%s route:%d coach:%d seat:%d departure:%d arrival:%d",
            ticket.tid, ticket.passenger, ticket.route, ticket.coach, ticket.seat, ticket.departure, ticket.arrival));
        boolean res = routes[ticket.route-1].refundTicket(ticket);
        LogUtil.Log(String.format("refundTicket: %s", res ? "successfully" : "failed"));

        return res;
    }
}