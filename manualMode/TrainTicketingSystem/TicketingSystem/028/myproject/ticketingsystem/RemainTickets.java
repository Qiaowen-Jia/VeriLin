package ticketingsystem;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RemainTickets {
    int stationnum;
    int[] remainArray;
    AtomicInteger readnum;
    ReadWriteLock lock;

    private final static int flag = 0x10000;

    public Boolean haveFlag(int readnum) {
        return (readnum & flag) == flag;
    }

    public int getIndex(int departure, int arrival) {
        return departure * (stationnum - 1) + arrival - 1;
    }

    public RemainTickets(int stationnum, int seatnum) {
        lock = new ReentrantReadWriteLock();
        this.stationnum = stationnum;
        remainArray = new int[(stationnum - 1) * (stationnum - 1)];
        for (int departure = 0; departure < stationnum - 1; departure++) {
            for (int arrival = departure + 1; arrival < stationnum; arrival++) {
                remainArray[getIndex(departure, arrival)] = seatnum;
            }
        }
        readnum = new AtomicInteger(0);
    }

    public int get(int departure, int arrival) {
        lock.readLock().lock();
        int index = getIndex(departure, arrival);
        int remain = remainArray[index];
        lock.readLock().unlock();
        return remain;
    }

    public int getV2(int departure, int arrival) {
        int num;
        do {
            num = readnum.get();
            while (haveFlag(num)) {
                num = readnum.get();
            }
        } while (!readnum.compareAndSet(num, num + 1));
        int index = getIndex(departure, arrival);
        int remain = remainArray[index];
        readnum.getAndDecrement();
        return remain;
    }

    public int tryGet(int departure, int arrival) {
        if (lock.readLock().tryLock()) {
            int index = getIndex(departure, arrival);
            int remain = remainArray[index];
            lock.readLock().unlock();
            return remain;
        }
        return -1;
    }

    public int tryGetV2(int departure, int arrival) {
        if (haveFlag(readnum.getAndIncrement())) {
            readnum.getAndDecrement();
            return -1;
        }
        int index = getIndex(departure, arrival);
        int remain = remainArray[index];
        readnum.getAndDecrement();
        return remain;
    }

    public void update(int departure, int arrival, int startStation, int terminalStation, int inc) {
        lock.writeLock().lock();
        for (int start = startStation; start < arrival; start++) {
            int terminal = Math.max(departure + 1, start + 1);
            for (; terminal <= terminalStation; terminal++) {
                int index = getIndex(start, terminal);
                remainArray[index] += inc;
            }
        }
        lock.writeLock().unlock();
    }

    public void updateV2(int departure, int arrival, int startStation, int terminalStation, int inc) {
        int num;
        do {
            num = readnum.get();
            while (num >= 1) {
                num = readnum.get();
            }
        } while (!readnum.compareAndSet(num, flag));
        for (int start = startStation; start < arrival; start++) {
            int terminal = Math.max(departure + 1, start + 1);
            for (; terminal <= terminalStation; terminal++) {
                int index = getIndex(start, terminal);
                remainArray[index] += inc;
            }
        }
        do {
            num = readnum.get();
        } while (!readnum.compareAndSet(num, num & ~flag));
    }
}
