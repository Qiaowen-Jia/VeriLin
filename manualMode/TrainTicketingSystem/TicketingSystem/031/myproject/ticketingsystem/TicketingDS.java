package ticketingsystem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TicketingDS implements TicketingSystem {
    public int routenum;
    public int coachnum;
    public int seatnum;
    public int stationnum;
    public int threadnum;

    //用于存储不同车次列车，车次从1开始到routenum
    public Route[] routes;
    //不同车次列车的锁，分别用于买票和退票同步
    private Object[] routeBuyLocks;
    private Object[] routeRefundLocks;
//    private final static ReadWriteLock rwLock = new ReentrantReadWriteLock();
//    private final static Lock readLock = rwLock.readLock();
//    private final static Lock writeLock = rwLock.writeLock();
    //买票退票的锁，用于卖票保存到hasSold操作和退票操作同步，防止卖票已保存未返回前就被退掉
//    private Object[] saveLocks;

    //用于保存已卖出的票
    private final static Map<Long, Ticket> hasSold = new ConcurrentHashMap<>();

    //用于生成车票id
    public final static AtomicLong idGenerator = new AtomicLong(1);

    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
        this.routenum = routenum;
        this.coachnum = coachnum;
        this.seatnum = seatnum;
        this.stationnum = stationnum;
        this.threadnum = threadnum;
        routes = new Route[routenum];
        routeBuyLocks = new Object[routenum];
        routeRefundLocks = new Object[routenum];
//        saveLocks = new Object[routenum];
        for (int i = 0; i < routenum; i++) {
            routes[i] = new Route(stationnum, coachnum, seatnum);
            routeBuyLocks[i] = new Object();
            routeRefundLocks[i] = new Object();
//            saveLocks[i] = new Object();
        }
    }

    /**
     * 买票方法
     * @param passenger 乘客名字
     * @param route 线路号
     * @param departure 起点站号
     * @param arrival 终点站号
     * @return 成功返回买到的ticket对象，并保存到hasSold中，失败返回null
     */
    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        //参数不合法（如终点小于起点等）返回null
        if (route < 1 || route > this.routenum || departure < 1 || departure > this.stationnum || arrival <= departure || arrival > this.stationnum) {
            return null;
        }
        int sec = (((this.stationnum - 1) + (this.stationnum - (departure - 1))) * (departure - 1) / 2) + (arrival - departure) - 1;
        //无票返回null
        if (this.routes[route - 1].rest[sec].get() <= 0)
            return null;
        synchronized (routeBuyLocks[route - 1]) {
            Route r = this.routes[route - 1];
            Seat[] seats = r.seats;
            //进入临界区再次判断是否有票，可能被别的线程先买走
            if (r.rest[sec].get() <= 0)
                return null;
            //未返回说明有余票
            int seatNo = 0;
            for (int i = 0; i < seats.length; i++) {
                if (seats[i].section[sec].get() == 0) {
                    seatNo = i;
                    break;
                }
            }
            //修改卖出票的区间及相关区间占用数和余票数，加锁，防止查询线程多次查询出现后查的查到买票前状态而先查的查到买票后状态，确保可线性化
            r.writeLock.lock();
            for (int i = 1; i < this.stationnum; i++) {
                for (int j = i + 1; j <= this.stationnum; j++) {
                    if (!(i >= arrival || j <= departure)) {    //判断是否为相关区间
                        int s = (((this.stationnum - 1) + (this.stationnum - (i - 1))) * (i - 1) / 2) + (j - i) - 1;    //映射到数组相应位置
//                        synchronized (r.restLocks[s]) {
                        if (seats[seatNo].section[s].getAndIncrement() == 0) {
                            //如果此区间占用由0修改为非0，则此区间余票减1
                            r.rest[s].getAndDecrement();
                        }
//                        }
                    }
                }
            }
            r.writeLock.unlock();
            //生成车票 long tid, String passenger, int route, int coach, int seat, int departure, int arrival
            Ticket ticket = new Ticket();
            ticket.tid = idGenerator.getAndIncrement();
            ticket.passenger = passenger;
            ticket.route = route;
            ticket.coach = seatNo / this.seatnum + 1;
            ticket.seat = seatNo % this.seatnum + 1;
            ticket.departure = departure;
            ticket.arrival = arrival;
//            synchronized (this.saveLocks[route - 1]){
            hasSold.put(ticket.tid, ticket);
            return ticket;
//            }
        }
    }

    /**
     * 查询余票方法
     * @param route 车次号
     * @param departure 起始站
     * @param arrival 终点站
     * @return 返回查询区间余票数量
     */
    @Override
    public int inquiry(int route, int departure, int arrival) {
        if (route < 1 || route > this.routenum || departure < 1 || departure > this.stationnum || arrival <= departure || arrival > this.stationnum) {
            return 0;  //参数错误，没票
        }
        int sec = (((this.stationnum - 1) + (this.stationnum - (departure - 1))) * (departure - 1) / 2) + (arrival - departure) -1;
//        synchronized (this.routes[route - 1].inqAndBuyLock) {
//            synchronized (this.routes[route - 1].inqAndRefLock) {
        try {
            this.routes[route - 1].readLock.lock();
            return routes[route - 1].rest[sec].get();
        } finally {
            this.routes[route - 1].readLock.unlock();
        }
    }

    /**
     * 退票方法
     * @param ticket 要退的票对象
     * @return 成功返回true，失败返回false
     */
    @Override
    public boolean refundTicket(Ticket ticket) {
        //先检查要退的票的合法性，不合法则返回false

//        synchronized (saveLocks[ticket.route - 1]) {
        Ticket t = hasSold.get(ticket.tid);
//        }
        if (ticket != t) {
            return false;
        }
        synchronized (routeRefundLocks[ticket.route - 1]) {
//            synchronized (saveLocks[ticket.route - 1]) {
                //进入临界区再次检查合法性，因为可能已经被别的线程退了
                t = hasSold.get(ticket.tid);
                if (ticket != t) {
                    return false;
                }
                //将票从已卖出Map中移除
                hasSold.remove(ticket.tid);
//            }
            Route r = this.routes[ticket.route - 1];
            Seat[] seats = r.seats;
            //修改退票区间及相关区间占用数和余票数，加锁，防止查询线程多次查询后查的查到退票前状态而先查的查到退票后状态
//            synchronized (r.inqAndRefLock) {
            r.writeLock.lock();
            for (int i = 1; i < this.stationnum; i++) {
                for (int j = i + 1; j <= this.stationnum; j++) {
                    if (!(i >= ticket.arrival || j <= ticket.departure)) {   //判断是否为相关区间
                        int s = (((this.stationnum - 1) + (this.stationnum - (i - 1))) * (i - 1) / 2) + (j - i) - 1;
//                        synchronized (r.restLocks[s]) {
                            int seatNo = (ticket.coach - 1) * this.seatnum + (ticket.seat - 1);
//                            int holdNum = seats[seatNo].section[s].decrementAndGet();
                            if (seats[seatNo].section[s].decrementAndGet() == 0) {
                                //如果此区间占用修改后变为0，则此区间余票加1
                                r.rest[s].getAndIncrement();
                            }
//                        }
                    }
                }
            }
            r.writeLock.unlock();
//            }
            return true;
        }
    }

    //ToDo

}
