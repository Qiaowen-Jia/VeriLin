package ticketingsystem;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Simple2ReadLock {
    private Lock lock;
    private Condition condition;
    private int[] readers;
    private SimpleReadLock[] readLocks;

    Simple2ReadLock() {
        lock = new ReentrantLock();
        condition = lock.newCondition();
        readers = new int[2];
        readLocks = new SimpleReadLock[2];
        for (int i = 0; i < 2; i++) {
            readLocks[i] = new SimpleReadLock(i);
        }
    }

    SimpleReadLock getReadLock(int index) {
        return readLocks[index];
    }

    class SimpleReadLock {
        private int index;

        SimpleReadLock(int index) {
            this.index = index;
        }

        void lock() {
            lock.lock();
            try {
                while (readers[1 - index] > 0) {
                    condition.await();
                }
                readers[index]++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }

        void unlock() {
            lock.lock();
            try {
                readers[index]--;
                if (readers[index] == 0) {
                    condition.signalAll();
                }
            } finally {
                lock.unlock();
            }
        }
    }
}
