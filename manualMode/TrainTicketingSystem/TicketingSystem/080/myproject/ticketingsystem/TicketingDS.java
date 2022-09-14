package ticketingsystem;


import java.util.ArrayList;









public class TicketingDS implements TicketingSystem{

    //ToDo
/*
    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
        this.threadNum = threadnum;
        this.coachNum = coachnum;
        this.seatNum = seatnum;
    }
*/
    private final int RouteNum;
    private final int StationNum;
    private ArrayList<Route> routeList;

    public TicketingDS(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
        this.RouteNum = routeNum;
        this.StationNum = stationNum;

        this.routeList = new ArrayList<Route>(routeNum);
        for (int routeId = 1; routeId <= routeNum; routeId++)
            this.routeList.add(new Route(routeId, coachNum, seatNum));
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        if (route <=0 || route > this.RouteNum || arrival > this.StationNum || departure >= arrival)
            return -1;
        return this.routeList.get(route-1).inquiry(departure, arrival);
    }

    @Override
    public boolean refundTicket(Ticket ticket) {
        final int routeId = ticket.route;
        if (ticket == null || routeId <=0 || routeId > this.RouteNum)
            return false;
        return this.routeList.get(routeId-1).refundTicket(ticket);
    }

    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        if (route <=0 || route > this.RouteNum || arrival > this.StationNum || departure >= arrival)
            return null;
        return this.routeList.get(route-1).buyTicket(passenger, departure, arrival);
    }


}
