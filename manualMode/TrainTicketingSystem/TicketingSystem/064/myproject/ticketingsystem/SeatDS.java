package ticketingsystem;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SeatDS {
    public int stationnum;
    public boolean[] idle;
    public boolean[] cacheSeat;

    public ReentrantReadWriteLock myRWLock;
    public Lock seatRdLock;
    public Lock seatWrLock;

    public SeatDS(int stationnum) {
        this.stationnum = stationnum;
        LockInit();
        CacheInit();
    }

    private void LockInit() {
        myRWLock = new ReentrantReadWriteLock();
        seatRdLock = myRWLock.readLock();
        seatWrLock = myRWLock.writeLock();
    }

    private void CacheInit() {
        idle = new boolean[this.stationnum];   // 从第i站启动后 该座位是否可用
        cacheSeat = new boolean[this.stationnum * this.stationnum];
        Arrays.fill(idle, true);
        Arrays.fill(cacheSeat, true);
    }

    public boolean[] modify(int departure, int arrival, boolean RefundOrBuy) {
        int index = (departure - 1) * this.stationnum + arrival - 1;
        boolean[] oldCache = new boolean[this.stationnum * this.stationnum];
        boolean[] returnArray = new boolean[this.stationnum * this.stationnum];

        seatWrLock.lock();
        try {
            if( (RefundOrBuy==false) && (!this.cacheSeat[index]) ) {
                return null; //被抢占，返回null，重试购票
            } else {
                System.arraycopy(this.cacheSeat, 0, oldCache, 0, this.cacheSeat.length);
                for (int i = departure; i < arrival; i++) {
                    idle[i] = RefundOrBuy;
                }
                flushSeatCache();
                for (int i = 1; i < this.cacheSeat.length; i++) {
                    returnArray[i] = (this.cacheSeat[i] == oldCache[i]) ? false : true;
                }
            }
        } finally {
            seatWrLock.unlock();
        }
        return returnArray;
    }

    public boolean isAvailable(int departure, int arrival) {
        int index = (departure - 1) * this.stationnum + arrival - 1;
        seatRdLock.lock();
        try {
            return this.cacheSeat[index];
        } finally {
            seatRdLock.unlock();
        } 
    }

    private void flushSeatCache() {     // 全部cache刷新
        boolean flag = true;   
        for (int i = 1; i < this.stationnum; i++) { //出发站：i
            int cache_row = (i - 1) * this.stationnum;
            for (int j = i + 1; j <= this.stationnum; j++) { //到达站：j
                flag = true;
                for (int k = i; k < j; k++) {   //区间某一站：k
                    if (!idle[k]) {           // a到b站区间，某站到某站的票被购买
                        flag = false;           // i站->j站 前任意一站被占用
                        break;
                    }
                }
                if (!flag) {
                    while (j <= this.stationnum) { // i站->j至终点站 都不可以卖
                        cacheSeat[cache_row + j - 1] = false;  
                        j++;
                    }
                    break;
                } else {
                    cacheSeat[cache_row + j - 1] = true;
                }
            }
        }
    }

}
