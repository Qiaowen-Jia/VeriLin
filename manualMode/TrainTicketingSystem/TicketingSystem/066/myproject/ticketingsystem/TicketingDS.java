package ticketingsystem;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class TicketingDS implements TicketingSystem {
    int routenum;//车次数
    int coachnum;//车厢数
    int seatnum;//每个车厢座位数
    int stationnum;//车停站数
    int threadnum;//线程数
    AtomicLong id;//全局唯一的tid
    volatile Seat[][][] seats;
    volatile Map<Long,Boolean> map;//有效的tid，本来想用HashSet，但JUC里没有Set于是用了ConcurrentHashMap
    class Seat {//Seat内部类
        boolean[] station;//标记经过的站点，true为站点有人占了
        Seat() {
            station = new boolean[stationnum + 1];
        }
        private boolean reader(int departure,int arrival){//读站点
            for (int i = departure; i < arrival; i++)
                if (station[i])
                    return false;
            return true;
        }

        public synchronized void writer(int departure,int arrival,boolean value) {//加锁写站点
            for (int i = departure; i < arrival; i++){
                station[i] = value;
            }
        }

        public boolean buy(int departure,int arrival) {//买票
            /*读一次如果起点到终点没有位置了，直接返回false
             * 如果有位置，需要给Seat加锁再读一次，如果还没问题，直接写成功
             * 如果加锁后发现有人比自己快，先占了座位，就相当于白做了，再去找其他位置
             * */
            if (reader(departure,arrival)){
                synchronized(this){
                    if (reader(departure,arrival)){
                        writer(departure,arrival,true);
                        return true;
                    }else
                        return false;
                }
            }
            return false;
        }
    }

    TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
        this.routenum = routenum;
        this.coachnum = coachnum;
        this.seatnum = seatnum;
        this.stationnum = stationnum;
        this.threadnum = threadnum;
        id = new AtomicLong(1);//tid从1开始
        map = new ConcurrentHashMap<>();
        seats = new Seat[routenum + 1][coachnum + 1][seatnum + 1];

        for (int i = 1; i <= routenum; i++) {//这里都从1开始，以便后面不用再+1
            for (int j = 1; j <= coachnum; j++) {
                for (int k = 1; k <= seatnum; k++)
                    seats[i][j][k] = new Seat();
            }
        }
    }

    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        for (int i = 1; i <= coachnum; i++) {
            for (int j = 1; j <= seatnum; j++) {
                Seat seat = seats[route][i][j];
                if (seat.buy(departure,arrival)){//成功竞争到writer
                    Ticket ticket = new Ticket();
                    ticket.tid = id.getAndIncrement();//对应上面的初始值1
                    ticket.passenger = passenger;
                    ticket.route = route;
                    ticket.coach = i;
                    ticket.seat = j;
                    ticket.departure = departure;
                    ticket.arrival = arrival;
                    map.put(ticket.tid,true);
                    //System.out.println("tid : " + ticket.tid);
                    return ticket;
                }
            }
        }
        return null;
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {//静态一致性，遍历一遍所有的座位
        int res = 0;
        for (int i = 1; i <= coachnum; i++) {
            for (int j = 1; j <= seatnum; j++) {
                if (seats[route][i][j].reader(departure,arrival))
                    res ++;
            }
        }
        return res;
    }

    @Override
    public boolean refundTicket(Ticket ticket) {
        //if (!map.containsKey(ticket.tid))//先判断是不是坏tid，重复tid
        //    return false;
        //System.out.println("coach : " + ticket.coach + " seat : " + ticket.seat);
        //System.out.println(map.remove(ticket.tid));
        //将tid从有效tid中移除
        if(!map.remove(ticket.tid)){
            return false;
        }
        Seat seat = seats[ticket.route][ticket.coach][ticket.seat];
        seat.writer(ticket.departure,ticket.arrival,false);
        return true;
    }
}
