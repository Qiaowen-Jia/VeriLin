package ticketingsystem;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class TicketingDS implements TicketingSystem {

	//ToDo
    // 需要注意的是序号起名都是从1开始
    private int routenum = 5;  // 车次总数
    private int coachnum = 8;  // 车厢总数
    private int seatnum = 100; // 每节车厢座位数
    private int stationnum = 10; // 车次经停站的数量
    private int threadnum = 16; // 线程数
    private List<List<List<Seat>>> seatLists; // 所有座位
    private AtomicLong getTid;
    private ConcurrentHashMap<Long, Ticket> selledTickets;
    private int[][][] TicketCache;
    private boolean[][][] isCacheValid;


    private void init(){
        getTid = new AtomicLong(1);
        selledTickets = new ConcurrentHashMap<>();
        //初始各个座位
        seatLists = new ArrayList<>(routenum);
        for (int i = 0; i < routenum; i++){
            List<List<Seat>> tempLists = new ArrayList<>(coachnum);
            for (int j = 0; j < coachnum; j++){
                List<Seat> list = new ArrayList<>(seatnum);
                for (int k = 0; k<seatnum; k++){
                    list.add(new Seat(stationnum));
                }
                tempLists.add(list);
            }
            seatLists.add(tempLists);
        }
        TicketCache = new int[routenum][stationnum-1][stationnum];
        isCacheValid = new boolean[routenum][stationnum-1][stationnum];
    }

    public TicketingDS(){
        init();
    }
    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum){
        this.routenum = routenum;
        this.coachnum = coachnum;
        this.seatnum = seatnum;
        this.stationnum = stationnum;
        this.threadnum = threadnum;
        init();
    }



    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        List<List<Seat>> tempLists = seatLists.get(route-1);
        for (int i = 0; i < coachnum; i++){
            for (int j = 0; j < seatnum; j++){
                // 买票成功，seat的信息已经改了， 同一seat的相关操作是互斥的
                if (tempLists.get(i).get(j).tryBuy(departure, arrival)){
                    long tid = getTid.getAndIncrement();
                    Ticket ticket = getTicket(tid, passenger, route, i+1, j+1, departure, arrival);
                    selledTickets.put(tid, ticket);
                    // 相应缓存发生了修改
                    for (int k = 0; k < stationnum-1; k++) {
                        for (int l = k+1; l < stationnum; l++) {
                            if (l <= departure-1 || k >= arrival-1)
                                continue;
                            isCacheValid[route-1][k][l] = false;
                        }
                    }
                    return ticket;
                }
            }
        }
        return null;
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        if (isCacheValid[route-1][departure-1][arrival-1])
            return TicketCache[route-1][departure-1][arrival-1];
        int count = 0;
        List<List<Seat>> tempLists = seatLists.get(route-1);
        for (int i = 0; i < coachnum; i++){
            for (int j = 0; j < seatnum; j++){
                if (tempLists.get(i).get(j).canSelled(departure, arrival)){
                    count++;
                }
            }
        }
        TicketCache[route-1][departure-1][arrival-1] = count;
        isCacheValid[route-1][departure-1][arrival-1] = true;
        return count;
    }

    @Override
    public boolean refundTicket(Ticket ticket) {
        Ticket selled = selledTickets.get(ticket.tid);
        if (isEqual(ticket, selled)){
            selledTickets.remove(selled.tid);
            seatLists.get(selled.route-1).get(selled.coach-1).get(selled.seat-1).free(selled.departure, selled.arrival);
            // 相应缓存发生了修改
            for (int k = 0; k < stationnum-1; k++) {
                for (int l = k+1; l < stationnum; l++) {
                    if (l <= selled.departure-1 || k >= selled.arrival-1)
                        continue;
                    isCacheValid[selled.route-1][k][l] = false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean isEqual(Ticket A, Ticket B){
        if (A == null || B == null)
            return false;
        return !(A.tid != B.tid || !A.passenger.equals(B.passenger) || A.route != B.route
        || A.coach != B.coach || A.seat != B.seat || A.departure != B.departure || A.arrival != B.arrival);
    }

    // 构建Ticket对象
    private Ticket getTicket(long tid, String passenger, int route, int coach, int seat, int departure, int arrival){
        Ticket t = new Ticket();
        t.tid = tid;
        t.passenger = passenger;
        t.route = route;
        t.coach = coach;
        t.seat = seat;
        t.departure = departure;
        t.arrival = arrival;
        return t;
    }
}
