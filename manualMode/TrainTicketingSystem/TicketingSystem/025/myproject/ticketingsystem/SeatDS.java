package ticketingsystem;

import java.util.Arrays;

public class SeatDS {
    public boolean[] status;
    public boolean[] cache;
    public int stationNum;

    public SeatDS(int stationnum) {
        this.stationNum = stationnum;
        status = new boolean[stationnum];
        cache = new boolean[this.stationNum * this.stationNum];
        Arrays.fill(status, true);
        Arrays.fill(cache, true);
    }

    public boolean hold(int departure, int arrival) {
        for (int i = departure; i < arrival; i++) {
            status[i] = false;
        }
        // 更新缓存
        return seatUpdateCache();
    }

    public boolean isAvailable(int departure, int arrival) {
        int index = (departure - 1) * this.stationNum + arrival;
        return this.cache[index];
    }

    public boolean unhold(int departure, int arrival) {
        for (int i = departure; i < arrival; i++) {
            status[i] = true;
        }
        // 更新缓存
        return seatUpdateCache();
    }

    public boolean getStatus(int departure, int arrival) {
        for (int i = departure; i < arrival; i++) {
            if (status[i] == false) {
                return false;
            }
        }
        return true;
    }

    public boolean seatUpdateCache() {
        for (int i = 1; i < this.stationNum; i++) {
            int begin = (i - 1) * this.stationNum;
            for (int j = i + 1; j <= this.stationNum; j++) {
                int end = j;
                if (!getStatus(i, j)) {
                    while (j <= this.stationNum) {
                        int index = begin + j;
                        cache[index] = false;
                        j++;
                    }
                    i = end;
                } else {
                    cache[begin + end] = true;
                }
            }
        }
        return true;
    }

}
