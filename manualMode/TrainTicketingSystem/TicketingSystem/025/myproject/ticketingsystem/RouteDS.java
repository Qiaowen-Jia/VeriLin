package ticketingsystem;

import java.util.Arrays;

public class RouteDS {
    public int coachNum;
    public int seatNum;
    public int stationNum;

    public SeatDS[] seats;
    public int[] cache;
    public int tid;

    public Object lockObject;

    public RouteDS(int coachnum, int seatnum, int stationnum) {
        this.coachNum = coachnum;
        this.seatNum = seatnum;
        this.stationNum = stationnum;

        // 初始化seats[]：
        this.seats = new SeatDS[coachnum * seatnum + 1];
        for (int i = 1; i <= coachnum * seatnum; i++) {
            this.seats[i] = new SeatDS(stationnum);
        }

        // 初始化缓存cache：
        this.cache = new int[(stationnum * (stationnum - 1)) + 1];
        Arrays.fill(cache, -1);

        // 初始化tid为0：
        tid = 0;

        // 初始化锁对象lockObject：
        lockObject = new Object();
    }

    public int[] buyTicket(int departure, int arrival) {
        int index = getIndex(departure, arrival);
        int count = 0;
        int i = 1;
        int tidNum = -1;
        int[] coachAndSeat = new int[2];
        boolean[] oldStatus = new boolean[this.stationNum * this.stationNum];
        boolean[] newStatus = new boolean[this.stationNum * this.stationNum];
        synchronized (lockObject) {
            if (this.cache[index] == 0) {
                return null;
            }
            for (; i < this.seats.length; i++) {
                if (this.seats[i].isAvailable(departure, arrival)) {
                    for (int j = 0; j < this.seats[i].cache.length; j++) {
                        oldStatus[j] = this.seats[i].cache[j];
                    }
                    if (this.seats[i].hold(departure, arrival)) {
                        tidNum = tid++;
                        coachAndSeat = transform2seat(i);
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
                count = countingSeat(i + 1, departure, arrival);
            }

            updateCache(oldStatus, newStatus, this.seats[i], false);

            this.cache[index] = count;
        }
        return new int[] { tidNum, coachAndSeat[0], coachAndSeat[1] };
    }

    public int inquiry(int departure, int arrival) {
        synchronized (lockObject) {
            int index = getIndex(departure, arrival);
            // 不为空取出当前缓存值,否则开始遍历访问。
            int value = this.cache[index];
            if (value != -1) {
                return value;
            }
            return countingSeat(1, departure, arrival);
        }
    }

    public boolean refund(Ticket ticket, int departure, int arrival) {
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

    private int getIndex(int departure, int arrival) {
        return (departure - 1) * this.stationNum + arrival;
    }

    // 将一维座位号转化为 车厢和座位
    private int[] transform2seat(int i) {
        int index1 = i / this.seatNum;
        int index2 = i % this.seatNum;
        if (index2 != 0) {
            return new int[] { index1 + 1, index2 };
        } else {
            return new int[] { index1, this.seatNum };
        }
    }

    private int countingSeat(int start, int departure, int arrival) {
        int count = 0;
        for (int i = start; i < this.seats.length; i++) {
            if (this.seats[i].isAvailable(departure, arrival)) {
                count++;
            }
        }
        return count;
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
