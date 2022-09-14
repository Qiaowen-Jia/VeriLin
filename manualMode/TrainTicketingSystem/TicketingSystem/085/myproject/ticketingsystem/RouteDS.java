package ticketingsystem;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RouteDS {
    public int routenum;
    public int coachNum;//车厢数
    public int stationNum;//每次车站经停次数
    public int route_Id;//车次号
    public int seatNum;//车座数
    public int all_seat_Num;//此次车所有的车座位数
    public SeatDS[] seat_Every;
    AtomicInteger[] all_seat_State;
    private ReentrantLock seat_occupy_status_Lock = new ReentrantLock();
    private ReentrantReadWriteLock seat_inquiry_read_lock = new ReentrantReadWriteLock();
    AtomicInteger tid;

    public RouteDS(int routenum, int coachnum, int seatnum, int stationnum, int route_id) {
        this.routenum = routenum;
        this.coachNum = coachnum;
        this.stationNum = stationnum;
        this.route_Id = route_id;
        this.seatNum = seatnum;
        this.all_seat_Num = coachnum * seatnum;
        this.seat_Every = new SeatDS[coachnum * seatnum + 1];
        for (int i = 1; i <= coachnum * seatnum; i++) {
            this.seat_Every[i] = new SeatDS(i, stationnum);
        }
        this.tid = new AtomicInteger(route_Id);;
        this.all_seat_State = new AtomicInteger[coachnum * seatnum + 1];
        for (int i = 1; i < coachnum * seatnum + 1; ++i) {
            this.all_seat_State[i] = new AtomicInteger(0);
        }
    }

    public int[] buyTicket(int departure, int arrival) {
        int tidNum = -1;
        int use_seat = -1;
        for (int i = 0; i < stationNum ; i++) {//i表示当前座位售出的票数
            boolean is_ok = false;
            //申请座位——找到第一个为i的位置j修改其seat_every[j]的数组的值，将all_seat_State[]置为i+1
            for (int j = 1; j < all_seat_State.length; j++) {
                //seat_Every[j]是否能够满足上车下车车站的要求seat_occupy_status数组中第departure和arrival是否为0
                while (all_seat_State[j].get() == i && is_seat_can_use(j, departure, arrival)){
                    //满足就把seat_Every[j].seat_occupy_status[departure-arrival-1]全都置1
                    //getandset判断all_seat_State[j]是否还等于i，等于就执行下面的不等于就不执行直接continue
                    //CAS主要作用是判断当前选的座位是否已经被别人占用了，即当前all_seat_State[j]的值不是循环里的i了，而是i+1了
                    if (all_seat_State[j].compareAndSet(i, all_seat_State[j].get())) {
                        try {
                            seat_occupy_status_Lock.lock();
                            for (int k = departure; k < arrival; k++) {
                                seat_Every[j].seat_occupy_status[k] = 1;
                            }
                            //更改all_seat_State[j]的值
                            all_seat_State[j].getAndAdd(1);
                            is_ok = true;
                            use_seat = j;
                            //System.out.println("route_Id:" + route_Id);
                            //System.out.println("--------tid:" + tid.get());
                            tid.getAndAdd(routenum);
                            tidNum = tid.get();
                            // System.out.println("tidNum:" + tidNum);
                            //System.out.println("tid:" + tid.get());
                        } finally {
                            seat_occupy_status_Lock.unlock();
                        }
                    }
                }
                if (is_ok == true) {
                    break;
                }
            }
            if (is_ok == true) {
                break;
            }
        }
        if(tidNum == -1){
            return null;
        }
        int[] seat_kno = use_seat_to_coach_seat_num(use_seat);
        int coach =  seat_kno[0];
        int seat_num = seat_kno[1];
        return new int[]{tidNum, coach, seat_num};
    }

    //查询余票方法，即查询rout车次从departure站到arrival站余票数
    public int inquiry(int departure, int arrival) {
        int count = 0;
        try {
            seat_inquiry_read_lock.readLock().lock();
            for(int j = 1; j < all_seat_State.length; j++){
                if(all_seat_State[j].get() == 0){
                    count = count + 1;
                }else{
                    if(is_seat_can_use(j, departure, arrival)){
                        count = count + 1;
                        //System.out.println("111111111111111111111111111111111111111111");
                    }
                }
            }
        } finally {
            seat_inquiry_read_lock.readLock().unlock();
        }
        return count;
    }

    public boolean refund(Ticket ticket, int departure, int arrival) {
        boolean is_refund_ok = false;
        try {
            seat_occupy_status_Lock.lock();
            //System.out.println("departure:" + departure + "  arrival:" + arrival + "  ticket.tid:" + ticket.tid + "  ticket.coach:" + ticket.coach + "  ticket.seat:" + ticket.seat + "  seatNum:" + seatNum + "  kkkkk:" + ((ticket.coach-1)*seatNum + ticket.seat));
            for (int k = departure; k < arrival; k++) {
                //System.out.println("k：" + k + "ticket.coach*seatNum + ticket.seat" + ticket.coach*seatNum + ticket.seat );
                seat_Every[(ticket.coach-1)*seatNum + ticket.seat].seat_occupy_status[k] = 0;
            }
            //更改all_seat_State[j]的值
            all_seat_State[(ticket.coach-1)*seatNum + ticket.seat].getAndAdd(-1);
            is_refund_ok = true;
        } finally {
            seat_occupy_status_Lock.unlock();
        }
        return is_refund_ok;
    }//退票方法，对有效的Ticket对象返回true，对错误或无效的Ticket对象返回false

    public boolean is_seat_can_use(int j, int departure, int arrival){
        boolean is_can_use = true;
        for(int l = departure; l < arrival; l++){
            if(seat_Every[j].seat_occupy_status[l] != 0 ){
                is_can_use = false;
                return is_can_use;
            }
        }
        return is_can_use;
    }

    public int[] use_seat_to_coach_seat_num(int use_seat){
        int[] seat_kno = {0,0};
        int coach = -1;
        int seat_num = -1;
        if(use_seat % seatNum == 0){
            coach = use_seat / seatNum;
            //seat_num = use_seat % seatNum;
        }else{
            coach = use_seat / seatNum + 1;
        }
        if(use_seat % seatNum == 0){
            seat_num = seatNum;
        }else{
            seat_num = use_seat % seatNum;
        }
        //System.out.println("use_seat: " + use_seat + "  coach: "+ coach + "  seat_num: " + seat_num);
        return new int[]{coach, seat_num};

    }
}
