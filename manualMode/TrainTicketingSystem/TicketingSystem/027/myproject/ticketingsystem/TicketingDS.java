package ticketingsystem;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 965087276@qq.com
 * @date 2019/12/8 16:09
 */
public class TicketingDS implements TicketingSystem {
    /**
     * 车次数组
     */
    private Route[] routes;
    /**
     * 车次数目
     */
    private int routeNum;
    /**
     * 每车的车厢数目
     */
    private int coachNum;
    /**
     * 每车厢的座位数目
     */
    private int seatNum;
    /**
     * 经停站数目
     */
    private int stationNum;
    /**
     * 线程数目
     */
    private int threadNum;

    /**
     * 全局计数器
     */
    private AtomicInteger nextId;
    /**
     * 已售出的车票
     */
    private ConcurrentHashMap<Integer, Ticket> soldTickets;

    public TicketingDS(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
        this.routeNum = routeNum;
        this.coachNum = coachNum;
        this.seatNum = seatNum;
        this.stationNum = stationNum;
        this.threadNum = threadNum;
        this.routes = new Route[this.routeNum];
        this.nextId = new AtomicInteger(1);
        this.soldTickets = new ConcurrentHashMap<>();
        for (int i = 0; i < this.routeNum; i++) {
            this.routes[i] = new Route(i, this.coachNum, this.seatNum, this.stationNum);
        }
    }
    /**
     * 购票
     *
     * @param passenger 乘客名
     * @param route     车次
     * @param departure 上车站
     * @param arrival   下车站
     * @return 有票返回车票，无票返回null
     */
    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        if (!(route >= 1 && route <= routeNum && departure >= 1 && departure < stationNum && arrival > departure && arrival <= stationNum)) {
            return null;
        }
        Ticket ticket = routes[route-1].buyTicket(departure, arrival);
        if (ticket != null) {
            ticket.setTid(this.nextId.getAndIncrement());
            ticket.setPassenger(passenger);
            ticket.setDeparture(departure);
            ticket.setArrival(arrival);
            ticket.setRoute(route);
            soldTickets.put(ticket.getTid(), ticket);
        }
        return ticket;
    }

    /**
     * 退票
     *
     * @param ticket 车票
     * @return 车票合法时应返回true，其他条件返回false
     */
    @Override
    public boolean refundTicket(Ticket ticket) {

        if (ticket == null) return false;

        Ticket oldTicket = soldTickets.get(ticket.getTid());
        if (oldTicket == null || !oldTicket.equals(ticket)) {
            return false;
        }
        // 已经被其它线程退票
        if ((ticket = soldTickets.remove(oldTicket.getTid())) == null) {
            return false;
        }
        // 只有一个线程可以走到这一步
        int seatId = (oldTicket.getCoach()-1) * this.seatNum + oldTicket.getSeat() - 1;
        boolean status = routes[ticket.getRoute()-1].refundTicket(seatId, ticket.getDeparture(), ticket.getArrival());
        // 讲道理这一步一定可以退票成功的
        if (status) {
            return true;
        }
        else {
            System.out.println("unknown error! ticket is " + oldTicket);
            return false;
        }
    }

    /**
     * 查询余票数量
     *
     * @param route     车次
     * @param departure 上车站
     * @param arrival   下车站
     * @return 返回余票数
     */
    @Override
    public int inquiry(int route, int departure, int arrival) {
        if (!(route >= 1 && route <= routeNum && departure >= 1 && departure < stationNum && arrival > departure && arrival <= stationNum)) {
            return 0;
        }
        return routes[route-1].inquiry(departure, arrival);
    }
}
