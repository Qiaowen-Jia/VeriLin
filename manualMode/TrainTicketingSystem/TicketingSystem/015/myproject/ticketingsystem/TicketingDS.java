package ticketingsystem;

import java.lang.Math;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;


class TicketInfo{
    protected int seatId;
    protected int coachId;
    protected int routeId;
    protected int departure;
    protected int arrival;

    public TicketInfo(int seatId, int coachId, int routeId, int departure, int arrival){
        this.seatId = seatId;
        this.coachId = coachId;
        this.routeId = routeId;
        this.departure = departure;
        this.arrival = arrival;
    }

    public int getSeatId(){ return this.seatId; }

    public int getCoachId(){ return this.coachId; }

    public int getRouteId(){ return this.coachId; }

    public int getDeparture(){ return this.departure; }

    public int getArrival(){ return this.arrival; }
}

interface RouteBase{
    int getRemainingTicketNum(int departure, int arrival);

    TicketInfo buyTicket(int departure, int arrival);

    boolean refundTicket(int coach, int seat, int departure, int arrival);
}

class RouteNoLock implements RouteBase{
    protected final int maxCoachId;
    protected final int maxStationId;
    protected final int maxSeatId;
    protected final int totalTicketsNum;
    protected final int routeId;

    protected int [][] totalRemainTicketNum; // departure - arrival
    protected int [][][] coachRemainTicketNum;
    protected boolean [][][] seatOccupiedStation; // coach-seat-station
    protected boolean [][][][] seatRangeOccupied; // coach-seat-departure-arrival-boolean

    public RouteNoLock(int routeId, int maxCoachNum, int coachSeatNum, int maxStationNum) {
        this.routeId = routeId;
        this.maxCoachId = maxCoachNum;
        this.maxStationId = maxStationNum;
        this.maxSeatId = coachSeatNum;
        this.totalTicketsNum = coachSeatNum * maxCoachNum;

        totalRemainTicketNum = new int[maxStationNum - 1][]; // there is maxStationNum-1 departure station
        for (int departure = 1; departure < maxStationNum; ++departure) {
            int departureIdx = departure - 1;
            totalRemainTicketNum[departureIdx] = new int[maxStationNum - departure];
            for (int arrivalIdx = departure; arrivalIdx < maxStationNum; ++arrivalIdx) {
                totalRemainTicketNum[departureIdx][arrivalIdx - departure] = this.totalTicketsNum;
            }
        }

        coachRemainTicketNum = new int [maxCoachNum][maxStationNum - 1][];
        for (int coachIdx = 0; coachIdx < maxCoachNum; ++coachIdx) {
            for (int departure = 1; departure < maxStationNum; ++departure) {
                int departureIdx = departure - 1;
                coachRemainTicketNum[coachIdx][departureIdx] = new int[maxStationNum - departure];
                for (int arrivalIdx = departure; arrivalIdx < maxStationNum; ++arrivalIdx) {
                    coachRemainTicketNum[coachIdx][departureIdx][arrivalIdx - departure] = coachSeatNum;
                }
            }
        }

        seatOccupiedStation = new boolean [maxCoachNum][coachSeatNum][maxStationNum - 1];
        for (int coachIdx = 0; coachIdx < maxCoachNum; ++coachIdx) {
            for (int seatIdx = 0; seatIdx < coachSeatNum; ++seatIdx) {
                for (int station = 1; station < maxStationNum; ++station) {
                    seatOccupiedStation[coachIdx][seatIdx][station - 1] = false;
                }
            }
        }

        seatRangeOccupied = new boolean[maxCoachNum][coachSeatNum][maxStationNum - 1][];
        for (int coachIdx = 0; coachIdx < maxCoachNum; ++coachIdx) {
            for (int seatIdx = 0; seatIdx < coachSeatNum; ++seatIdx) {
                for (int departure = 1; departure < maxStationNum; ++departure) {
                    int departureIdx = departure - 1;
                    seatRangeOccupied[coachIdx][seatIdx][departureIdx] = new boolean[maxStationNum - departure];
                    for (int arrivalIdx = departure; arrivalIdx < maxStationNum; ++arrivalIdx) {
                        seatRangeOccupied[coachIdx][seatIdx][departureIdx][arrivalIdx - departure] = false;
                    }
                }
            }
        }
    }

