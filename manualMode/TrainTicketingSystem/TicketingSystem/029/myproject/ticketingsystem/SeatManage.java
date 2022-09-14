package ticketingsystem;

import java.util.concurrent.atomic.AtomicIntegerArray;

public class SeatManage {
    private int routeSeatNum;
    private int seatNum;
    private AtomicIntegerArray[] seats;

    public SeatManage(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
        this.seatNum = seatNum;
        this.routeSeatNum = coachNum * seatNum;
        seats = new AtomicIntegerArray[routeNum];
        for (int i= 0; i < seats.length; i++) {
            seats[i] = new AtomicIntegerArray(routeSeatNum);
        }
    }
    //判断座位是否可用
    public boolean isAvailable(MyTicket it){
        int s = it.departure - 1;
        int e = it.arrival - 1;
        int index = getSeatIndex(it.coach, it.seat);
        int value = seats[it.route - 1].get(index);
        return BitManage.isRangeZero(value, s, e);
    }
    //尝试占用座位
    public boolean tryOccupy(MyTicket it) {
        int s = it.departure - 1;
        int e = it.arrival - 1;
        int index = getSeatIndex(it.coach, it.seat);
        int oldValue = seats[it.route - 1].get(index);
        if (!BitManage.isRangeZero(oldValue, s, e))
            return false;
        int newValue = BitManage.setRange(oldValue, s, e);
        return seats[it.route - 1].compareAndSet(index, oldValue, newValue);
    }
    //释放座位
    public void free(MyTicket it) {
        int s = it.departure - 1;
        int e = it.arrival - 1;
        int index = getSeatIndex(it.coach, it.seat);
        while (true) {
            int oldValue = seats[it.route - 1].get(index);
            int newValue = BitManage.resetRange(oldValue, s, e);
            if(seats[it.route - 1].compareAndSet(index, oldValue, newValue)){
                break;
            }
        }
    }

    public int getSeatIndex(int coach, int seat){
        coach -= 1;
        seat -= 1;
        return coach * seatNum + seat;
    }
}
