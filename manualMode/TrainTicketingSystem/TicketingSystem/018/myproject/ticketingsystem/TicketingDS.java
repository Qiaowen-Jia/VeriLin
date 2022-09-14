package ticketingsystem;

import java.util.concurrent.ConcurrentHashMap;

class TicketingDS implements TicketingSystem {
    public int route_num;
    public int coach_num;
    public int seat_num;
    public int station_num;
    public int thread_num;
    public RouteManager[] managers;
    public ConcurrentHashMap<Integer, Ticket> ticket_sold1;
    public ConcurrentHashMap<Integer, Ticket> ticket_sold2;

    TicketingDS()
    {
        route_num = 5;
        coach_num = 8;
        seat_num = 100;
        thread_num = 16;
        station_num = 10;
        managers = new RouteManager[route_num];
        for(int i=0;i<route_num;i++)
            managers[i] = new RouteManager(i+1,coach_num,seat_num,station_num);
        ticket_sold1 = new ConcurrentHashMap<Integer, Ticket>();
        ticket_sold2 = new ConcurrentHashMap<>();
    }

    TicketingDS(int route_num, int coach_num, int seat_num,
                int station_num, int thread_num)
    {
        this.route_num = route_num;
        this.coach_num = coach_num;
        this.seat_num = seat_num;
        this.thread_num = thread_num;
        this.station_num = station_num;
        managers = new RouteManager[route_num];
        for(int i=0;i<route_num;i++)
            managers[i] = new RouteManager(i+1,coach_num,seat_num,station_num);
	    ticket_sold1 = new ConcurrentHashMap<Integer, Ticket>();
	    ticket_sold2 = new ConcurrentHashMap<>();
    }
    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
	if(!check(route,departure,arrival))
            return null;
	Ticket ticket = managers[route-1].buyTicket(passenger, departure, arrival);
	if(ticket != null)
	    if(ticket.tid % 2 == 0)
	        ticket_sold1.put(ticket.tid,ticket);
	    else
	        ticket_sold2.put(ticket.tid,ticket);
        return ticket;
    }
    @Override
    public int inquiry(int route, int departure, int arrival) {
	if(!check(route,departure,arrival))
            return 0;
        return managers[route-1].inquiry(departure,arrival);
    }
    @Override
    public boolean refundTicket(Ticket ticket) {
	if(ticket == null)
            return false;

        if(ticket.tid % 2 == 0) {
            if(ticket_sold1.remove(ticket.tid, ticket))
            {
                managers[ticket.route-1].refundTicket(ticket);
                return true;
            }
            else
                return false;
        }
        else {
            if(ticket_sold2.remove(ticket.tid, ticket))
            {
                managers[ticket.route-1].refundTicket(ticket);
                return true;
            }
            else
                return false;
        }
    }

    public boolean check(int route,int departure,int arrival)
    {
        return (route>=1 && route<=route_num) &&
               (departure>=1 && departure<=(station_num-1)) &&
                (arrival>departure && arrival<=station_num);
    }
}