    final boolean isSeatAvaiable(int coach, int seat, int departure, int arrival) {
        int coachIdx = coach - 1;
        int seatIdx = seat - 1;
        int arrivalIdx = arrival - 1;
        for(int stationIdx = departure - 1; stationIdx < arrivalIdx; ++stationIdx){
            if(seatOccupiedStation[coachIdx][seatIdx][stationIdx]){
                return false;
            }
        }
        return true;
    }

    final boolean isSeatOccupied(int coach, int seat, int departure, int arrival) {
        int coachIdx = coach - 1;
        int seatIdx = seat - 1;
        int arrivalIdx = arrival - 1;
        for(int stationIdx = departure - 1; stationIdx < arrivalIdx; ++stationIdx){
            if(!seatOccupiedStation[coachIdx][seatIdx][stationIdx]){
                return false;
            }
        }
        return true;
    }

    final int findLeftUnoccupied(int coach, int seat, int station){
        int coachIdx = coach - 1;
        int seatIdx = seat - 1;
        while (station > 0 && !seatOccupiedStation[coachIdx][seatIdx][station - 1]) {
            --station;
        }
        return station + 1;
    }

    final int findRightUnoccupied(int coach, int seat, int station){
        int coachIdx = coach - 1;
        int seatIdx = seat - 1;
        while (station < maxStationId && !seatOccupiedStation[coachIdx][seatIdx][station - 1]) {
            ++station;
        }
        return station - 1;
    }

    void takeSeat(int coach, int seat, int departure, int arrival){
        int coachIdx = coach - 1;
        int seatIdx = seat - 1;
        int arrivalIdx = arrival - 1;
        for(int stationIdx = departure - 1; stationIdx < arrivalIdx; ++stationIdx){
            seatOccupiedStation[coachIdx][seatIdx][stationIdx] = true;
        }

        int left = findLeftUnoccupied(coach, seat, departure - 1);
        int right = findRightUnoccupied(coach, seat, arrival) + 1;
        for(int start = left; start < arrival; ++ start){
            for(int endIdx = Math.max(start, departure); endIdx < right; ++endIdx){
                int startIdx = start - 1;
                if(!seatRangeOccupied[coachIdx][seatIdx][startIdx][endIdx - start]){
                    seatRangeOccupied[coachIdx][seatIdx][startIdx][endIdx - start] = true;

                    --coachRemainTicketNum[coachIdx][startIdx][endIdx - start];
                    --totalRemainTicketNum[startIdx][endIdx - start];
                }
            }
        }
    }

    void returnSeat(int coach, int seat, int departure, int arrival){
        int coachIdx = coach - 1;
        int seatIdx = seat - 1;
        int arrivalIdx = arrival - 1;
        for(int stationIdx = departure - 1; stationIdx < arrivalIdx; ++stationIdx){
            seatOccupiedStation[coachIdx][seatIdx][stationIdx] = false;
        }

        int left = findLeftUnoccupied(coach, seat, departure - 1);
        int right = findRightUnoccupied(coach, seat, arrival) + 1;
        for(int start = left; start < arrival; ++ start){
            for(int endIdx = Math.max(start, departure); endIdx < right; ++endIdx){
                int startIdx = start - 1;
                if(seatRangeOccupied[coachIdx][seatIdx][startIdx][endIdx - start]){
                    seatRangeOccupied[coachIdx][seatIdx][startIdx][endIdx - start] = false;

                    ++coachRemainTicketNum[coachIdx][startIdx][endIdx - start];
                    ++totalRemainTicketNum[startIdx][endIdx - start];
                }
            }
        }
    }

