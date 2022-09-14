package ticketingsystem;

import java.util.Arrays;
import java.util.*;

import java.util.concurrent.atomic.AtomicInteger;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;

public class RouteDS {
    private int coachnum;        //车厢数量
    private int seatnum;         //座位数量
    private int stationnum;      //经停站数量
    private SeatDS[] seats;

    private int cacheSize;
    private int[] cacheRoute;   //大小n*n，用于索引i到j有多少票

    private ReentrantReadWriteLock routeRWLock;
    private Lock routeRdLock;
    private Lock routeWrLock;
    public Lock modifyLock;

    private static final boolean BUY = false;
    private static final boolean REFUND = true;
    private static final int NotUsed = -10;
    private static final int WrongHere = -20;

    public RouteDS(int coachnum, int seatnum, int stationnum) {
        this.coachnum = coachnum;
        this.seatnum = seatnum;
        this.stationnum = stationnum;
        this.cacheSize = this.stationnum * this.stationnum;

        this.seats = new SeatDS[coachnum * seatnum + 1]; //从1开始索引 直到seats[coachnum*seatnum]

        for (int i = 1; i <= coachnum * seatnum; i++) {
            this.seats[i] = new SeatDS(stationnum);
        }   

        this.cacheRoute = new int[this.cacheSize]; 
        Arrays.fill(cacheRoute, NotUsed);

        LockInit();
        cacheInit();
    }

    private void LockInit() {
        routeRWLock = new ReentrantReadWriteLock();
        routeRdLock = routeRWLock.readLock();
        routeWrLock = routeRWLock.writeLock();
        modifyLock = new ReentrantLock();
    }

    private void cacheInit() {
        int max_seats = this.coachnum * this.seatnum;
        for(int i=1; i<this.stationnum; ++i) {
            for(int j=i+1; j<=this.stationnum; ++j) {
                this.cacheRoute[ (i-1)*this.stationnum + j - 1] = max_seats;
            }
        }
    }
    
    public int inquiry(int departure, int arrival) {
        int idx_route = (departure - 1) * this.stationnum + arrival - 1;
        routeRdLock.lock();
        try {
            if (this.cacheRoute[idx_route] != NotUsed) {
                return this.cacheRoute[idx_route];
            }
        } finally {
            routeRdLock.unlock();
        } 
        return WrongHere;
    }
    
    
    //key为例如从1，2，就为12.成功的话返回[coachNo,seatidx]
    public int[] buyTicket(int departure, int arrival) {
        int idx_route = (departure - 1) * this.stationnum + arrival - 1;
        int availableSeats = 0;
        int seatidx = 1;
        boolean haveTicket = false;
        boolean needRetry = false;
        boolean[] buyArray = new boolean[this.cacheSize];

        while(true) {
            routeRdLock.lock();
            try {
                if (this.cacheRoute[idx_route] == 0) {
                    return null;
                }
            } finally {
                routeRdLock.unlock();
            } 
            
            for (seatidx = 1; seatidx < this.seats.length; seatidx++) {    // i索引到第一个a到b的有座的座位
                if (this.seats[seatidx].isAvailable(departure, arrival)) { 
                    modifyLock.lock();
                    try {
                        buyArray = this.seats[seatidx].modify(departure, arrival, BUY);
                        if(buyArray == null) {
                            needRetry = true;
                            break;
                        } 
                        flushRouteCache(buyArray,BUY);  
                    } finally {
                        modifyLock.unlock();
                    }
                
                    haveTicket = true;
                    break;
                }
            } 
            if(needRetry) {
                needRetry = false;
            } else {
                if (!haveTicket) {
                    return null;
                } else {
                    return getCoachSeat(seatidx); 
                }
            }
        }
    }


    public boolean refundTicket(Ticket ticket) {
        int departure = ticket.departure;
        int arrival = ticket.arrival;
        int seatidx = (ticket.coach - 1) * this.seatnum + ticket.seat;
        boolean flag = false;
        boolean[] refundArray = new boolean[this.cacheSize];
        modifyLock.lock();
        try{

        refundArray = this.seats[seatidx].modify(departure, arrival, REFUND);
        if(refundArray != null)  {
            flushRouteCache(refundArray, REFUND);
            flag = true;
        }
        } finally {
            modifyLock.unlock();
        }
        return flag;
    }
    
    //将一维座位号转化为 车厢和座位
    private int[] getCoachSeat(int seatidx) {
        int coach = seatidx / this.seatnum;  
        int seat = seatidx % this.seatnum;
        if (seat != 0) {
            return new int[] {coach + 1, seat};
        } else {
            return new int[] {coach, this.seatnum};
        }
    }


    private void flushRouteCache(boolean[] updateArray, boolean RefundOrBuy) {
        int chg = RefundOrBuy ? 1 : -1;
        routeWrLock.lock();
        try {
            for (int i = 1; i < this.stationnum; i++) {
                for (int j = i + 1; j <= this.stationnum; j++) {
                    int idx_route = (i - 1) * this.stationnum + j - 1;
                    if (updateArray[idx_route]) {     
                        this.cacheRoute[idx_route] += chg;
                    }
                }
            }
        } finally {
            routeWrLock.unlock();
        }
    }

}
