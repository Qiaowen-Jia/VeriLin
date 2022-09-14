package ticketingsystem;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

// 每趟列车的余票计数器
public abstract class TrainRemainTicketCounter {

    protected int maxStationnum;

    public abstract int inquiryRemainTicket(int departure, int arrival);

    public abstract boolean buyRange(int departure, int arrival, Seat seat);

    public abstract boolean refundRange(int departure, int arrival, Seat seat);

    protected boolean rangeLegalCheck(int departure, int arrival) {
        // 检查区间合法性
        return departure >= 1 && arrival <= maxStationnum && (arrival - departure > 0);
    }

    protected int rangeToIndex(int departure, int arrival) {
        return (departure - 1) * maxStationnum + (arrival - 1);
    }
}

class SeatLevelAtomicRemainTicketCounter extends TrainRemainTicketCounter {
    private AtomicInteger[] counterboard;

    SeatLevelAtomicRemainTicketCounter(int stationnum, int coachnum, int seatnum) {
        this.maxStationnum = stationnum;
        int rangeCount = stationnum * stationnum; // 这里浪费了一半内存
        this.counterboard = new AtomicInteger[rangeCount];
        for (int i = 0; i < rangeCount; i++) {
            this.counterboard[i] = new AtomicInteger(coachnum * seatnum);
        }
    }


    @Override
    public int inquiryRemainTicket(int departure, int arrival) {
        if (!this.rangeLegalCheck(departure, arrival)) {
            // 区间不合法直接返回0
            return 0;
        }
        return this.counterboard[rangeToIndex(departure, arrival)].get();
    }

