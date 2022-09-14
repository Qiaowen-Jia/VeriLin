package ticketingsystem;

import java.util.*;
import java.util.concurrent.locks.*;

public class SeatLock {
  boolean[] flag;
  ReentrantReadWriteLock lock; // optimistic lock, used only when buy/refund finds an available seat

  public SeatLock(int stationNum, boolean fair) {
    flag = new boolean[stationNum - 1];
    lock = new ReentrantReadWriteLock(fair);
    for (int i = 0; i < (stationNum - 1); i++)
      flag[i] = false;
  }

  public Lock readLock() {
    return lock.readLock();
  }

  public Lock writeLock() {
    return lock.writeLock();
  }

  public boolean isAvailable(int begin, int end) {
    for (int i = begin; i <= end; i++) {
      if (flag[i])
        return false;
    }
    return true;
  }

  public void set(int begin, int end) {
    for (int i = begin; i <= end; i++)
      flag[i] = true;
  }

  public void unset(int begin, int end) {
    for (int i = begin; i <= end; i++)
      flag[i] = false;
  }

  public boolean lockAndSet(int begin, int end) {
    try {
      this.writeLock().lock();
      if (this.isAvailable(begin, end)) {
        set(begin, end);
        return true;
      } else
        return false; // validate fail
    } finally {
      this.writeLock().unlock();
    }
  }

  public void lockAndUnset(int begin, int end) {
    try {
      this.writeLock().lock();
      unset(begin, end);
    } finally {
      this.writeLock().unlock();
    }
  }

}