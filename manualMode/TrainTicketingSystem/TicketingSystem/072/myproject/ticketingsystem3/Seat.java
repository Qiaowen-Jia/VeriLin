package ticketingsystem;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Seat {
    int stationNum;
    boolean[] occupied;

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    Seat(int stationNum) {
        this.stationNum = stationNum;
        this.occupied = new boolean[stationNum];
        for (int i = 0; i < stationNum; ++i) {
            this.occupied[i] = false;
        }
    }

    boolean queryVacant(int departure, int arrival) {
        readLock.lock();
        try {
            // caveat: range is not exclusive on both sides!
            for (int k = departure - 1; k < arrival - 1; ++k) {
                if (occupied[k]) {
                    return false;
                }
            }
        } finally {
            readLock.unlock();
        }
        return true;
    }

    boolean queryVacantLockFree(int departure, int arrival) {
        for (int k = departure - 1; k < arrival - 1; ++k) {
            if (occupied[k]) {
                return false;
            }
        }
        return true;
    }

    boolean queryOccupied(int departure, int arrival) {
        readLock.lock();
        try {
            for (int k = departure - 1; k < arrival - 1; ++k) {
                if (!occupied[k]) {
                    return false;
                }
            }
        } finally {
            readLock.unlock();
        }
        return true;
    }

    boolean queryOccupiedLockFree(int departure, int arrival) {
        for (int k = departure - 1; k < arrival - 1; ++k) {
            if (!occupied[k]) {
                return false;
            }
        }
        return true;
    }

    boolean occupy(int departure, int arrival) {
        writeLock.lock();
        try {
            // double check
            if (queryOccupiedLockFree(departure, arrival)) {
                for (int k = departure - 1; k < arrival - 1; ++k) {
                    occupied[k] = true;
                }
            }
            else {
                return false;
            }
        } finally {
            writeLock.unlock();
        }
        return true;
    }

    boolean vacate(int departure, int arrival) {
        writeLock.lock();
        try {
            // double check
            if (queryVacantLockFree(departure, arrival)) {
                for (int k = departure - 1; k < arrival - 1; ++k) {
                    occupied[k] = false;
                }
            }
            else {
                return false;
            }
        } finally {
            writeLock.unlock();
        }
        return true;
    }
}
