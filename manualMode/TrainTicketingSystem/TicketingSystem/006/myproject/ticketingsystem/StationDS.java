package ticketingsystem;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicStampedReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// it's mainly about which seats are available in that station
// should use some data structure like a segment tree

class StationDS
{

    private int coachNum, seatNum;
    // seat available? true : false
    public BitSet seatBitMap;
    public StationDS(int coachNum, int seatNum)
    {
        this.coachNum = coachNum;
        this.seatNum = seatNum;
        this.seatBitMap = new BitSet(coachNum * seatNum);
        this.lock = new ReentrantLock();
        //this.lock = new ReentrantReadWriteLock();
    }
    public ReentrantLock lock;
    //public ReentrantReadWriteLock lock;
}

/*

*/




