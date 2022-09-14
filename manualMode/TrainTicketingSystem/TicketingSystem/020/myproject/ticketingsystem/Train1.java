package ticketingsystem;

import java.util.concurrent.atomic.AtomicReference;

public class Train1 implements Train {
    private SeatWithMissTimes[][] seats;
    private AtomicReference<SeatWithMissTimes> head;
    // TODO: 2019/12/22 lock

    Train1(int coachNum, int seatNum, int stationNum) {
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
    }

    @Override
    public int inquiry(int departure, int arrival) {
        SeatWithMissTimes headSeat = head.get();
        SeatWithMissTimes currSeat = headSeat;
        int result = 0;
        do {
            if (currSeat.seatOccupy.get().notOccupied(departure, arrival - 1)) {
                result++;
            }
            currSeat = currSeat.next;
        } while (currSeat != headSeat);
        return result;
    }

    @Override
    public boolean get(int departure, int arrival, int[] coach, int[] seat) {
        SeatWithMissTimes headSeat = head.get();
        SeatWithMissTimes currSeat = headSeat;
        SeatOccupy oldSeatOccupy;
        do {
            while ((oldSeatOccupy = currSeat.seatOccupy.get()).notOccupied(departure, arrival - 1)) {
                SeatOccupy newSeatOccupy = oldSeatOccupy.copy();
                newSeatOccupy.occupy(departure, arrival - 1);
                if (currSeat.seatOccupy.compareAndSet(oldSeatOccupy, newSeatOccupy)) {
                    currSeat.maxMissTimes.getAndAdd(-(arrival - departure) * currSeat.missTimesPerStation);
                    coach[0] = currSeat.coach;
                    seat[0] = currSeat.seat;
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
        return false;
    }

    @Override
    public void put(int departure, int arrival, int coach, int seat) {
        SeatWithMissTimes currSeat = seats[coach][seat];
        while (true) {
            SeatOccupy oldSeatOccupy = currSeat.seatOccupy.get();
            SeatOccupy newSeatOccupy = oldSeatOccupy.copy();
            newSeatOccupy.free(departure, arrival - 1);
            if (currSeat.seatOccupy.compareAndSet(oldSeatOccupy, newSeatOccupy)) {
                currSeat.maxMissTimes.getAndAdd((arrival - departure) * currSeat.missTimesPerStation);
                return;
            }
        }
    }

    public static void main(String[] args) {
        Train1 train1 = new Train1(4, 5, 6);
        SeatWithMissTimes head = train1.head.get();
        do {
            System.out.println(head.coach + " " + head.seat);
            head = head.next;
        } while (head != train1.head.get());
    }
}
