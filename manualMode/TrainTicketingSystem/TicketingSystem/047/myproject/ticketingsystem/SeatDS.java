package ticketingsystem;

import java.util.Arrays;

/**
 * @author haoheipi
 * @date 19/12/11 14:44
 **/
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
        //更新缓存.
        return updateCache();
    }


    public boolean isAvailable(int departure, int arrival) {
        int index = (departure - 1) * this.stationNum + arrival;
        return this.cache[index];
    }

    public boolean unhold(int departure, int arrival) {
        for (int i = departure; i < arrival; i++) {
            status[i] = true;
        }
        return updateCache();
    }

    public boolean getStatus(int departure, int arrival) {
        //是合法则没被holded。
        for (int i = departure; i < arrival; i++) {
            if (!status[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean updateCache() {
        for (int i = 1; i < this.stationNum; i++) {
            int temp_begin = (i - 1) * this.stationNum;
            for (int j = i + 1; j <= this.stationNum; j++) {
                boolean flag = getStatus(i,j);
                int temp_end = j;
                if (!flag){
                    while (j<=this.stationNum){
                        int index = temp_begin + j;
                        cache[index] = false;
                        j++;
                    }
                    i = temp_end;
                }else {
                    cache[ temp_begin + temp_end] = true;
                }
            }
        }
        return true;
    }

}
