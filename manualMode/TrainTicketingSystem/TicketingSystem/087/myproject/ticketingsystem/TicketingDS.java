

package ticketingsystem;

import java.util.*;

public class TicketingDS implements TicketingSystem {
		
    private final List<Route> allRoutes;
    private final int route_num;
    private final int coach_num;
    private final int station_num;
    
    public TicketingDS(int route_num, int coach_num, 
        int countOfSeat, int station_num, int countOfThread) {
        this.route_num = route_num;
        this.coach_num = coach_num;
        this.station_num = station_num;
        
        this.allRoutes = new ArrayList<Route>();
        for (int i = 0; i < route_num; i++) {
            this.allRoutes.add(new Route(i + 1, station_num,
                coach_num, countOfSeat));
        }
    }
    
    public Ticket buyTicket(String name,
        int route, int departure, int arrival) {
            
        if (departure <= 0 && arrival > this.station_num)
            return null;
        
        return this.allRoutes.get(route - 1)
            .trySeal(name, departure, arrival);
    }
    

    
    public boolean refundTicket(Ticket ticket) {
        
        final int route = ticket.route;
        return this.allRoutes.get(route - 1)
            .tryRefund(ticket);
    }

    public int inquiry(int route,
        int departure, int arrival) {
            
        if (departure <= 0 && arrival > this.station_num)
            return 0;
        
        return this.allRoutes.get(route - 1)
            .checkFreeSeat(departure, arrival);
    }

}
