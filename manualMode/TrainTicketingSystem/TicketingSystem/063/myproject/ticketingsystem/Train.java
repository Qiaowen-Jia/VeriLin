package ticketingsystem;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

public class Train {
    public AtomicBoolean[] section;
    public int[] section_seatnum;
    public boolean[][] section_seatmap;

    public int coach;
    public int seat;
    public int total;

    public void Train(int coachnum, int seatnum, int stationnum) {
        coach = coachnum;
        seat = seatnum;
        total = coachnum * seatnum;
        section = new AtomicBoolean[stationnum];
        for (int i = 0; i < stationnum; i++) {
            section[i] = new AtomicBoolean(false);
        }
        section_seatmap = new boolean[coachnum * seatnum + 1][stationnum];
        for (int i = 0; i <= coachnum * seatnum; i++) {
            for (int j = 0; j < stationnum; j++) {
                section_seatmap[i][j] = true;
            }
        }
    }

    public int try_lock(int departure, int arrival) {
        for (int i = departure; i < arrival; i++) {
            if (section[i].compareAndSet(false, true)) {
                //
            } else {
                return i; // failing point
            }
        }
        return -1;// succeed
    }

    public void try_unlock(int departure, int fail_point) {
            for (int i = fail_point-1; i >= departure; i--) {
                section[i].set(false);
            }
    }

    public void release_lock(int departure, int arrival) {
        for (int i = arrival; i >= departure; i--) {
            section[i].set(false);
        }
    }

    public int inquiry(int departure, int arrival) {
        int remain = 0;
        first: for (int i = 1; i <= total; i++) {
            for (int j = departure; j < arrival; j++) {
                if (!section_seatmap[i][j]) {
                    continue first;
                }
            }
            remain++;
        }
        return remain;
    }

    public int buy_ticket(int departure, int arrival) {
        second: for (int i = 1; i <= total; i++) {
            for (int j = departure; j < arrival; j++) {
                if (!section_seatmap[i][j]) {
                    continue second;
                }
            }
            for (int k = departure; k < arrival; k++) {
                section_seatmap[i][k] = false;
            }
            return i;
        }
        return -1;
    }

    public int refund_ticket(int departure, int arrival, int seat) {
        for (int i = departure; i < arrival; i++) {
            section_seatmap[seat][i] = true;
        }
        return 1;
    }

}