    public int getRemainingTicketNum(int departure, int arrival){
        return totalRemainTicketNum[departure - 1][arrival - departure - 1];
    }

    public TicketInfo buyTicket(int departure, int arrival){
        if(totalRemainTicketNum[departure - 1][arrival - departure - 1] == 0){
            return null;
        } else {
            for(int coach = 1; coach <= maxCoachId; ++coach){
                if(coachRemainTicketNum[coach-1][departure-1][arrival-departure-1] == 0) // coach has no ticket left, check next coach
                    continue;
                for(int seat = 1; seat <= maxSeatId; ++seat){
                    if(isSeatAvaiable(coach, seat, departure, arrival)){
                        takeSeat(coach, seat, departure, arrival);
                        return new TicketInfo(seat, coach, routeId, departure, arrival);
                    }
                }
            }
            System.out.println("Logical Error: there is supposed to have ticket left");
            System.exit(1);
            return null;
        }
    }

    public boolean refundTicket(int coach, int seat, int departure, int arrival){
        if(coachRemainTicketNum[coach-1][departure-1][arrival-departure-1] < maxSeatId /*&& isSeatOccupied(coach, seat, departure, arrival)*/){
            returnSeat(coach, seat, departure, arrival);
            return true;
        } else {
            return false;
        }
    }
}

class TTASLock{
    private AtomicBoolean busy;

    public TTASLock(){
        busy = new AtomicBoolean(false);
    }

    final public void lock(){
        do {
            while (busy.get()) { }
        } while (busy.getAndSet(true));
    }

    final public void unlock(){
        busy.set(false);
    }
}

class RouteReadWriteLock extends RouteNoLock{
    protected ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public RouteReadWriteLock(int routeId, int maxCoachNum, int coachSeatNum, int maxStationNum) {
        super(routeId, maxCoachNum, coachSeatNum, maxStationNum);
    }

    public int getRemainingTicketNum(int departure, int arrival){
        lock.readLock().lock();
        try {
            return totalRemainTicketNum[departure - 1][arrival - departure - 1];
        }finally {
            lock.readLock().unlock();
        }
    }

    public TicketInfo buyTicket(int departure, int arrival){
        lock.writeLock().lock();
        try {
            if (totalRemainTicketNum[departure - 1][arrival - departure - 1] == 0) {
                return null;
            } else {
                for (int coach = 1; coach <= maxCoachId; ++coach) {
                    if (coachRemainTicketNum[coach - 1][departure - 1][arrival - departure - 1] == 0) // coach has no ticket left, check next coach
                        continue;
                    for (int seat = 1; seat <= maxSeatId; ++seat) {
                        if (isSeatAvaiable(coach, seat, departure, arrival)) {
                            takeSeat(coach, seat, departure, arrival);
                            return new TicketInfo(seat, coach, routeId, departure, arrival);
                        }
                    }
                }
                System.out.println("Logical Error: there is supposed to have ticket left");
                System.exit(1);
                return null;
            }
        }finally {
            lock.writeLock().unlock();
        }
    }

    public boolean refundTicket(int coach, int seat, int departure, int arrival){
        lock.writeLock().lock();
        try {
            if (coachRemainTicketNum[coach - 1][departure - 1][arrival - departure - 1] < maxSeatId /*&& isSeatOccupied(coach, seat, departure, arrival)*/) {
                returnSeat(coach, seat, departure, arrival);
                return true;
            } else {
                return false;
            }
        }finally {
            lock.writeLock().unlock();
        }
    }
}

class RouteWithCoachLocked implements RouteBase{
    protected final int maxCoachId;
    protected final int maxStationId;
    protected final int maxSeatId;
    protected final int totalTicketsNum;
    protected final int routeId;

    protected int [][][] coachRemainTicketNum;
    protected boolean [][][] seatOccupiedStation; // coach-seat-station
    protected boolean [][][][] seatRangeOccupied; // coach-seat-departure-arrival-boolean

    private TTASLock [] coachLocks;

