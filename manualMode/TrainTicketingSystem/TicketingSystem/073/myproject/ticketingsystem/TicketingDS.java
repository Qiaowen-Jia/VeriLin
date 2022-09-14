package ticketingsystem;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;






public class TicketingDS implements TicketingSystem {


    private final int routeNum;
    private final int stationNum;
    private ArrayList<RouteNode> routeList;


    public TicketingDS(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
        this.routeNum = routeNum;
        this.stationNum = stationNum;

        this.routeList = new ArrayList<RouteNode>(routeNum);
        for (int routeId = 1; routeId <= routeNum; routeId++)//routeId从1开始
            this.routeList.add(new RouteNode(routeId, coachNum, seatNum));
    }

    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        // 确保参数合法
        if (route <=0 || route > this.routeNum || arrival > this.stationNum || departure >= arrival)
            return null;
        // 去指定车次获取座位
        return this.routeList.get(route-1).trySealTic(passenger, departure, arrival);
    }

    public int inquiry(int route, int departure, int arrival) {
        if (route <=0 || route > this.routeNum || arrival > this.stationNum || departure >= arrival)
            return -1;
        return this.routeList.get(route-1).inquiryTic(departure, arrival);
    }

    public boolean refundTicket(Ticket ticket) {
        final int routeId = ticket.route;
        if (ticket == null || routeId <=0 || routeId > this.routeNum)
            return false;
        return this.routeList.get(routeId-1).tryRefundTic(ticket);
    }
}
