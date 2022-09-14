package ticketingsystem;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Seat {
	int stationNum;
	boolean[] stationState;
	ReadWriteLock lock = new ReentrantReadWriteLock();
	Lock readlock = lock.readLock();
	Lock writelock = lock.writeLock();
	
	Seat(int stationnum) {
		this.stationNum = stationnum;
		stationState = new boolean[stationNum];
		for(int i=0;i<stationNum;i++) {
			stationState[i] = false;
		}
		
	}

	boolean canWrite(int departure, int arrival) {
		int k=0;
		writelock.lock();
		for(k=departure;k<arrival;k++) {
			if(stationState[k-1]) {
				writelock.unlock();
				return false;
			}
		}
		for(k=departure;k<arrival;k++) {
			stationState[k-1]=true;
		}
		writelock.unlock();
		return true;
	}
	
	boolean canRead(int departure, int arrival) {
		readlock.lock();
		for(int k=departure;k<arrival;k++) {
			if(stationState[k-1]) {
				readlock.unlock();
				return false;
			}
		}
		readlock.unlock();
		return true;
	}
	
	boolean releaseSeat(int departure, int arrival) {
		writelock.lock();
		for(int i=departure;i<arrival;i++) {
			stationState[i-1]=false;
		}
		writelock.unlock();	
		return true;
	}
}