    public RouteWithCoachLocked(int routeId, int maxCoachNum, int coachSeatNum, int maxStationNum) {
        this.routeId = routeId;
        this.maxCoachId = maxCoachNum;
        this.maxStationId = maxStationNum;
        this.maxSeatId = coachSeatNum;
        this.totalTicketsNum = coachSeatNum * maxCoachNum;

        this.coachLocks = new TTASLock[maxCoachNum];
        for(int i = 0; i < maxCoachNum; ++i)
            coachLocks[i] = new TTASLock();

        coachRemainTicketNum = new int [maxCoachNum][maxStationNum - 1][];
        for (int coachIdx = 0; coachIdx < maxCoachNum; ++coachIdx) {
            for (int departure = 1; departure < maxStationNum; ++departure) {
                int departureIdx = departure - 1;
                coachRemainTicketNum[coachIdx][departureIdx] = new int[maxStationNum - departure];
                for (int arrivalIdx = departure; arrivalIdx < maxStationNum; ++arrivalIdx) {
                    coachRemainTicketNum[coachIdx][departureIdx][arrivalIdx - departure] = coachSeatNum;
                }
            }
        }

        seatOccupiedStation = new boolean [maxCoachNum][coachSeatNum][maxStationNum - 1];
        for (int coachIdx = 0; coachIdx < maxCoachNum; ++coachIdx) {
            for (int seatIdx = 0; seatIdx < coachSeatNum; ++seatIdx) {
                for (int station = 1; station < maxStationNum; ++station) {
                    seatOccupiedStation[coachIdx][seatIdx][station - 1] = false;
                }
            }
        }

        seatRangeOccupied = new boolean[maxCoachNum][coachSeatNum][maxStationNum - 1][];
        for (int coachIdx = 0; coachIdx < maxCoachNum; ++coachIdx) {
            for (int seatIdx = 0; seatIdx < coachSeatNum; ++seatIdx) {
                for (int departure = 1; departure < maxStationNum; ++departure) {
                    int departureIdx = departure - 1;
                    seatRangeOccupied[coachIdx][seatIdx][departureIdx] = new boolean[maxStationNum - departure];
                    for (int arrivalIdx = departure; arrivalIdx < maxStationNum; ++arrivalIdx) {
                        seatRangeOccupied[coachIdx][seatIdx][departureIdx][arrivalIdx - departure] = false;
                    }
                }
            }
        }
    }

    final boolean isSeatAvaiable(int coach, int seat, int departure, int arrival) {
        int coachIdx = coach - 1;
        int seatIdx = seat - 1;
        int arrivalIdx = arrival - 1;
        for(int stationIdx = departure - 1; stationIdx < arrivalIdx; ++stationIdx){
            if(seatOccupiedStation[coachIdx][seatIdx][stationIdx]){
                return false;
            }
        }
        return true;
    }

    final boolean isSeatOccupied(int coach, int seat, int departure, int arrival) {
        int coachIdx = coach - 1;
        int seatIdx = seat - 1;
        int arrivalIdx = arrival - 1;
        for(int stationIdx = departure - 1; stationIdx < arrivalIdx; ++stationIdx){
            if(!seatOccupiedStation[coachIdx][seatIdx][stationIdx]){
                return false;
            }
        }
        return true;
    }

    final int findLeftUnoccupied(int coach, int seat, int station){
        int coachIdx = coach - 1;
        int seatIdx = seat - 1;
        while (station > 0 && !seatOccupiedStation[coachIdx][seatIdx][station - 1]) {
            --station;
        }
        return station + 1;
    }

    final int findRightUnoccupied(int coach, int seat, int station){
        int coachIdx = coach - 1;
        int seatIdx = seat - 1;
        while (station < maxStationId && !seatOccupiedStation[coachIdx][seatIdx][station - 1]) {
            ++station;
        }
        return station - 1;
    }

