package ticketingsystem;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Train3 implements Train {
    private SeatWithMissTimes[][] seats;
    private AtomicReference<SeatWithMissTimes> head;
    private AtomicInteger[][] remainSeatsNum;
    private int stationNum;
    private Simple2ReadLock.SimpleReadLock rLock1, rLock2;

    Train3(int coachNum, int seatNum, int stationNum) {
        seats = new SeatWithMissTimes[coachNum + 1][seatNum + 1];
        SeatWithMissTimes curr = seats[1][1] = new SeatWithMissTimes(1, 1, stationNum);
        head = new AtomicReference<>(curr);
        for (int i = 1; i <= coachNum; i++) {
            for (int j = 1; j <= seatNum; j++) {
                if (i == 1 && j == 1) {
                    continue;
                }
                seats[i][j] = new SeatWithMissTimes(i, j, stationNum);
                curr.next = seats[i][j];
                curr = seats[i][j];
            }
        }
        curr.next = seats[1][1];
        this.stationNum = stationNum;
        remainSeatsNum = new AtomicInteger[stationNum][stationNum];
        for (int i = 0; i < remainSeatsNum.length; i++) {
            for (int j = 0; j < remainSeatsNum[i].length; j++) {
                remainSeatsNum[i][j] = new AtomicInteger(coachNum * seatNum);
            }
        }
        Simple2ReadLock simple2ReadLock = new Simple2ReadLock();
        rLock1 = simple2ReadLock.getReadLock(0);
        rLock2 = simple2ReadLock.getReadLock(1);
    }

    @Override
    public int inquiry(int departure, int arrival) {
        return remainSeatsNum[departure][arrival - 1].get();
    }

    @Override
    public boolean get(int departure, int arrival, int[] coach, int[] seat) {
        rLock1.lock();
        try {
            while (inquiry(departure, arrival) != 0) {
                SeatWithMissTimes headSeat = head.get();
                SeatWithMissTimes currSeat = headSeat;
                SeatOccupy oldSeatOccupy;
                do {
//                if (inquiry(departure, arrival) == 0) {
//                    return false;
//                }
                    while ((oldSeatOccupy = currSeat.seatOccupy.get()).notOccupied(departure, arrival - 1)) {
                        SeatOccupy newSeatOccupy = oldSeatOccupy.copy();
                        newSeatOccupy.occupy(departure, arrival - 1);
                        if (currSeat.seatOccupy.compareAndSet(oldSeatOccupy, newSeatOccupy)) {
                            currSeat.maxMissTimes.getAndAdd(-(arrival - departure) * currSeat.missTimesPerStation);
                            coach[0] = currSeat.coach;
                            seat[0] = currSeat.seat;
                            updateSeatsNum(newSeatOccupy, departure, arrival, 0);
                            return true;
                        }
                    }
                    int missTimes = currSeat.missTimes.incrementAndGet();
                    if (currSeat == head.get() && missTimes > currSeat.maxMissTimes.get()) {
                        SeatWithMissTimes newHead = currSeat.next;
                        if (head.compareAndSet(currSeat, newHead)) {
                            currSeat.missTimes.set(0);
                        }
                    }
                    currSeat = currSeat.next;
                } while (currSeat != headSeat);
            }
        } finally {
            rLock1.unlock();
        }
        return false;
    }

    @Override
    public void put(int departure, int arrival, int coach, int seat) {
        rLock2.lock();
        try {
            SeatWithMissTimes currSeat = seats[coach][seat];
            while (true) {
                SeatOccupy oldSeatOccupy = currSeat.seatOccupy.get();
                SeatOccupy newSeatOccupy = oldSeatOccupy.copy();
                newSeatOccupy.free(departure, arrival - 1);
                if (currSeat.seatOccupy.compareAndSet(oldSeatOccupy, newSeatOccupy)) {
                    currSeat.maxMissTimes.getAndAdd((arrival - departure) * currSeat.missTimesPerStation);
                    updateSeatsNum(newSeatOccupy, departure, arrival, 1);
                    return;
                }
            }
        } finally {
            rLock2.unlock();
        }
    }

    private void updateSeatsNum(SeatOccupy seatOccupy, int departure, int arrival, int operation) {
        int next1Index = -1;
        for (int i = arrival - 1; i >= 1; i--) {
            if (i < departure && seatOccupy.get(i) == 1) {
                return;
            }
            for (int j = Math.max(i, departure); j < next1Index || next1Index == -1; j++) {
                if (next1Index == -1 && j > arrival - 1 && (seatOccupy.get(j) == 1 || j > stationNum - 1)) {
                    next1Index = j;
                    continue;
                }
                if (operation == 0) {   // get
                    remainSeatsNum[i][j].getAndDecrement();
                } else if (operation == 1) {    // put
                    remainSeatsNum[i][j].getAndIncrement();
                }
            }
        }
    }

    void output() {
        System.out.print("    ");
        for (int j = 1; j <= stationNum - 1; j++) {
            System.out.print(j + " ");
        }
        System.out.println();
        System.out.print("----");
        for (int j = 1; j <= stationNum - 1; j++) {
            System.out.print("--");
        }
        System.out.println();
        for (int i = 1; i <= stationNum - 1; i++) {
            System.out.print(i + " | ");
            for (int j = 1; j <= stationNum - 1; j++) {
                System.out.print(remainSeatsNum[i][j].get() + " ");
            }
            System.out.println();
        }
        System.out.println("============================================");
    }

    static void test() {
        Train3 train3 = new Train3(1, 1, 10);
        int[] coach = new int[1];
        int[] seat = new int[1];
        train3.output();
        System.out.println(train3.get(2, 4, coach, seat));
        train3.output();
        System.out.println(train3.get(8, 9, coach, seat));
        train3.output();
        System.out.println(train3.get(4, 7, coach, seat));
        train3.output();
        train3.put(4, 7, coach[0], seat[0]);
        train3.output();
        train3.put(8, 9, coach[0], seat[0]);
        train3.output();
        train3.put(2, 4, coach[0], seat[0]);
        train3.output();
    }

    public static void main(String[] args) {
        test();
    }
}
