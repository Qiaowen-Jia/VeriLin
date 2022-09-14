package ticketingsystem;
import java.util.concurrent.ConcurrentHashMap;

public class TicketingDS implements TicketingSystem {
	private int routeNum = 5,coachNum = 8,seatNum = 100,stationNum = 10,threadNum = 16;
	    public Route[] routes;
	    public static ConcurrentHashMap<Long, Boolean> ConHashMap;

	    public TicketingDS(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
	        this.routeNum = routeNum;
	        this.coachNum = coachNum;
	        this.seatNum =  seatNum;
	        this.stationNum =stationNum;
	        this.threadNum =threadNum;
	        this.routes = new Route[routeNum + 1];
	        for (int i = 1; i <= routeNum; i++) {
	            this.routes[i] = new Route(this.coachNum, this.seatNum, this.stationNum);
	        }
	        ConHashMap = new ConcurrentHashMap<>(200000);
	    }

	    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
	        if (!legal(route, departure, arrival)) return null;
	        int[] r = routes[route].buyTicket(departure, arrival);
	        if (r!= null) {
	            Ticket ticket = new Ticket();
	            ticket.tid = r[0] * this.routeNum + route;
	            ticket.coach = r[1];
	            ticket.seat =r[2];
	            ticket.passenger = passenger;
	            ticket.route = route;
	            ticket.departure = departure;
	            ticket.arrival = arrival;
	            ConHashMap.put(ticket.tid, true);
	            return ticket;
	        }
	        return null;
	    }


	    public int inquiry(int route, int departure, int arrival) {
	        if (legal(route, departure, arrival)) 
	        	return routes[route].inquiry(departure, arrival);
	        return -1;
	    }


	    public boolean refundTicket(Ticket ticket) {
	        if (ConHashMap.remove(ticket.tid) != null) {
	            return routes[ticket.route].refund(ticket, ticket.departure, ticket.arrival);
	        }
	        return false;
	    }

	    private boolean legal(int route, int departure, int arrival) {
	        if (route < 1 || route > this.routeNum|| departure < 1 || departure >= this.stationNum|| arrival <= 1 || arrival > this.stationNum || arrival < departure) {
	            return false;
	        }
	        return true;
	    }
	}