    void takeSeat(int coach, int seat, int departure, int arrival){
        int coachIdx = coach - 1;
        int seatIdx = seat - 1;
        int arrivalIdx = arrival - 1;
        for(int stationIdx = departure - 1; stationIdx < arrivalIdx; ++stationIdx){
            seatOccupiedStation[coachIdx][seatIdx][stationIdx] = true;
        }

        int left = findLeftUnoccupied(coach, seat, departure - 1);
        int right = findRightUnoccupied(coach, seat, arrival) + 1;
        for(int start = left; start < arrival; ++ start){
            for(int endIdx = Math.max(start, departure); endIdx < right; ++endIdx){
                int startIdx = start - 1;
                if(!seatRangeOccupied[coachIdx][seatIdx][startIdx][endIdx - start]){
                    seatRangeOccupied[coachIdx][seatIdx][startIdx][endIdx - start] = true;

                    --coachRemainTicketNum[coachIdx][startIdx][endIdx - start];
                }
            }
        }
    }

    void returnSeat(int coach, int seat, int departure, int arrival){
        int coachIdx = coach - 1;
        int seatIdx = seat - 1;
        int arrivalIdx = arrival - 1;
        for(int stationIdx = departure - 1; stationIdx < arrivalIdx; ++stationIdx){
            seatOccupiedStation[coachIdx][seatIdx][stationIdx] = false;
        }

        int left = findLeftUnoccupied(coach, seat, departure - 1);
        int right = findRightUnoccupied(coach, seat, arrival) + 1;
        for(int start = left; start < arrival; ++ start){
            for(int endIdx = Math.max(start, departure); endIdx < right; ++endIdx){
                int startIdx = start - 1;
                if(seatRangeOccupied[coachIdx][seatIdx][startIdx][endIdx - start]){
                    seatRangeOccupied[coachIdx][seatIdx][startIdx][endIdx - start] = false;

                    ++coachRemainTicketNum[coachIdx][startIdx][endIdx - start];
                }
            }
        }
    }

    boolean isSame(int[] snapshot, int[] newSnap){
        int n = snapshot.length;
        for(int i = 0; i < n; ++i){
            if(snapshot[i] != newSnap[i])
                return false;
        }
        return true;
    }

    void lockAll(){
        for(int i = 0; i < maxCoachId; ++i)
            coachLocks[i].lock();
    }

    void releaseAll(){
        for(int i = 0; i < maxCoachId; ++i)
            coachLocks[i].unlock();
    }

    int countRemain(int departure, int arrival){
        int remain = 0;
        for(int i = 0; i < maxCoachId; ++i)
            remain += coachRemainTicketNum[i][departure - 1][arrival - departure - 1];
        return remain;
    }

    public int getRemainingTicketNum(int departure, int arrival){
        int[] snapshot = new int[maxCoachId];
        int sum = 0;
        for(int i = 0; i < maxCoachId; ++i) {
            snapshot[i] = coachRemainTicketNum[i][departure - 1][arrival - departure - 1];
            sum += snapshot[i];
        }

        while(true){
            int[] newSnap = new int[maxCoachId];
            int newSum = 0;
            for(int i = 0; i < maxCoachId; ++i) {
                newSnap[i] = coachRemainTicketNum[i][departure - 1][arrival - departure - 1];
                newSum += newSnap[i];
            }

            if(newSum == sum && isSame(snapshot, newSnap)){
                return newSum;
            }
            sum = newSum;
            snapshot = newSnap;
        }
    }

    public TicketInfo buyTicket(int departure, int arrival){
        lockAll();
        try {
            if (countRemain(departure, arrival) == 0) {
                return null;
            } else {
                for (int coach = 1; coach <= maxCoachId; ++coach) {
                    if (coachRemainTicketNum[coach - 1][departure - 1][arrival - departure - 1] == 0) // coach has no ticket left, check next coach
                        continue;
                    for (int seat = 1; seat <= maxSeatId; ++seat) {
                        if (isSeatAvaiable(coach, seat, departure, arrival)) {
                            takeSeat(coach, seat, departure, arrival);
                            return new TicketInfo(seat, coach, routeId, departure, arrival);
                        }
                    }
                }
                System.out.println("Logical Error: there is supposed to have ticket left");
                System.exit(1);
                return null;
            }
        } finally {
            releaseAll();
        }
    }

