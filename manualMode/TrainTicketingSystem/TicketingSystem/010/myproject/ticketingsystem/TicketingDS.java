package ticketingsystem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Jianyong Feng
 **/
public class TicketingDS implements TicketingSystem {

    // 车次数目
    private int routeNum;

    // 车厢数目
    private int coachNum;

    // 座位数目
    private int seatNum;

    // 车站数目
    private int stationNum;

    // 线程数目
    private int threadNum;

    // 车次列表
    private Route[] routeArray;

    // HashMap线程不安全
    private Map<Long, Ticket> ticketHistory = new ConcurrentHashMap<>();

    public TicketingDS(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {

        this.routeNum = routeNum;
        this.coachNum = coachNum;
        this.seatNum = seatNum;
        this.stationNum = stationNum;
        this.threadNum = threadNum;
        routeArray = new Route[routeNum + 1];

        // 总座位数
        int totalSeatNum = coachNum * seatNum;

        for (int i = 1; i <= routeNum; i++) {
            BitSet[] seatStatus = new BitSet[stationNum];
            for (int j = 1; j < stationNum; j++) {
                seatStatus[j] = new BitSet(totalSeatNum + 1);
            }
            routeArray[i] = new Route(i,coachNum, seatNum, seatStatus);
        }
    }

    /**
     * 购票
     *
     * @param passenger 乘客名字
     * @param route     车次
     * @param departure 出发站编号
     * @param arrival   到达站编号
     * @return 成功：有效的Ticket对象，失败：null
     */
    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        if (route <= 0 || route > this.routeNum || arrival > this.stationNum || departure >= arrival)
            return null;
        Ticket ticket = routeArray[route].trySealTicket(passenger, departure, arrival);
        if (ticket != null) {
            ticketHistory.put(ticket.tid, ticket);
            return ticket;
        }
        return null;
    }

    /**
     * 查询余票
     *
     * @param route     车次
     * @param departure 出发站编号
     * @param arrival   到达站编号
     * @return 余票数量
     */
    @Override
    public int inquiry(int route, int departure, int arrival) {
        if (route <= 0 || route > this.routeNum || arrival > this.stationNum || departure >= arrival)
            return -1;
        return routeArray[route].inquiryTicket(departure, arrival);
    }

    /**
     * 退票
     *
     * @param ticket 车票
     * @return ticket有效返回true，无效返回false
     */
    @Override
    public boolean refundTicket(Ticket ticket) {
        if (ticket == null || ticket.route <= 0 || ticket.route > this.routeNum)
            return false;
        Ticket ticketInMap = ticketHistory.get(ticket.tid);
        if (!twoTicketsEquals(ticketInMap, ticket))
            return false;
        boolean refundSuccess = routeArray[ticket.route].tryRefundTicket(ticket);
        if (refundSuccess) {
            ticketHistory.remove(ticket.tid);
            return true;
        }
        return false;
    }

    public boolean twoTicketsEquals(Ticket ticket1, Ticket ticket2) {
        if (ticket1 == ticket2) return true;
        if (ticket1 == null || ticket2 == null)
            return false;
        return ticket1.tid == ticket2.tid &&
                ticket1.route == ticket2.route &&
                ticket1.coach == ticket2.coach &&
                ticket1.seat == ticket2.seat &&
                ticket1.departure == ticket2.departure &&
                ticket1.arrival == ticket2.arrival &&
                ticket1.passenger.equals(ticket2.passenger);
    }

    public int getRouteNum() {
        return routeNum;
    }

    public int getCoachNum() {
        return coachNum;
    }

    public int getSeatNum() {
        return seatNum;
    }
}
