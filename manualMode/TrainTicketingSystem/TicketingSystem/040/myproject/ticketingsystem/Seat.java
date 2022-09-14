package ticketingsystem;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class Seat {
    /** Whether ticket is **really** sold from departure to arrival
     *
     */
    boolean[][] sold;


    /**
     * 这个座位对[departure, arrival]区间的影响次数，用于防止多次扣票
     *
     */
    int[][] influence;

    ReentrantLock seatLock;
    public Seat(int stationNum) {
        sold = new boolean[stationNum + 1][stationNum + 1];
        influence = new int[stationNum + 1][stationNum + 1];

        for(int dep = 1; dep <= stationNum; dep++) {
            for(int arr = dep + 1; arr <= stationNum; arr++) {
                sold[dep][arr] = false;
                influence[dep][arr] = 0;
            }
        }
        seatLock = new ReentrantLock();
    }

}