    public boolean refundTicket(int coach, int seat, int departure, int arrival){
        coachLocks[coach-1].lock();
        try {
            if (coachRemainTicketNum[coach - 1][departure - 1][arrival - departure - 1] < maxSeatId /*&& isSeatOccupied(coach, seat, departure, arrival)*/) {
                returnSeat(coach, seat, departure, arrival);
                return true;
            } else {
                return false;
            }
        }
        finally{
            coachLocks[coach-1].unlock();
        }
    }
}

class RouteOptimized implements RouteBase{
    private final int maxCoachId;
    private final int maxStationId;
    private final int maxSeatId;
    private final int totalTicketsNum;
    private final int routeId;


    private int [][] totalRemainTicketNum;
    private int [][][] coachRemainTicketNum;
    private boolean [][][] seatOccupiedStation; // coach-seat-station
    private boolean [][][][] seatRangeOccupied; // coach-seat-departure-arrival-boolean
    private AtomicBoolean busy;

    public RouteOptimized(int routeId, int maxCoachNum, int coachSeatNum, int maxStationNum) {
        this.routeId = routeId;
        this.maxCoachId = maxCoachNum;
        this.maxStationId = maxStationNum;
        this.maxSeatId = coachSeatNum;
        this.totalTicketsNum = coachSeatNum * maxCoachNum;
        this.busy = new AtomicBoolean(false);

        totalRemainTicketNum = new int[maxStationNum - 1][]; // there is maxStationNum-1 departure station
        for (int departure = 1; departure < maxStationNum; ++departure) {
            int departureIdx = departure - 1;
            totalRemainTicketNum[departureIdx] = new int[maxStationNum - departure];
            for (int arrivalIdx = departure; arrivalIdx < maxStationNum; ++arrivalIdx) {
                totalRemainTicketNum[departureIdx][arrivalIdx - departure] = this.totalTicketsNum;
            }
        }

        coachRemainTicketNum = new int [maxCoachNum][maxStationNum - 1][];
        for (int coachIdx = 0; coachIdx < maxCoachNum; ++coachIdx) {
            for (int departure = 1; departure < maxStationNum; ++departure) {
                int departureIdx = departure - 1;
                coachRemainTicketNum[coachIdx][departureIdx] = new int[maxStationNum - departure];
                for (int arrivalIdx = departure; arrivalIdx < maxStationNum; ++arrivalIdx) {
                    coachRemainTicketNum[coachIdx][departureIdx][arrivalIdx - departure] = coachSeatNum;
                }
            }
        }

        seatOccupiedStation = new boolean [maxCoachNum][coachSeatNum][maxStationNum - 1];
        for (int coachIdx = 0; coachIdx < maxCoachNum; ++coachIdx) {
            for (int seatIdx = 0; seatIdx < coachSeatNum; ++seatIdx) {
                for (int station = 1; station < maxStationNum; ++station) {
                    seatOccupiedStation[coachIdx][seatIdx][station - 1] = false;
                }
            }
        }

        seatRangeOccupied = new boolean[maxCoachNum][coachSeatNum][maxStationNum - 1][];
        for (int coachIdx = 0; coachIdx < maxCoachNum; ++coachIdx) {
            for (int seatIdx = 0; seatIdx < coachSeatNum; ++seatIdx) {
                for (int departure = 1; departure < maxStationNum; ++departure) {
                    int departureIdx = departure - 1;
                    seatRangeOccupied[coachIdx][seatIdx][departureIdx] = new boolean[maxStationNum - departure];
                    for (int arrivalIdx = departure; arrivalIdx < maxStationNum; ++arrivalIdx) {
                        seatRangeOccupied[coachIdx][seatIdx][departureIdx][arrivalIdx - departure] = false;
                    }
                }
            }
        }
    }

