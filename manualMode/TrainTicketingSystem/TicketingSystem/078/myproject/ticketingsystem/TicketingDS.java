package ticketingsystem;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class TicketingDS implements TicketingSystem {
    int myRoutenum, myCoachnum, mySeatnum, myStationnum, myThreadnum;
    int seatAtrain, seatAll;
    boolean[][] seat;
    int[] tidofseat;
    int[] lastseat;
//    train[] trains;

    ConcurrentHashMap tickets_sold;
    // 为每个座位设置一把锁
    Lock[] locks;


    //保证tid号唯一，所以使用AtomicLong，并且在生成车票tid时使用getAndIncrement()
//    AtomicLong tid_num = new AtomicLong(0);
//    long tid_num;
    TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
        myRoutenum = routenum;
        myCoachnum = coachnum;
        mySeatnum = seatnum;
        myStationnum = stationnum;
        myThreadnum = threadnum;
        //初始化 卖出票的hashmap
        tickets_sold = new ConcurrentHashMap(seatnum * coachnum * routenum);

        //初始化座位
        seatAtrain = coachnum * seatnum;
        lastseat = new int[routenum];

        seatAtrain = coachnum * seatnum;
        seat = new boolean[routenum * coachnum * seatnum][stationnum - 1];

        //初始化座位锁
        seatAll = routenum * coachnum * seatnum;
        locks = new ReentrantLock[seatAll];
        for (int i = 0; i < seatAll; ++i) {
            locks[i] = new ReentrantLock();
        }
        //初始化座位的tid
        tidofseat = new int[routenum * coachnum * seatnum];
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        if ((route > myRoutenum || route <= 0) || (departure <= 0 || departure > myStationnum
        ) || (arrival < 0 || arrival > myStationnum) || departure >= arrival) {
            return 0;
        }
        int ticketRemain = 0;
        int dep = departure - 1;
        int arr = arrival - 1;
        int rou = route - 1;
        int seatonetrain = seatAtrain;

        int fisrtofthetrain = rou * seatonetrain;

        boolean isEmpty;
        for (int i = 0; i < seatonetrain; ++i) {
            isEmpty = true;
            for (int j = dep; j < arr; j++) {
                if (seat[fisrtofthetrain + i][j]) {
                    isEmpty = false;
                    break;
                }
            }
            if (isEmpty) {
                ticketRemain++;
            }
        }

        return ticketRemain;
    }


    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        if ((route > myRoutenum || route <= 0) || (departure <= 0 || departure >= myStationnum
        ) || (arrival <= 0 || arrival > myStationnum) || departure >= arrival) {
            return null;
        }
        //新建一个车票对象

        Ticket ticket = new Ticket();
        ticket.passenger = passenger;
        ticket.route = route;
        ticket.departure = departure;
        ticket.arrival = arrival;


        int dep = departure - 1;
        int arr = arrival - 1;
        int rou = route - 1;
        boolean isEmpty = true;
        int seatonetrain = seatAtrain;

        int fisrtofthetrain = rou * seatonetrain;
        int seattid;
        long ttid;
        //循环查找该route下的所有座位

        int lastSeatofthetrain = lastseat[rou];//上一次这趟车卖出的票的位置

        for (int i = lastSeatofthetrain; i < seatonetrain; ++i) {
            //设置判断座位为空的布尔值
            isEmpty = true;
            //到底是区间还是站的问题
            //查找每个座位在区间内是否都空闲
            for (int j = dep; j < arr; j++) {
                if (seat[fisrtofthetrain + i][j]) {
                    isEmpty = false;
                    break;
                }
            }
            //如果查找到有座位在对应的所有区间都没有人坐，就尝试获得锁
            if (isEmpty) {
                //计算票的车厢和座位号
                ticket.coach = i / mySeatnum + 1;
                ticket.seat = i % mySeatnum + 1;


                //获取锁
                this.locks[fisrtofthetrain + i].lock();
                try {
                    //获取成功后再次检查该座位对应区间内是否被别人买走
                    for (int j = dep; j < arr; j++) {
                        if (seat[fisrtofthetrain + i][j]) {
                            isEmpty = false;
                            break;
                        }
                    }
                    //如果没有被别人买走，并且此时该线程以获得锁，所以可以正式买票
                    if (isEmpty) {

                        //将该座位对应的站点设置为1，表示已有人。
                        for (int j = dep; j < arr; j++) {
                            seat[fisrtofthetrain + i][j] = true;
                        }
                        //获取车票tid
                        seattid = tidofseat[fisrtofthetrain + i]++;
                        ttid = (((long) (fisrtofthetrain + i + 1)) << 32) | seattid;
//                        ticket.tid = (((long)(rou * seatonetrain + i + 1)) << 32 )  | seattid;
                        ticket.tid = ttid;
                        tickets_sold.put(ticket.tid, ticket);
                        if (lastseat[rou] < (seatonetrain - 1)) {
                            lastseat[rou]++;
                        } else {
                            lastseat[rou] = 0;
                        }
                        return ticket;
                    }
                } finally {
                    //如果isEmpty不为true，表示在查找到票到获取锁的这段时间里
                    //有人买了这个座位的这个区间内的票，所以跳出循环查找下一个座位
                    this.locks[fisrtofthetrain + i].unlock();
                }
            }
        }
        for (int i = 0; i < lastSeatofthetrain; ++i) {
            //设置判断座位为空的布尔值
            isEmpty = true;
            //到底是区间还是站的问题
            //查找每个座位在区间内是否都空闲
            for (int j = dep; j < arr; j++) {
                if (seat[fisrtofthetrain + i][j]) {
                    isEmpty = false;
                    break;
                }
            }
            //如果查找到有座位在对应的所有区间都没有人坐，就尝试获得锁
            if (isEmpty) {
                //计算票的车厢和座位号
                ticket.coach = i / mySeatnum + 1;
                ticket.seat = i % mySeatnum + 1;

                //获取锁
                this.locks[fisrtofthetrain + i].lock();
                try {
                    //获取成功后再次检查该座位对应区间内是否被别人买走
                    for (int j = dep; j < arr; ++j) {
                        if (seat[fisrtofthetrain + i][j]) {
                            isEmpty = false;
                            break;
                        }
                    }
                    //如果没有被别人买走，并且此时该线程以获得锁，所以可以正式买票
                    if (isEmpty) {

                        //将该座位对应的站点设置为1，表示已有人。
                        for (int j = dep; j < arr; ++j) {
                            seat[fisrtofthetrain + i][j] = true;
                        }
                        //获取车票tid

                        seattid = tidofseat[fisrtofthetrain + i]++;
                        ticket.tid = (((long) (fisrtofthetrain + i + 1)) << 32) | seattid;
                        tickets_sold.put(ticket.tid, ticket);
                        if (lastseat[rou] < (seatonetrain - 1)) {
                            lastseat[rou]++;
                        } else {
                            lastseat[rou] = 0;
                        }
                        return ticket;
                    }
                } finally {
                    //如果isEmpty不为true，表示在查找到票到获取锁的这段时间里
                    //有人买了这个座位的这个区间内的票，所以跳出循环查找下一个座位
                    this.locks[fisrtofthetrain + i].unlock();
//                this.locksfortrain[route - 1].unlock();
                }
            }
        }
        // 查找了所有座位后都没有票，返回空，表示无票
        return null;
    }

    @Override
    public boolean refundTicket(Ticket ticket) {

        boolean result;
        int seatnumofthetrain = ((ticket.route - 1) * seatAtrain + (ticket.coach - 1) * mySeatnum + ticket.seat - 1);
        int dep = ticket.departure - 1;
        int ari = ticket.arrival - 1;

        result = tickets_sold.remove(ticket.tid, ticket);
        if (result) {
            locks[seatnumofthetrain].lock();
            try {
                for (int i = dep; i < ari; ++i) {
                    seat[seatnumofthetrain][i] = false;
                }
            } finally {
                locks[seatnumofthetrain].unlock();
            }

            return true;
        }
        return false;
    }
    //ToDo

}
