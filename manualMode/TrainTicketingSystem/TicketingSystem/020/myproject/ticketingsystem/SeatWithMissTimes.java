package ticketingsystem;

import java.util.concurrent.atomic.AtomicInteger;

public class SeatWithMissTimes extends Seat {
    int missTimesPerStation;
    SeatWithMissTimes next;
    SimpleInteger missTimes;
    AtomicInteger maxMissTimes;

    SeatWithMissTimes(int coach, int seat, int stationNum) {
        super(coach, seat, stationNum);
        this.missTimesPerStation = 10;
        missTimes = new SimpleInteger(0);
        maxMissTimes = new AtomicInteger(stationNum * missTimesPerStation);
    }

    static class SimpleInteger {
        int value;

        SimpleInteger(int value) {
            this.value = value;
        }

        void set(int value) {
            this.value = value;
        }

        int get() {
            return this.value;
        }

        int incrementAndGet() {
            return ++value;
        }

        int getAndAdd(int value) {
            int res = this.value;
            this.value += value;
            return res;
        }
    }
}