    final boolean isSeatAvaiable(int coach, int seat, int departure, int arrival) {
        int coachIdx = coach - 1;
        int seatIdx = seat - 1;
        int arrivalIdx = arrival - 1;
        for(int stationIdx = departure - 1; stationIdx < arrivalIdx; ++stationIdx){
            if(seatOccupiedStation[coachIdx][seatIdx][stationIdx]){
                return false;
            }
        }
        return true;
    }

    final boolean isSeatOccupied(int coach, int seat, int departure, int arrival) {
        int coachIdx = coach - 1;
        int seatIdx = seat - 1;
        int arrivalIdx = arrival - 1;
        for(int stationIdx = departure - 1; stationIdx < arrivalIdx; ++stationIdx){
            if(!seatOccupiedStation[coachIdx][seatIdx][stationIdx]){
                return false;
            }
        }
        return true;
    }

    final int findLeftUnoccupied(int coach, int seat, int station){
        int coachIdx = coach - 1;
        int seatIdx = seat - 1;
        while (station > 0 && !seatOccupiedStation[coachIdx][seatIdx][station - 1]) {
            --station;
        }
        return station + 1;
    }

    final int findRightUnoccupied(int coach, int seat, int station){
        int coachIdx = coach - 1;
        int seatIdx = seat - 1;
        while (station < maxStationId && !seatOccupiedStation[coachIdx][seatIdx][station - 1]) {
            ++station;
        }
        return station - 1;
    }

    void takeSeat(int coach, int seat, int departure, int arrival, int[][] copy){
        int coachIdx = coach - 1;
        int seatIdx = seat - 1;
        int arrivalIdx = arrival - 1;
        for(int stationIdx = departure - 1; stationIdx < arrivalIdx; ++stationIdx){
            seatOccupiedStation[coachIdx][seatIdx][stationIdx] = true;
        }

        int left = findLeftUnoccupied(coach, seat, departure - 1);
        int right = findRightUnoccupied(coach, seat, arrival) + 1;
        for(int start = left; start < arrival; ++ start){
            for(int endIdx = Math.max(start, departure); endIdx < right; ++endIdx){
                int startIdx = start - 1;
                if(!seatRangeOccupied[coachIdx][seatIdx][startIdx][endIdx - start]){
                    seatRangeOccupied[coachIdx][seatIdx][startIdx][endIdx - start] = true;

                    --coachRemainTicketNum[coachIdx][startIdx][endIdx - start];
                    --copy[startIdx][endIdx - start];
                }
            }
        }
    }

    void returnSeat(int coach, int seat, int departure, int arrival, int[][] copy){
        int coachIdx = coach - 1;
        int seatIdx = seat - 1;
        int arrivalIdx = arrival - 1;
        for(int stationIdx = departure - 1; stationIdx < arrivalIdx; ++stationIdx){
            seatOccupiedStation[coachIdx][seatIdx][stationIdx] = false;
        }

        int left = findLeftUnoccupied(coach, seat, departure - 1);
        int right = findRightUnoccupied(coach, seat, arrival) + 1;
        for(int start = left; start < arrival; ++ start){
            for(int endIdx = Math.max(start, departure); endIdx < right; ++endIdx){
                int startIdx = start - 1;
                if(seatRangeOccupied[coachIdx][seatIdx][startIdx][endIdx - start]){
                    seatRangeOccupied[coachIdx][seatIdx][startIdx][endIdx - start] = false;

                    ++coachRemainTicketNum[coachIdx][startIdx][endIdx - start];
                    ++copy[startIdx][endIdx - start];
                }
            }
        }
    }

    public int getRemainingTicketNum(int departure, int arrival){
        return totalRemainTicketNum[departure - 1][arrival - departure - 1];
    }

    int [][] copy(){
        int [][] newOne =  new int[maxStationId - 1][]; // there is maxStationNum-1 departure station
        for (int departure = 1; departure < maxStationId; ++departure) {
            int departureIdx = departure - 1;
            newOne[departureIdx] = new int[maxStationId - departure];
            System.arraycopy(totalRemainTicketNum[departureIdx], departure - departure, newOne[departureIdx], departure - departure, maxStationId - departure);
        }
        return newOne;
    }

