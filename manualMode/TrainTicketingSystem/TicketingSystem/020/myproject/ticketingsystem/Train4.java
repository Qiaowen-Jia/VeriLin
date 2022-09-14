package ticketingsystem;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

public class Train4 implements Train {
    private ConcurrentSkipListSet<SeatWithInterval>[][] seats;
    private ConcurrentSkipListSet<SeatWithInterval>[][] heads;
    private AtomicInteger[][] remainSeatsNum;
    private int stationNum;
    private Simple2ReadLock.SimpleReadLock rLock1, rLock2;

    Train4(int coachNum, int seatNum, int stationNum) {
        this.stationNum = stationNum;
        seats = (ConcurrentSkipListSet<SeatWithInterval>[][]) new ConcurrentSkipListSet[stationNum][stationNum];
        heads = (ConcurrentSkipListSet<SeatWithInterval>[][]) new ConcurrentSkipListSet[coachNum + 1][seatNum + 1];
        remainSeatsNum = new AtomicInteger[stationNum][stationNum];
        for (int i = 0; i < seats.length; i++) {
            for (int j = 0; j < seats[i].length; j++) {
                seats[i][j] = new ConcurrentSkipListSet<>();
            }
        }
        for (int i = 1; i < heads.length; i++) {
            for (int j = 1; j < heads[i].length; j++) {
                heads[i][j] = new ConcurrentSkipListSet<>();
            }
        }
        for (int i = 1; i <= coachNum; i++) {
            for (int j = 1; j <= seatNum; j++) {
                SeatWithInterval seat = new SeatWithInterval(i, j, stationNum, 0, stationNum - 1);  // start 为 0 保证 heads 里面每个链表永远不为空
                seats[0][stationNum - 1].add(seat);
                heads[i][j].add(seat);
            }
        }
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
                // TODO: 2019/12/24 无法处理该线程把区间分解，并把分解后的区间加入到其他线程已经访问过的链表中的情况，因此要多次循环直到确认没有票为止
                // FIXME: 2019/12/24 死循环？
                for (int i = departure; i >= 0; i--) {
                    for (int j = arrival - 1; j <= stationNum - 1; j++) {
                        SeatWithInterval currSeat = seats[i][j].pollFirst();
                        if (currSeat != null) {
                            if (!heads[currSeat.coach][currSeat.seat].remove(currSeat)) {
                                System.out.println("heads remove error");
                            }
                            SeatOccupy newSeatOccupy;
                            while (true) {
                                SeatOccupy oldSeatOccupy = currSeat.seatOccupy.get();
                                newSeatOccupy = oldSeatOccupy.copy();
                                newSeatOccupy.occupy(departure, arrival - 1);
                                if (currSeat.seatOccupy.compareAndSet(oldSeatOccupy, newSeatOccupy)) {
                                    break;
                                }
                            }
                            updateSeatsNum(newSeatOccupy, departure, arrival, 0);
                            if (i <= departure - 1) {
                                SeatWithInterval pred = currSeat.copy(i, departure - 1);
                                seats[i][departure - 1].add(pred);
                                heads[currSeat.coach][currSeat.seat].add(pred);
                            }
                            if (arrival <= j) {
                                SeatWithInterval next = currSeat.copy(arrival, j);
                                seats[arrival][j].add(next);
                                heads[currSeat.coach][currSeat.seat].add(next);
                            }
                            coach[0] = currSeat.coach;
                            seat[0] = currSeat.seat;
                            return true;
                        }
                    }
                }
            }
        } finally {
            rLock1.unlock();
        }
        return false;
    }

    @Override
    public void put(int departure, int arrival, int coach, int seat) {
        // FIXME: 2019/12/24 方法实现有问题，死循环？
        rLock2.lock();
        try {
            SeatWithInterval currSeat = heads[coach][seat].first().copy(departure, arrival - 1);
            while (true) {
                SeatWithInterval pred = heads[coach][seat].higher(currSeat);
                SeatWithInterval next = heads[coach][seat].lower(currSeat);
                if (pred == null && next == null) {
                    break;
                }
                if (pred != null && pred.end + 1 == currSeat.start && seats[pred.start][pred.end].remove(pred)) {
                    if (!heads[coach][seat].remove(pred)) {
                        System.out.println("heads remove error");
                    }
                    currSeat.start = pred.start;
                }
                if (next != null && currSeat.end + 1 == next.start && seats[next.start][next.end].remove(next)) {
                    if (!heads[coach][seat].remove(next)) {
                        System.out.println("heads remove error");
                    }
                    currSeat.end = next.end;
                }
            }
            seats[currSeat.start][currSeat.end].add(currSeat);
            heads[coach][seat].add(currSeat);
            while (true) {
                SeatOccupy oldSeatOccupy = currSeat.seatOccupy.get();
                SeatOccupy newSeatOccupy = oldSeatOccupy.copy();
                newSeatOccupy.free(departure, arrival - 1);
                if (currSeat.seatOccupy.compareAndSet(oldSeatOccupy, newSeatOccupy)) {
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
}