    private boolean modifyRange(int departure, int arrival, boolean isBuy, Seat seat) {
        if (!this.rangeLegalCheck(departure, arrival)) {
            return false;
        }
        for (int d = 1; d < maxStationnum; d++) {
            for (int a = d + 1; a <= maxStationnum; a++) {
                if (!rangeLegalCheck(d, a)) {
                    continue;
                }
                if (d < arrival && a > departure) {
                    if (seat.isRangeOccupied(d, a)) {
                        continue; // 之前已经记录过了，不需要再修改
                    }
                    if (isBuy) {
                        this.counterboard[rangeToIndex(d, a)].decrementAndGet();
                    } else {
                        this.counterboard[rangeToIndex(d, a)].incrementAndGet();
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean buyRange(int departure, int arrival, Seat seat) {
        return this.modifyRange(departure, arrival, true, seat);
    }

    @Override
    public boolean refundRange(int departure, int arrival, Seat seat) {
        return this.modifyRange(departure, arrival, false, seat);
    }
}

class SeatLevelLongAdderRemainTicketCounter extends TrainRemainTicketCounter {
    private LongAdder[] counterboard;

    SeatLevelLongAdderRemainTicketCounter(int stationnum, int coachnum, int seatnum) {
        this.maxStationnum = stationnum;
        int rangeCount = stationnum * stationnum; // 这里浪费了一半内存
        this.counterboard = new LongAdder[rangeCount];
        for (int i = 0; i < rangeCount; i++) {
            this.counterboard[i] = new LongAdder();
            this.counterboard[i].add(coachnum * seatnum);
        }
    }

    @Override
    public int inquiryRemainTicket(int departure, int arrival) {
        if (!this.rangeLegalCheck(departure, arrival)) {
            // 区间不合法直接返回0
            return 0;
        }
        return this.counterboard[rangeToIndex(departure, arrival)].intValue();
    }

    private boolean modifyRange(int departure, int arrival, boolean isBuy, Seat seat) {
        if (!this.rangeLegalCheck(departure, arrival)) {
            return false;
        }
        for (int d = 1; d < maxStationnum; d++) {
            for (int a = d + 1; a <= maxStationnum; a++) {
                if (!rangeLegalCheck(d, a)) {
                    continue;
                }
                if (d < arrival && a > departure) {
                    if (seat.isRangeOccupied(d, a)) {
                        continue; // 之前已经记录过了，不需要再修改
                    }
                    if (isBuy) {
                        this.counterboard[rangeToIndex(d, a)].decrement();
                    } else {
                        this.counterboard[rangeToIndex(d, a)].increment();
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean buyRange(int departure, int arrival, Seat seat) {
        return this.modifyRange(departure, arrival, true, seat);
    }

    @Override
    public boolean refundRange(int departure, int arrival, Seat seat) {
        return this.modifyRange(departure, arrival, false, seat);
    }
}

class SeatLevelReadWriteRemainTicketCounter extends TrainRemainTicketCounter {
    private int[] counterboard;
    private ReentrantReadWriteLock lock;

    SeatLevelReadWriteRemainTicketCounter(int stationnum, int coachnum, int seatnum) {
        this.maxStationnum = stationnum;
        int rangeCount = stationnum * stationnum; // 这里浪费了一半内存
        this.counterboard = new int[rangeCount];
        this.lock = new ReentrantReadWriteLock(true);
        for (int i = 0; i < rangeCount; i++) {
            this.counterboard[i] = coachnum * seatnum;
        }
    }


    @Override
    public int inquiryRemainTicket(int departure, int arrival) {
        if (!this.rangeLegalCheck(departure, arrival)) {
            // 区间不合法直接返回0
            return 0;
        }
        this.lock.readLock().lock();
        try{
            return this.counterboard[rangeToIndex(departure, arrival)];
        } finally {
            this.lock.readLock().unlock();
        }

    }

    private boolean modifyRange(int departure, int arrival, boolean isBuy, Seat seat) {
        if (!this.rangeLegalCheck(departure, arrival)) {
            return false;
        }
        this.lock.writeLock().lock();
        try {
            for (int d = 1; d < maxStationnum; d++) {
                for (int a = d + 1; a <= maxStationnum; a++) {
                    if (!rangeLegalCheck(d, a)) {
                        continue;
                    }
                    if (d < arrival && a > departure) {
                        if (seat.isRangeOccupied(d, a)) {
                            continue; // 之前已经记录过了，不需要再修改
                        }
                        if (isBuy) {
                            this.counterboard[rangeToIndex(d, a)]--;
                        } else {
                            this.counterboard[rangeToIndex(d, a)]++;
                        }
                    }
                }
            }
        } finally {
            this.lock.writeLock().unlock();
        }
        return true;
    }

    @Override
    public boolean buyRange(int departure, int arrival, Seat seat) {
        return this.modifyRange(departure, arrival, true, seat);
    }

    @Override
    public boolean refundRange(int departure, int arrival, Seat seat) {
        return this.modifyRange(departure, arrival, false, seat);
    }
}

class SeatLevelFCRemainTicketCounter extends TrainRemainTicketCounter {
    private int[][] counterboard;
    private StampedLock[] threadLock;
    private StampedLock lock;
    private int amountTicket;
    private int threadnum;

    SeatLevelFCRemainTicketCounter(int stationnum, int coachnum, int seatnum, int threadnum) {
        this.maxStationnum = stationnum;
        this.threadnum = threadnum;
        int rangeCount = stationnum * stationnum; // 这里浪费了一半内存
        this.amountTicket = coachnum * seatnum;
        this.threadLock = new StampedLock[threadnum];
        this.counterboard = new int[threadnum][rangeCount];
        this.lock = new StampedLock();
        for (int i = 0; i < threadnum; i++) {
            this.threadLock[i] = new StampedLock();
            for(int j = 0; j < rangeCount; j++) {
                this.counterboard[i][j] = 0;
            }
        }
    }


    @Override
    public int inquiryRemainTicket(int departure, int arrival) {
        if (!this.rangeLegalCheck(departure, arrival)) {
            // 区间不合法直接返回0
            return 0;
        }
        int delta = 0, threadDelta=0;
        long stamp, counter=0;
        for(int i=0; i < this.threadnum; i++){
            counter = 0;
            do {
                counter++;
                stamp = this.threadLock[i].tryOptimisticRead();
                threadDelta = this.counterboard[i][rangeToIndex(departure, arrival)];
            } while (!this.threadLock[i].validate(stamp));
            //System.out.println(counter);
            delta += threadDelta;
        }
        return this.amountTicket + delta;
    }

    private boolean modifyRange(int departure, int arrival, boolean isBuy, Seat seat) {
        if (!this.rangeLegalCheck(departure, arrival)) {
            return false;
        }
        int threadNr = MyThreadId.get() % this.threadnum;
        long stamp = this.threadLock[threadNr].writeLock();
        try {
            for (int d = 1; d < maxStationnum; d++) {
                for (int a = d + 1; a <= maxStationnum; a++) {
                    if (!rangeLegalCheck(d, a)) {
                        continue;
                    }
                    if (d < arrival && a > departure) {
                        if (seat.isRangeOccupied(d, a)) {
                            continue; // 之前已经记录过了，不需要再修改
                        }
                        if (isBuy) {
                            this.counterboard[threadNr][rangeToIndex(d, a)]--;
                        } else {
                            this.counterboard[threadNr][rangeToIndex(d, a)]++;
                        }
                    }
                }
            }
            return true;
        } finally {
            this.threadLock[threadNr].unlockWrite(stamp);
        }
    }

    @Override
    public boolean buyRange(int departure, int arrival, Seat seat) {
        return this.modifyRange(departure, arrival, true, seat);
    }

    @Override
    public boolean refundRange(int departure, int arrival, Seat seat) {
        return this.modifyRange(departure, arrival, false, seat);
    }
}

class SeatLevelFCStampedRemainTicketCounter extends TrainRemainTicketCounter {
    private int[][] counterboard;
    private StampedLock[] threadLock; // 线程间同步
    private AtomicInteger stamp;
    private int amountTicket;
    private int threadnum;

    SeatLevelFCStampedRemainTicketCounter(int stationnum, int coachnum, int seatnum, int threadnum) {
        this.maxStationnum = stationnum;
        this.threadnum = threadnum;
        int rangeCount = stationnum * stationnum; // 这里浪费了一半内存
        this.amountTicket = coachnum * seatnum;
        this.threadLock = new StampedLock[threadnum];
        this.counterboard = new int[threadnum][rangeCount];
        this.stamp = new AtomicInteger(0);
        for (int i = 0; i < threadnum; i++) {
            this.threadLock[i] = new StampedLock();
            for(int j = 0; j < rangeCount; j++) {
                this.counterboard[i][j] = 0;
            }
        }
    }

    @Override
    public int inquiryRemainTicket(int departure, int arrival) {
        if (!this.rangeLegalCheck(departure, arrival)) {
            // 区间不合法直接返回0
            return 0;
        }
        int delta = 0, threadDelta=0;
        long threadStamp, globalStamp ,counter=0;

        do {
            delta = 0;
            globalStamp = stamp.get();
            for(int i=0; i < this.threadnum; i++){
                do {
                    threadStamp = this.threadLock[i].tryOptimisticRead();
                    threadDelta = this.counterboard[i][rangeToIndex(departure, arrival)];
                } while (!this.threadLock[i].validate(threadStamp));
                delta += threadDelta;
            }
        } while (globalStamp != stamp.get());

        return this.amountTicket + delta;
    }

    private boolean modifyRange(int departure, int arrival, boolean isBuy, Seat seat) {
        if (!this.rangeLegalCheck(departure, arrival)) {
            return false;
        }
        int threadNr = MyThreadId.get() % this.threadnum;
        long threadStamp = this.threadLock[threadNr].writeLock();
        try {
            for (int d = 1; d < maxStationnum; d++) {
                for (int a = d + 1; a <= maxStationnum; a++) {
                    if (!rangeLegalCheck(d, a)) {
                        continue;
                    }
                    if (d < arrival && a > departure) {
                        if (seat.isRangeOccupied(d, a)) {
                            continue; // 之前已经记录过了，不需要再修改
                        }
                        if (isBuy) {
                            this.counterboard[threadNr][rangeToIndex(d, a)]--;
                        } else {
                            this.counterboard[threadNr][rangeToIndex(d, a)]++;
                        }
                    }
                }
            }
            stamp.getAndIncrement();
            return true;
        } finally {
            this.threadLock[threadNr].unlockWrite(threadStamp);
        }
    }

    @Override
    public boolean buyRange(int departure, int arrival, Seat seat) {
        return this.modifyRange(departure, arrival, true, seat);
    }

    @Override
    public boolean refundRange(int departure, int arrival, Seat seat) {
        return this.modifyRange(departure, arrival, false, seat);
    }
}

class CoachLevelRemainTicketHint extends TrainRemainTicketCounter{
    private int[][] counterboard;
    private int amountTicket;
    private int coachnum;
    private int seatnum;
    private int seatAmount;
    private ThreadLocalRandom rand;
    CoachLevelRemainTicketHint(int stationnum, int coachnum, int seatnum, int threadnum) {
        this.maxStationnum = stationnum;
        int rangeCount = stationnum * stationnum; // 这里浪费了一半内存
        this.amountTicket = coachnum * seatnum;
        this.coachnum = coachnum;
        this.seatnum = seatnum;
        this.seatAmount = coachnum * seatnum;
        this.counterboard = new int[coachnum][rangeCount];
        this.rand = ThreadLocalRandom.current();
        for (int i = 0; i < coachnum; i++) {
            for(int j=0; j < rangeCount; j++)
                this.counterboard[i][j] = amountTicket;
        }
    }
    private boolean modifyRange(int departure, int arrival, boolean isBuy, Seat seat, int seatIndex) {
        if (!this.rangeLegalCheck(departure, arrival)) {
            return false;
        }
        int coachIndex = seatIndex / this.seatnum;
        for (int d = 1; d < maxStationnum; d++) {
            for (int a = d + 1; a <= maxStationnum; a++) {
                if (!rangeLegalCheck(d, a)) {
                    continue;
                }
                if (d < arrival && a > departure) {
                    if (seat.isRangeOccupied(d, a)) {
                        continue; // 之前已经记录过了，不需要再修改
                    }
                    if (isBuy) {
                        this.counterboard[coachIndex][rangeToIndex(d, a)]--;
                    } else {
                        this.counterboard[coachIndex][rangeToIndex(d, a)]++;
                    }
                }
            }
        }
        return true;
    }

    public boolean buyRange(int departure, int arrival, Seat seat, int seatIndex) {
        return this.modifyRange(departure, arrival, true, seat, seatIndex);
    }

    public boolean refundRange(int departure, int arrival, Seat seat, int seatIndex) {
        return this.modifyRange(departure, arrival, false, seat, seatIndex);
    }

    public int hintSeatIndex(int departure, int arrival){
        int coachStartPoint = this.rand.nextInt(this.coachnum);
        int coachIndex, coachRemain;
        int maxRemain=-10, maxRemainCoach=0;
        for(int i=0; i<this.coachnum; i++){
            coachIndex = (coachStartPoint + i)%this.coachnum;
            coachRemain = this.counterboard[coachIndex][rangeToIndex(departure, arrival)];
            if(coachRemain > maxRemain){
                // 这个车厢这个区间有空座
                maxRemain = coachRemain;
                maxRemainCoach = coachIndex;
            }
        }
        return maxRemainCoach * this.seatnum + this.rand.nextInt(this.seatnum << 1);
        // 什么？都没有空座？
        // return this.rand.nextInt(this.seatAmount);
    }

    @Override
    public int inquiryRemainTicket(int departure, int arrival) {
        return 0;
    }

    @Override
    public boolean buyRange(int departure, int arrival, Seat seat) {
        return false;
    }

    @Override
    public boolean refundRange(int departure, int arrival, Seat seat) {
        return false;
    }
}