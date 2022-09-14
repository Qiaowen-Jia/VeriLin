package ticketingsystem;



import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author haoheipi
 * @date 19/12/11 0:55
 **/

public class RouteDS {
    public int coachNum;
    public int seatNum;
    public int stationNum;

    public SeatDS[] seats;
    public int tid;
    public Object lockObject;


    public int[] cache;

//    public AtomicInteger hitNum = new AtomicInteger(0);
//    public AtomicInteger sumNum = new AtomicInteger(0);

    public RouteDS(int coachnum, int seatnum, int stationnum) {
        this.coachNum = coachnum;
        this.seatNum = seatnum;
        this.stationNum = stationnum;

        this.seats = new SeatDS[coachnum * seatnum + 1];
        for (int i = 1; i <= coachnum * seatnum; i++) {
            this.seats[i] = new SeatDS(stationnum);
        }
        tid = 0;
        lockObject = new Object();


        this.cache = new int[(stationnum * (stationnum - 1)) + 1];
        Arrays.fill(cache, -1);
    }


    //key为例如从1，2，就为12.成功的话返回[tid,coachNo,seatNo]
    public int[] buyTicket(int departure, int arrival) {
//        sumNum.incrementAndGet();
        int index = (departure - 1) * this.stationNum + arrival;
        //顺序遍历座位，如果hold则返回该
        int count = 0;
        int i = 1;
        int tidNum = -1;
        int[] coachAndSeat = new int[2];
        boolean[] oldStatus = new boolean[this.stationNum * this.stationNum];
        boolean[] newStatus = new boolean[this.stationNum * this.stationNum];
        synchronized (lockObject) {
            if (this.cache[index] == 0){
                return null;
            }
            for (; i < this.seats.length; i++) {
                if (this.seats[i].isAvailable(departure, arrival)) {
                    for (int k = 0; k < this.seats[i].cache.length; k++) {
                        oldStatus[k] = this.seats[i].cache[k];
                    }
                    if (this.seats[i].hold(departure, arrival)) {
                        tidNum = tid++;
                        coachAndSeat = pasre(i);
                        break;
                    }
                }
            }
            if (tidNum == -1) {
                return null;
            }
            int value = this.cache[index];
            if (value != -1) {
                count = value - 1;
            } else {
                for (int j = i + 1; j < this.seats.length; j++) {
                    if (this.seats[j].isAvailable(departure, arrival)) {
                        count++;
                    }
                }
            }

            updateCache(oldStatus, newStatus, this.seats[i], false);

            this.cache[index] = count;
        }
        return new int[]{tidNum, coachAndSeat[0], coachAndSeat[1]};
    }


    public int inquiry(int departure, int arrival) {
//        sumNum.incrementAndGet();
        int index = (departure - 1) * this.stationNum + arrival;
        //不为空取出当前缓存值,否则开始遍历访问。
        int value = this.cache[index];
        if (value != -1) {
//            hitNum.incrementAndGet();
            return value;
        }
        int count = 0;
        for (int i = 1; i < this.seats.length; i++) {
            if (this.seats[i].isAvailable(departure, arrival)) {
                count++;
            }
        }
        return count;
    }


    public boolean refund(Ticket ticket, int departure, int arrival) {
//        sumNum.incrementAndGet();
        int seatNo = (ticket.coach - 1) * this.coachNum + ticket.seat;
        boolean[] oldStatus = new boolean[this.stationNum * this.stationNum];
        boolean[] newStatus = new boolean[this.stationNum * this.stationNum];
        synchronized (lockObject) {
            for (int i = 0; i < this.seats[seatNo].cache.length; i++) {
                oldStatus[i] = this.seats[seatNo].cache[i];
            }
            if (this.seats[seatNo].unhold(departure, arrival)) {
                updateCache(oldStatus, newStatus, this.seats[seatNo], true);
                return true;
            }
        }
        return false;
    }


    //将一维座位号转化为 车厢和座位
    private int[] pasre(int i) {
        int i1 = i / this.seatNum;
        int i2 = i % this.seatNum;
        if (i2 != 0) {
            return new int[]{i1 + 1, i2};
        } else {
            return new int[]{i1, this.seatNum};
        }
    }

    private void updateCache(boolean[] oldStatus, boolean[] newStatus, SeatDS seat, boolean flag) {
        int i;
        for (i = 0; i < seat.cache.length; i++) {
            newStatus[i] = seat.cache[i];
        }
        for (i = 1; i < this.stationNum; i++) {
            int temp = (i - 1) * this.stationNum;
            for (int j = i + 1; j <= this.stationNum; j++) {
                int index = temp + j;
                if (oldStatus[index] != newStatus[index]) {
                    int res = this.cache[index];
                    if (res != -1) {
                        this.cache[index] = (flag ? res + 1 : res - 1);
                    }
                }
            }
        }
    }
}
