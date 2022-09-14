package ticketingsystem;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class Coach {
    Seat[] seats;

    public Coach(int seatNum, int stationNum) {

        seats = new Seat[seatNum + 1];
        // seats[0] is invalid
        for(int i = 1; i <= seatNum; i++) {
            seats[i] = new Seat(stationNum);
        }
    }

}
