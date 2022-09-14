package ticketingsystem;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 座位锁+乐观锁+位运算+优化查询+线程分区+tid分段+threadlocal
 */
public class TicketingDS implements TicketingSystem {

    private int routenum;
    private int coachnum;
    private int seatnum;
    private int stationnum;
    private int threadnum;

    private final int MAX_SECTION_NUM = 100;  // 最大分区数>=1
    private final Long MAX_TID = 10000000L;  // 每个线程tid区段

    private Lock[][][] locks;  // 座位锁
    private int[][][] arr;  // bit位代表某座某站是否为空
    private ConcurrentHashMap<Long, Ticket> orderedTickets;  // 用于退票检验
    private AtomicInteger[][][] leftTickets;  // 余票统计

    private AtomicInteger sectionId;  // 线程所属分区
    private int sectionnum; // 分区数量
    private AtomicInteger threadId;  // 用于给线程指派tid起点
    private int[] startCoachAtSection;  // 分区起始车厢
    private int[] startSeatAtSection;  // 分区起始座位


    // 每个线程从自己的tid开始递增
    static ThreadLocal<Long> tl_tid = new ThreadLocal<Long>(){
        @Override
        protected Long initialValue() {
            return 0L;
        }
    };
    // 该线程查票起始车厢
    static ThreadLocal<Integer> tl_startCoach = new ThreadLocal<Integer>(){
        @Override
        protected Integer initialValue() {
            return 0;
        }
    };
    // 该线程查票起始座位
    static ThreadLocal<Integer> tl_startSeat = new ThreadLocal<Integer>(){
        @Override
        protected Integer initialValue() {
            return 0;
        }
    };

    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
        this.routenum = routenum;
        this.coachnum = coachnum;
        this.seatnum = seatnum;
        this.stationnum = stationnum;
        this.threadnum = threadnum;