    public TicketInfo buyTicket(int departure, int arrival){
        while(true){
            while (busy.get()) { }
            if(!busy.getAndSet(true))
                break;
        }

        try{
            if(totalRemainTicketNum[departure - 1][arrival - departure - 1] == 0){
                return null;
            } else {
                int[][] tmp = copy();
                for(int coach = 1; coach <= maxCoachId; ++coach){
                    if(coachRemainTicketNum[coach-1][departure-1][arrival-departure-1] == 0) // coach has no ticket left, check next coach
                        continue;
                    for(int seat = 1; seat <= maxSeatId; ++seat){
                        if(isSeatAvaiable(coach, seat, departure, arrival)){
                            takeSeat(coach, seat, departure, arrival, tmp);
                            totalRemainTicketNum = tmp;
                            return new TicketInfo(seat, coach, routeId, departure, arrival);
                        }
                    }
                }
                totalRemainTicketNum = tmp;
                System.out.println("Logical Error: there is supposed to have ticket left");
                System.exit(1);
                return null;
            }
        }
        finally {
            busy.set(false);
        }
    }

    public boolean refundTicket(int coach, int seat, int departure, int arrival){
        while(true){
            while (busy.get()) { }
            if(!busy.getAndSet(true))
                break;
        }

        try{
            if(coachRemainTicketNum[coach-1][departure-1][arrival-departure-1] < maxSeatId /*&& isSeatOccupied(coach, seat, departure, arrival)*/){
                int[][] tmp = copy();
                returnSeat(coach, seat, departure, arrival, tmp);
                totalRemainTicketNum = tmp;
                return true;
            } else {
                return false;
            }
        }
        finally {
            busy.set(false);
        }
    }
}

class RouteFactory{
    RouteBase build(int route, int coachnum, int seatnum, int stationnum, int threadnum){
        if(threadnum == 1)
            return new RouteNoLock(route, coachnum, seatnum, stationnum);
        return new RouteOptimized(route, coachnum, seatnum, stationnum);
    }
}

public class TicketingDS implements TicketingSystem {
    private AtomicLong nextTid;
    private RouteBase[] routes;
    private ConcurrentHashMap<Long,Ticket> currentTicket;

    TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum){
        routes = new RouteBase[routenum];
        RouteFactory factory = new RouteFactory();
        for(int route = 1; route <= routenum; ++route){
            routes[route - 1] = factory.build(route, coachnum, seatnum, stationnum, threadnum);
        }
        nextTid = new AtomicLong(1);
        currentTicket = new ConcurrentHashMap<Long,Ticket>();
    }

    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        TicketInfo tmp = routes[route - 1].buyTicket(departure, arrival);
        if(tmp == null)
            return null;
        else {
            Ticket result = new Ticket();
            result.tid = nextTid.getAndIncrement();
            result.passenger = passenger;
            result.route = route;
            result.coach = tmp.coachId;
            result.seat = tmp.seatId;
            result.departure = tmp.departure;
            result.arrival = tmp.arrival;
            currentTicket.put(result.tid, result);
            return result;
        }
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        return routes[route - 1].getRemainingTicketNum(departure, arrival);
    }

    @Override
    public boolean refundTicket(Ticket ticket) {
        Ticket sold = currentTicket.get(ticket.tid);
        if(sold == null)
            return false;

        if(!sold.passenger.equals(ticket.passenger) || sold.departure != ticket.departure || sold.arrival != ticket.arrival
                || sold.route != ticket.route || sold.coach != ticket.coach || sold.seat != ticket.seat){
            return false;
        }

        boolean success = routes[ticket.route - 1].refundTicket(ticket.coach, ticket.seat, ticket.departure, ticket.arrival);
        if(success){
            currentTicket.remove(ticket.tid);
        }
        return success;
    }
}

