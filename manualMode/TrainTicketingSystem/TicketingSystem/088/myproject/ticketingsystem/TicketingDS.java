 

package ticketingsystem;

import java.util.*;

public class TicketingDS implements TicketingSystem {
		
    private final List<Route> allRoutes;
    private final int countOfRoute;
    private final int countOfCoach;
    private final int countOfStation;
    
    public TicketingDS(int countOfRoute, int countOfCoach, 
        int countOfSeat, int countOfStation, int countOfThread) {
        
        // Even check the count of station is import,
        // but for test, we will not use it.
        /*
        if (countOfStation > 32) {
            throw new Exception();
        } */
        
        this.countOfRoute = countOfRoute;
        this.countOfCoach = countOfCoach;
        this.countOfStation = countOfStation;
        
        this.allRoutes = new ArrayList<Route>();
        for (int i = 0; i < countOfRoute; i++) {
            this.allRoutes.add(new Route(i + 1, countOfStation,
                countOfCoach, countOfSeat));
        }
    }
    
    public Ticket buyTicket(String name,
        int route, int departure, int arrival) {
            
        if (departure <= 0 && arrival > this.countOfStation)
            return null;
        
        return this.allRoutes.get(route - 1)
            .trySeal(name, departure, arrival);
    }
    
    public int inquiry(int route,
        int departure, int arrival) {
            
        if (departure <= 0 && arrival > this.countOfStation)
            return 0;
        
        return this.allRoutes.get(route - 1)
            .checkFreeSeat(departure, arrival);
    }
    
    public boolean refundTicket(Ticket ticket) {
        
        final int route = ticket.route;
        return this.allRoutes.get(route - 1)
            .tryRefund(ticket);
    }

}