        this.arr = new int[routenum+1][coachnum+1][seatnum+1];
        this.sectionId = new AtomicInteger(1);
        this.threadId = new AtomicInteger(1);
        this.orderedTickets = new ConcurrentHashMap<>(5*routenum*coachnum*seatnum);
        this.locks = new Lock[routenum+1][coachnum+1][seatnum+1];
        for(int i=1; i<=routenum; i++){
            for(int j=1; j<=coachnum; j++){
                for(int k=1; k<=seatnum; k++){
                    this.locks[i][j][k] = new ReentrantLock();
                }
            }
        }
        // 初始化余票
        this.leftTickets = new AtomicInteger[routenum+1][stationnum+1][stationnum+1]; // 余票字段是点，arr字段是线段
        for(int i=1; i<=routenum; i++){
            for(int j=1; j<=stationnum; j++){
                for(int k=1; k<=stationnum; k++){
                    this.leftTickets[i][j][k] = new AtomicInteger(coachnum*seatnum);
                }
            }
        }
        // 计算分区起始位置
        this.sectionnum = this.threadnum<MAX_SECTION_NUM ? this.threadnum : MAX_SECTION_NUM;
        this.startCoachAtSection = new int[this.sectionnum+1];
        this.startSeatAtSection = new int[this.sectionnum+1];
        int seatnumOfsection = coachnum*seatnum / this.sectionnum;
        for(int i=1; i<=this.sectionnum; i++){
            int seatid = (i-1)*seatnumOfsection + 1;
            int startCoach;
            int startSeat;
            if(seatid%this.seatnum == 0){
                startCoach = seatid / this.seatnum;
                startSeat = this.seatnum;
            }else{
                startCoach = (seatid/this.seatnum) + 1;
                startSeat = seatid % this.seatnum;
            }
            this.startCoachAtSection[i] = startCoach;
            this.startSeatAtSection[i] = startSeat;
        }
    }

    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {

        // 初始化线程tid, 0表示未分配
        Long tid = tl_tid.get();
        if(tid == 0L){
            tl_tid.set(this.threadId.getAndIncrement()*MAX_TID);
        }

        // 买票前先分配分区, 0表示未分配
        if(tl_startCoach.get() == 0){
            int sid = this.sectionId.getAndIncrement();
            int s = sid%this.sectionnum==0 ? this.sectionnum : sid%this.sectionnum;
            tl_startCoach.set(this.startCoachAtSection[s]);
            tl_startSeat.set(this.startSeatAtSection[s]);
        }

        // 无效输入
        if(route>routenum || route<1
                || departure>stationnum || departure<1
                || arrival>stationnum || arrival<1
                || departure>=arrival){
            return null;
        }

        // 每个线程先去自己的分区工作
        int startCoach = tl_startCoach.get();
        int endCoach = this.coachnum;
        int startSeat = tl_startSeat.get();
        int endSeat = this.seatnum;

        int time = 2; // 尝试两次, 第一次工作于自己的区间，第二次为剩下的区间
        while(inquiry(route, departure, arrival)>0&&time>0) {

            for (int i = startCoach; i <= endCoach; i++) {
                for (int j = startSeat; j <= endSeat; j++) {
                    // 座位可用时才需要临界区，不可用时直接跳过
                    if (validateSeat(route, i, j, departure, arrival)) {
                        // 占座
                        this.locks[route][i][j].lock();
                        try {
                            // 多个线程同时看好一个座位，所以需要在临界区内再检查一遍，若不可用继续查座位
                            if (!validateSeat(route, i, j, departure, arrival)) continue;
                            takeSeat(route, i, j, departure, arrival);
                        } finally {
                            this.locks[route][i][j].unlock();
                        }
                        // inquiry只需静态一致，无须同步
                        updateLeftTicket(false, route, i, j, departure, arrival);
                        Ticket ticket = createTicket(passenger, route, i, j, departure, arrival);
                        // 添加已购车票
                        orderedTickets.put(ticket.tid, ticket);
                        // 更改起始位置为上次卖掉的位置,更利于买票
                        tl_startCoach.set(i);
                        tl_startSeat.set(j);
                        return ticket;
                    }
                }
            }
            // 在自己的分区未买到票，更改搜索分区。
            time--;
            endCoach = startCoach;
            endSeat = startSeat;
            startCoach = 1;
            startSeat = 1;
        }

        return null;
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        return this.leftTickets[route][departure][arrival].get();
    }

    @Override
    public boolean refundTicket(Ticket ticket) {

        if(ticket.route>this.routenum || ticket.coach>this.coachnum
                || ticket.seat>this.seatnum
                || ticket.departure >= ticket.arrival) return false;

        // 验证无效票
        if(!orderedTickets.containsKey(ticket.tid)) return false;

        Ticket t = orderedTickets.get(ticket.tid);
        if(t!=null){
            if(t.passenger!=ticket.passenger || t.route!=ticket.route
                    || t.coach!=ticket.coach || t.seat!=ticket.seat
                    || t.departure!=ticket.departure
                    || t.arrival!=ticket.arrival) return false;
        }

        locks[ticket.route][ticket.coach][ticket.seat].lock();
        try {
            if(!orderedTickets.containsKey(ticket.tid)){
                return false;
            }
            // 还座
            returnSeat(ticket.route, ticket.coach, ticket.seat, ticket.departure, ticket.arrival);
            // 去掉票对象,一定要在临界区内，否则重复退票
            orderedTickets.remove(ticket.tid);
        }finally {
            locks[ticket.route][ticket.coach][ticket.seat].unlock();
        }
        updateLeftTicket(true, ticket.route, ticket.coach, ticket.seat, ticket.departure, ticket.arrival);
        return true;
    }

    // 创建票对象
    private Ticket createTicket(String passenger, int route, int coach, int seat, int departure, int arrival){
        Ticket ticket = new Ticket();
        ticket.passenger = passenger;
        ticket.departure = departure;
        ticket.arrival = arrival;
        ticket.coach = coach;
        ticket.route = route;
        ticket.seat = seat;
        ticket.tid = tl_tid.get();
        tl_tid.set(ticket.tid+1);
        return ticket;
    }

    // 1bit表示一站路程
    private int mask(int departure, int arrival){
        return ((1<<arrival)-(1<<departure))>>1;
    }

    // 验证某座位是否可坐,座位按位从右往左排列
    private boolean validateSeat(int route, int coach, int seat, int departure, int arrival){
        int a = mask(departure, arrival);
        int b = arr[route][coach][seat];
        return (a&b)==0 ? true : false;
    }

    // 占座
    private void takeSeat(int route, int coach, int seat, int departure, int arrival){
        int a = mask(departure, arrival);
        int b = arr[route][coach][seat];
        arr[route][coach][seat] = a|b;
    }

    // 还座
    private void returnSeat(int route, int coach, int seat, int departure, int arrival){
        int a = ~(mask(departure, arrival));
        int b = arr[route][coach][seat];
        arr[route][coach][seat] = a&b;
    }

    // direction == true, 余票增加, direction == false, 余票减少
    private void updateLeftTicket(boolean direction, int route, int coach, int seat, int departure, int arrival){

        // 寻找右边临界值，查找起始车站
        int startStation = 1;
        for(int i=departure-1; i>=1; i--){
            int mask = 1<<(i-1);
            if((mask & arr[route][coach][seat]) == mask){
                startStation = i+1;
                break;
            }
        }
        // 寻找左边临界值，查找终点车站
        int endStation = this.stationnum;
        for(int i=arrival; i<this.stationnum; i++){
            int mask = 1<<(i-1);
            if((mask & arr[route][coach][seat]) == mask){
                endStation = i;
                break;
            }
        }
        // 更新区间
        for(int x=startStation; x<endStation; x++){
            for(int y=startStation+1; y<=endStation; y++){
                if(y<=departure || x>=arrival) continue; // 未交叉的地方管不着
                if(direction) this.leftTickets[route][x][y].getAndIncrement();
                else this.leftTickets[route][x][y].getAndDecrement();
            }
        }
    }

}
