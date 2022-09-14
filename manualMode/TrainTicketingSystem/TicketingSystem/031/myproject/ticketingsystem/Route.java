package ticketingsystem;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Route {
    //记录本趟列车每个区间余票，下标表示与Seat区间一致，查询时直接查rest
    public AtomicInteger[] rest;
//    public int[] rest;
    //买票退票时修改余票时用到的锁，确保修改操作不被打断
//    public Object[] restLocks;
    //买票查询同步锁
//    public final Object inqAndBuyLock;
    //退票查询同步锁
//    public final Object inqAndRefLock;
    //本趟列车的所有座位
    public Seat[] seats;

    protected final ReadWriteLock rwLock;
    protected final Lock readLock;
    protected final Lock writeLock;

    public Route(int stationnum, int coachnum, int seatnum){
        int restCapacity = stationnum * (stationnum - 1) / 2;
        rest = new AtomicInteger[restCapacity];
//        rest = new int[restCapacity];
//        restLocks = new Object[restCapacity];
        for (int i = 0; i < restCapacity; i++) {
            rest[i] = new AtomicInteger(coachnum * seatnum);
//            rest[i] = coachnum * seatnum;
//            restLocks[i] = new Object();
        }
        //总共coachnum * seatnum个座位，车厢号为座位编号seatNo / seatnum + 1，座位号为座位编号seatNo % seatnum + 1
        int seatCapacity = coachnum * seatnum;
        seats = new Seat[seatCapacity];
        for (int j = 0; j < seatCapacity; j++) {
            seats[j] = new Seat(stationnum);
        }
//        inqAndBuyLock = new Object();
//        inqAndRefLock = new Object();

        rwLock = new ReentrantReadWriteLock();
        readLock = rwLock.readLock();
        writeLock = rwLock.writeLock();
    }
}
