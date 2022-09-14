package ticketingsystem;
class TicketID {

    static int threadNum;

    public static void setThreadNum(int n) {
        threadNum = n;
    }

    private static ThreadLocal<Long> ticketId = new ThreadLocal<Long>() {
        @Override
        protected Long initialValue() {
            return Long.valueOf(ThreadId.get());
        }};

    public static long getTicketId()
    {
        long currTicketId = ticketId.get().longValue();
        ticketId.set(Long.valueOf(currTicketId + threadNum));
        return currTicketId;
    }
}
public class TicketingDS implements TicketingSystem {

    private int routeNum;
    private int coachNum;
    private int seatNum;
    private int stationNum;
    private int threadNum;
    private Route[] routes;
    public TicketingDS(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
        this.routeNum = routeNum;
        this.coachNum = coachNum;
        this.seatNum = seatNum;
        this.stationNum = stationNum;
        this.threadNum = threadNum;
        routes = new Route[routeNum + 1];
        TicketID.setThreadNum(threadNum);
        for (int i = 1; i <= routeNum; ++i) {
            routes[i] = new Route(i, coachNum, seatNum, stationNum);
        }
    }

    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        if (passenger != null && route <= routeNum && departure < arrival) {
           return routes[route].buyTicket(passenger, route, departure, arrival);
        } else {
            //System.out.println("所买车票不符合规范，请核对后重新提交购票信息");
            return null;
        }

    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        if (route <= routeNum && departure < arrival) {
            return routes[route].inquiry(route, departure, arrival);
        } else {
            //System.out.println("所查询车票不符合规范，请核对后重新提交购票信息");
            return 0;
        }
    }

    @Override
    public boolean refundTicket(Ticket ticket) {
        if (ticket != null && ticket.route <= routeNum) {
            return routes[ticket.route].refundTicket(ticket);
        } else {
            //System.out.println("所退车票不符合规范，请核对后重新提交购票信息");
            return false;
        }
    }

}
