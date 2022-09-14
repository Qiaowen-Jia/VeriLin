package ticketingsystem;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class TicketingDS implements TicketingSystem {

    private int routenum = 5;        //列车 车次数
    private int coachnum = 8;        //车厢 数
    private int seatnum = 100;       //座位号 数量
    private int stationnum = 10;     //车次经停站的数量
    private int threadnum = 16;

    private RouteDS[] routes;      //车次-数据结构
    private static ConcurrentHashMap<Ticket, Boolean> tidHash;    //票号-哈希
    private AtomicInteger tidCounter;    //票号-累加


    private void init() {
        this.routes = new RouteDS[this.routenum + 1];
        for (int i = 1; i <= this.routenum; i++) {
            this.routes[i] = new RouteDS(this.coachnum, this.seatnum, this.stationnum);
        }
        tidHash = new ConcurrentHashMap<>(5000000);
        tidCounter = new AtomicInteger(0);
    }


    public TicketingDS() {
        init();
    }

    public TicketingDS(int newRoutenum, int newCoachnum, int newSeatnum, int newStationnum, int newThreadnum) {
        this.routenum = newRoutenum;
        this.coachnum =  newCoachnum;
        this.seatnum =  newSeatnum;
        this.stationnum = newStationnum;
        this.threadnum = newThreadnum;
        init();
    }


    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        if (!validCheck(route, departure, arrival)) return null;
        int[] coachANDseat = routes[route].buyTicket(departure, arrival);
        if (coachANDseat != null) {
            return makeTicket(passenger, route, departure, arrival, coachANDseat[0], coachANDseat[1]);
        } else {
            return null;
        }
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        if (!validCheck(route, departure, arrival)) {
            return -1;
        } else {
            return routes[route].inquiry(departure, arrival);
        }
    }

    @Override
    public boolean refundTicket(Ticket ticket) {
        if (tidHash.remove(ticket) == true) {
            if(routes[ticket.route].refundTicket(ticket) ) {
                tidHash.put(ticket, false);
                return true;
            } else {
                tidHash.put(ticket, true);
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean validCheck(int route, int departure, int arrival) {
        if (route <= 0 || route > this.routenum || departure <= 0  
            || departure > this.stationnum || arrival <= 0 
            || arrival > this.stationnum || departure >= arrival)
            return false;
        return true;   
    }

    private Ticket makeTicket(String passenger, int route, int departure, int arrival,int coach, int seat) {
        Ticket ticket = new Ticket();
        ticket.tid = tidCounter.incrementAndGet();
        ticket.coach = coach;
        ticket.seat = seat;
        ticket.passenger = passenger;
        ticket.route = route;
        ticket.departure = departure;
        ticket.arrival = arrival;
        tidHash.put(ticket, true);
        return ticket;
    }


}
