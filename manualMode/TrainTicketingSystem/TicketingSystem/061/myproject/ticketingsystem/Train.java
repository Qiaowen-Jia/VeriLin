package ticketingsystem;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Train {
    private AtomicLong[] seats;
    private final int coachNum; //车厢数
    private final int seatNum; //每个车厢的座位数
    private final int stationNum; //车站数
    private final int allSeatsNum; //本次列车总的座位数
    private Lock lock = new ReentrantLock();

    public Train(final int coachNum, final int seatNum, final int stationNum){
        this.coachNum = coachNum;
        this.seatNum = seatNum;
        this.stationNum = stationNum;
        allSeatsNum = (coachNum * seatNum) << 3;   //填充成cacheline的大小64字节，避免乒乓效应

        this.seats = new AtomicLong[allSeatsNum];
        for(int i = 0; i < allSeatsNum; i+=8)
            this.seats[i] = new AtomicLong(0);
    }

    public int lockSeat(final int departure, final int arrival){
        long mask = getMask(departure, arrival);
        for(int i = 0; i < allSeatsNum; i+=8){
            long tmp = seats[i].get();
            while((mask & tmp) == 0){   //可能某个倒霉的线程锁定座位A时，被另外一个不停锁定和释放座位A的线程打扰，以至于饿死
                if(seats[i].compareAndSet(tmp, (tmp | mask))){
                //    System.out.println("Train::lockSeat::" + (i >> 3));
                    return i >> 3;
                }
                tmp = seats[i].get();
            }
        }
        return -1;
    }

    /*num: 座位号（整个火车座位在一起编号）*/
    public boolean unlockSeat(final int num, final int departure, final int arrival){
        long mask = getMask(departure, arrival);
        while(true){
            long tmp = seats[num << 3].get();
            if(seats[num << 3].compareAndSet(tmp, (tmp & ~mask)))
                return true;
        }
    }

    public int querySeat(final int departure, final int arrival){
        lock.lock();
        int availableSeatsNum = 0;
        long mask = getMask(departure, arrival);
        try {
            for(int i = 0; i < allSeatsNum; i+=8){
            //    System.out.println("Train::querySeat::seat[" + (i>>3) + "]:" + Long.toBinaryString(seats[i].get()) + "::::mask::::" + Long.toBinaryString(mask));
                if((mask & seats[i].get()) == 0){
                    availableSeatsNum += 1;
                 //   System.out.println("Train::querySeat::" + (i >> 3));
                }

            }
        } finally {
            lock.unlock();
        }
        return availableSeatsNum;
    }

    private long getMask(final int departure, final int arrival){
        long mask = (0x01 << (arrival - departure)) - 1;
        mask = mask << departure;
        return mask;
    }
}
