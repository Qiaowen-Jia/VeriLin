package ticketingsystem;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

// !! Note that index in this class is canonical !!
public class Train {
    
    public boolean[][] seatMap;       // 表示车座状态
    public final int seatNum;         // 表示列车座位的总数
    public final int coachNum;        // 表示车厢数
    public final int stationNum;      // 表示车站数目
    private ReentrantLock[] locks;       // 列车锁，可重入锁
    private AtomicLong modifyID;

    public Train(int coachnum, int seatnum, int stationnum) {
        seatNum = coachnum * seatnum;
        coachNum = coachnum;
        stationNum = stationnum;
        seatMap = new boolean[seatNum][stationNum];
        locks = new ReentrantLock[seatNum+1];
        modifyID = new AtomicLong(0);
        for (int i = 0; i <= seatNum; i++) {
            locks[i] = new ReentrantLock();
        }

        for (int i = 0; i < seatNum; i++) {
            for (int j = 0; j < stationNum; j++) {
                seatMap[i][j] = false;
            }
        }     
    }

    public int lockSeat(int departure, int arrival) {
        for (int i = 0; i < seatNum; i++) {
            boolean seatValid = true;
            for (int j = departure; j < arrival && seatValid; j++) {
                seatValid = !seatMap[i][j];
            }
            if (seatValid) {
                locks[i].lock();
                modifyID.getAndIncrement();
                try {
                    for (int j = departure; j < arrival && seatValid; j++) {
                        seatValid = !seatMap[i][j];
                    }
                    if (seatValid) {
                        for (int j = departure; j < arrival; j++) {
                            assert !seatMap[i][j];
                            seatMap[i][j] = true;
                        }
                        return i;
                    }
                } finally {
                    locks[i].unlock();
                }
            }
        }
        return -1;
    }

    public boolean unlockSeat(int seatnum, int departure, int arrival) {
        locks[seatnum].lock();
        modifyID.getAndIncrement();
        try {
            for (int i = departure; i < arrival; i++) {
                if (!seatMap[seatnum][i]) {
                    assert false : "Panic!! unlock no-owned seat";
                }
                seatMap[seatnum][i] = false;
            }
            return true;
        } finally {
            locks[seatnum].unlock();
        }
    }


    public int querySeat(int departure, int arrival) {
        /*
        int counter = 0;
        for (int i = 0; i < seatNum; i++) {
            boolean seatValid = true;
            for (int j = departure; j < arrival && seatValid; j++) {
                seatValid = !seatMap[i][j];
            }
            if (seatValid) {
                counter++;
            }
        }
        return counter;*/
        int counter;
        boolean[][] counterMap = new boolean[2][seatNum];
        long[] idMap = new long[2];
        int counterPtr = 0;

        do {
            // First scan
            counter = 0;
            idMap[0] = modifyID.get();
            for (int i = 0; i < seatNum; i++) {
                boolean seatValid = true;
                for (int j = departure; j < arrival && seatValid; j++) {
                    seatValid = !seatMap[i][j];
                }
                counterMap[0][i] = seatValid;
                if (seatValid) {
                    counter++;
                }
            }

            // Second scan
            counter = 0;
            for (int i = 0; i < seatNum; i++) {
                boolean seatValid = true;
                for (int j = departure; j < arrival && seatValid; j++) {
                    seatValid = !seatMap[i][j];
                }
                counterMap[1][i] = seatValid;
                if (seatValid) {
                    counter++;
                }
            }
            idMap[1] = modifyID.get();
        } while (!(idMap[0] == idMap[1]) || !Arrays.equals(counterMap[0], counterMap[1]));

        return counter;
    }
}
