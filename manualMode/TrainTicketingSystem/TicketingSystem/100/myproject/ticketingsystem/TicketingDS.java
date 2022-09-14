package ticketingsystem;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class TicketingDS implements TicketingSystem {
    //ToDo
    private int routenum;
    private int coachnum;
    private int seatnum;
    private int stationnum;
    private int threadnum;
    private Routes [] routes;
    private AtomicLong tid;
    ConcurrentHashMap<Long,Ticket> soldTickets;
    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum){
        this.routenum=routenum;
        this.coachnum=coachnum;
        this.seatnum=seatnum;
        this.stationnum=stationnum;
        this.threadnum=threadnum;
        this.routes=new Routes[routenum+1];
        for(int i=1;i<=routenum;i++){
            routes[i]=new Routes(coachnum,seatnum,stationnum);
        }
        this.tid=new AtomicLong(1);
        this.soldTickets=new ConcurrentHashMap<>();
    }

    public Ticket buyTicket(String passenger, int route, int departure, int arrival){
        if(route>routenum||route<1){
            return null;
        }
        if(departure<1||departure>stationnum){
            return null;
        }
        if(arrival>stationnum||arrival<1){
            return null;
        }
        if(arrival==departure){
            return null;
        }
        if(inquiry(route, departure, arrival)>0){
            Ticket ticket=routes[route].occupyRoutes(departure,arrival);
            if(ticket!=null){
                ticket.passenger=passenger;
                ticket.tid=tid.getAndAdd(1);
                ticket.route=route;
                ticket.departure=departure;
                ticket.arrival=arrival;
                soldTickets.put(ticket.tid,ticket);
                return ticket;
            }
        }
        return null;
    }

    public int inquiry(int route, int departure, int arrival){
        if(route>routenum||route<1){
            return 0;
        }
        if(departure<1||departure>stationnum){
            return 0;
        }
        if(arrival>stationnum||arrival<1){
            return 0;
        }
        if(arrival==departure){
            return 0;
        }
        return routes[route].queryRoutes(departure,arrival);
    }

    public boolean refundTicket(Ticket ticket){
        if(ticket.route>routenum||ticket.route<1){
            return false;
        }
        if(ticket.departure<1||ticket.departure>stationnum){
            return false;
        }
        if(ticket.arrival>stationnum||ticket.arrival<1){
            return false;
        }
        if(ticket.arrival==ticket.departure){
            return false;
        }
        if(ticket.seat<1||ticket.seat>seatnum){
            return false;
        }
        if(ticket.coach<1||ticket.coach>coachnum){
            return false;
        }
        Ticket now=soldTickets.get(ticket.tid);
        if(!ticket.passenger.equals(now.passenger)){
            return false;
        }
        if(ticket.route!=now.route){
            return false;
        }
        if(ticket.coach!=now.coach){
            return false;
        }
        if(ticket.seat!=now.seat){
            return false;
        }
        if(ticket.departure!=now.departure){
            return false;
        }
        if(ticket.arrival!=now.arrival){
            return false;
        }
        boolean i = routes[ticket.route].releaseRoutes(ticket.departure,ticket.arrival,ticket.coach,ticket.seat);
        if (i) {
            soldTickets.remove(ticket.tid);
        }
        return i;
    }
}
