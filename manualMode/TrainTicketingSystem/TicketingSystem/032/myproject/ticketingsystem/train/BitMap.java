package ticketingsystem.train;

import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BitMap {
	private ReentrantReadWriteLock rwLock;
	private ReadLock readLock;
	private WriteLock writeLock;
	private int seat_size;
	private AtomicLongArray map;
    private static final int longSize = Long.SIZE;
    
    public BitMap(int size) {
    	rwLock = new ReentrantReadWriteLock();
    	readLock = rwLock.readLock();
    	writeLock = rwLock.writeLock();
    	seat_size = size;
        int mapSize = (seat_size + longSize - 1) / longSize;
        map = new AtomicLongArray(mapSize);
        int remainSize = mapSize * longSize - seat_size;
        map.set(map.length() - 1, BitMethod.setRange(map.get(map.length() - 1), longSize - remainSize, longSize));
    }

    public void lock(int flag) {
    	if(flag == 0) {
    		this.readLock.lock();
    	}
    	else if(flag == 1) {
    		this.writeLock.lock();
    	}
	}
	public void unlock(int flag) {
		if(flag == 0) {
    		this.readLock.unlock();
    	}
    	else if(flag == 1) {
    		this.writeLock.unlock();
    	}
	}
    
    public long[] bitSnapshot() {
        long[] res = new long[map.length()];
        for (int i = 0; i < res.length; ++i)
            res[i] = map.get(i);
        return res;
    }
    public void set_one(int index){
        int mapIndex = index / longSize;
        int i = index % longSize;
        while (true) {
            long oldValue = map.get(mapIndex);
            long newValue = BitMethod.set(oldValue, i);
            if (map.compareAndSet(mapIndex, oldValue, newValue))
                break;
        }
    }
    public void set_zero(int index) {
        int mapIndex = index / longSize;
        int i = index % longSize;
        while (true) {
            long oldValue = map.get(mapIndex);
            long newValue = BitMethod.reset(oldValue, i);
            if (map.compareAndSet(mapIndex, oldValue, newValue))
                break;
        }
    }
    
    public boolean read_one(int index){
        int mapIndex = index / longSize;
        int i = index % longSize;
        long oldValue = map.get(mapIndex);
        return (oldValue & (1L << i))!=0;
    }
    
}
