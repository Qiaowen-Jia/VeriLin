package ticketingsystem;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class Train {
    private int coachNum;
    private int seatNum;
    private int stationNum;

    private CopyOnWriteArrayList<boolean[]> seatsUse;
    private ConcurrentHashMap<Long, Ticket> soldTickets;

    private ReentrantLock seatUseLock = new ReentrantLock();
    private ReentrantLock[] seatLock;

    public Train(final int coachnum, final int seatnum, final int stationnum) {
        this.coachNum = coachnum;
        this.seatNum = seatnum;
        this.stationNum = stationnum;
        this.seatsUse = new CopyOnWriteArrayList<>();
        int totalSeat = coachnum * seatnum;

        // 整个for都要保证是一个线程在写
        try {
            seatUseLock.lock();
            for (int i = 0; i < totalSeat; i++) {
                this.seatsUse.add(new boolean[this.stationNum - 1]);
            }
        } finally {
            seatUseLock.unlock();
        }
        seatLock = new ReentrantLock[totalSeat];
        for (int i = 0; i < totalSeat; i++) {
            seatLock[i] = new ReentrantLock();
        }

        int initialCapacity = 128;
        float loadFactor = 0.5f;
        int concurrencyLevel = 2;
        soldTickets = new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel);
    }

    public int getSeat(int departure, int arrival) {
        boolean[] seat;
        for (int i = 0; i < seatsUse.size(); i++) {
            // 挨个座位检查
            try{
                seatLock[i].lock();
                seat = seatsUse.get(i);
                if (judgeAndChangeSeat(seat, departure, arrival, true))
                    return i + 1;// globalSeat从1开始
            }finally {
                seatLock[i].unlock();
            }
        }
        return -1;
    }

    public int remainSeat(int departure, int arrival) {
        int remainSeat = 0;

        boolean[] seat;
        for (int i = 0; i < seatsUse.size(); i++) {
            seat = seatsUse.get(i);
            if (judgeAndChangeSeat(seat, departure, arrival, false))
                remainSeat++;
        }
        return remainSeat;
    }

    public boolean setSeat(int seatID, int departure, int arrival) {
        boolean[] seat;
        seat = seatsUse.get(seatID - 1);
        for (int i = departure - 1; i < arrival - 1; i++) {
            if (seat[i] == true)
                try{
                    seatLock[i].lock();
                    seat[i] = false;
                }finally {
                    seatLock[i].unlock();
                }
            else
                return false;
        }
        seatsUse.set(seatID - 1, seat);
        return true;

    }

    private boolean judgeAndChangeSeat(boolean[] seat, int departure, int arrival, boolean change) {
        for (int i = departure - 1; i < arrival - 1; i++) {
            if (seat[i] == true) return false;
        }
        // 修改座位占用状态
        if (change) {
            for (int i = departure - 1; i < arrival - 1; i++) {
                seat[i] = true;
            }
        }
        return true;
    }

    public final boolean containAndRemove(Ticket ticket) {

        Ticket containTicket = soldTickets.get(ticket.tid);
        if (containTicket == null || !ticketEquals(ticket, containTicket)) {
            return false;
        }
        soldTickets.remove(ticket.tid);
        return true;
    }

    public final void addSoldTicket(Ticket ticket) {
        soldTickets.put(ticket.tid, ticket);
    }

    private final boolean ticketEquals(Ticket x, Ticket y) {
        if (x == y) return true;
        if (x == null || y == null) return false;

        return (
                (x.tid == y.tid) &&
                        (x.passenger.equals(y.passenger)) &&
                        (x.route == y.route) &&
                        (x.coach == y.coach) &&
                        (x.seat == y.seat) &&
                        (x.departure == y.departure) &&
                        (x.arrival == y.arrival)
        );
    }
}