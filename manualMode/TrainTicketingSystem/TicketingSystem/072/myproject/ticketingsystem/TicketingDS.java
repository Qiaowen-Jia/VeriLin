package ticketingsystem;

public class TicketingDS implements TicketingSystem {
    int routeNum;
    int coachNum;
    int seatNum;
    int stationNum;
    int threadNum;
    long[] rangeMask;

    private Route[] routes;

    private void generateRangeMask(int stationNum) {
        rangeMask = new long[stationNum * stationNum];

        for (int i = 0; i < stationNum - 1; ++i) {
            long mask = (1 << i);
            for (int j = i + 1; j <= stationNum - 1; ++j) {
                mask |= (1 << (j - 1));
                rangeMask[i * stationNum + j] = mask;
            }
        }
    }

    TicketingDS() {
        this.routeNum = 5;
        this.coachNum = 8;
        this.seatNum = 100;
        this.stationNum = 10;
        this.threadNum = 16;

        generateRangeMask(this.stationNum);

        this.routes = new Route[routeNum];
        for (int i = 0; i < routeNum; ++i) {
            routes[i] = new Route(i + 1, coachNum, seatNum, stationNum, rangeMask);
        }
    }

    TicketingDS(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
        this.routeNum = routeNum;
        this.coachNum = coachNum;
        this.seatNum = seatNum;
        this.stationNum = stationNum;
        this.threadNum = threadNum;
        
        generateRangeMask(this.stationNum);

        this.routes = new Route[routeNum];
        for (int i = 0; i < routeNum; ++i) {
            routes[i] = new Route(i + 1, coachNum, seatNum, stationNum, rangeMask);
        }
    }

    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        if (route <= 0 || route > this.routeNum || arrival > this.stationNum || departure >= arrival) {
            return null;
        }

        return routes[route - 1].buyTicket(passenger, departure, arrival);
    }

    public int inquiry(int route, int departure, int arrival) {
        if (route <= 0 || route > this.routeNum || arrival > this.stationNum || departure >= arrival) {
            return 0;
        }
        return routes[route - 1].inquiry(departure, arrival);
    }

    public boolean refundTicket(Ticket ticket) {
        if (ticket == null || ticket.route <= 0 || ticket.route > this.routeNum || ticket.departure >= ticket.arrival) {
            return false;
        }
        return routes[ticket.route - 1].refundTicket(ticket);
    }
}
