package ticketingsystem;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class Ticket{
        long tid;
        String passenger;
        int route;
        int coach;
        int seat;
        int departure;
        int arrival;
}

class Seat {
    private final int ID;
    private Long info;
    private final int stationNum;

    public Seat(final int ID, final int stationNum) {
        this.ID = ID;
        this.info = new Long(0);
        this.stationNum = stationNum;
    }

    public int left(int departure) {
        int l = departure;
        while (l > 1) {
            if ((this.info & (1 << (l - 2))) != 0)
                return l;
            l--;
        }
        return l;
    }

    public int right(int arrival) {
        int r = arrival;
        while (r < this.stationNum) {
            if ((this.info & (1 << (r - 1))) != 0)
                return r;
            r++;
        }
        return r;
    }

    public int buyTicket(final int departure, final int arrival) {
        long cur = 0;
        for (int i = departure - 1; i < arrival - 1; i++)
            cur |= (1 << i);
        if ((this.info & cur) != 0)
            return -1;
        else
            this.info = (this.info | cur);
        return this.ID;
    }

    public boolean refundTicket(final int departure, final int arrival) {
        long cur = 0;
        for (int i = departure - 1; i < arrival - 1; i++)
            cur |= (1 << i);
        cur = ~cur;
        this.info = (this.info & cur);
        return true;
    }
}

class Coach {
    private final int ID;
    private final int seatNum;
    private Seat[] seats;

    public Coach(final int ID, final int seatNum, final int stationNum) {
        this.ID = ID;
        this.seatNum = seatNum;
        this.seats = new Seat[seatNum];
        for (int i = 0; i < seatNum; i++)
            this.seats[i] = new Seat(i + 1, stationNum);
    }

    public int left(int seatID, int departure) {
        return this.seats[seatID - 1].left(departure);
    }

    public int right(int seatID, int arrival) {
        return this.seats[seatID - 1].right(arrival);
    }

    public Ticket buyTicket(final int departure, final int arrival) {
        Ticket t = new Ticket();
        t.coach = this.ID;
        int id;
        for (int i = 0; i < this.seatNum; i++) {
            id = this.seats[i].buyTicket(departure, arrival);
            if (id != -1) {
                t.seat = id;
                return t;
            }
        }
        return null;
    }

    public boolean refundTicket(final int ID, final int departure, final int arrival) {
        return this.seats[ID - 1].refundTicket(departure, arrival);
    }
}

class Route {
    private final int ID;
    private final int coachNum;
    private Coach[] coaches;
    private Set<Long> sold;
    public ReentrantReadWriteLock lock;

    public Route(final int ID, final int coachNum, final int seatNum, final int stationNum) {
        this.ID = ID;
        this.coachNum = coachNum;
        this.coaches = new Coach[coachNum];
        this.sold = new HashSet<Long>();

        for (int i = 0; i < coachNum; i++)
            this.coaches[i] = new Coach(i + 1, seatNum, stationNum);
        lock = new ReentrantReadWriteLock();
    }

    public int left(int coachID, int seatID, int departure) {
        return this.coaches[coachID - 1].left(seatID, departure);
    }

    public int right(int coachID, int seatID, int arrival) {
        return this.coaches[coachID - 1].right(seatID, arrival);
    }

    public Ticket buyTicket(final long tid, final String passenger, final int departure, final int arrival) {
        for (int i = 0; i < this.coachNum; i++) {
            Ticket t = this.coaches[i].buyTicket(departure, arrival);
            if (t != null) {
                t.tid = tid;
                t.passenger = passenger;
                t.route = this.ID;
                t.departure = departure;
                t.arrival = arrival;

                this.sold.add(tid);
                return t;
            }
        }
        return null;
    }

    public boolean refundTicket(final Ticket t) {
        if (!this.sold.contains(t.tid))
            return false;
        else {
            this.sold.remove(t.tid);
            return this.coaches[t.coach - 1].refundTicket(t.seat, t.departure, t.arrival);
        }
    }
}

//public class TicketingDS implements TicketingSystem {
public class TicketingDS {
    private final int routeNum;
    private final int stationNum;
    private final int[][][] inq;
    private AtomicLong tid;
    private Route[] routes;

    public TicketingDS(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
        this.routeNum = routeNum;
        this.stationNum = stationNum;
        this.inq = new int[routeNum][stationNum][stationNum];
        int num = coachNum * seatNum;
        for (int i = 0; i < routeNum; i++)
            for (int j = 0; j < stationNum; j++)
                for (int k = 0; k < stationNum; k++)
                    this.inq[i][j][k] = num;
        this.routes = new Route[routeNum];
        for (int i = 0; i < routeNum; i++)
            routes[i] = new Route(i + 1, coachNum, seatNum, stationNum);
        this.tid = new AtomicLong(0);
    }

    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        if (route <= 0 || route > this.routeNum || arrival <= 0 || arrival > this.stationNum || departure <= 0 || departure > this.stationNum || arrival <= departure)
            return null;
        int r = route - 1;
        Ticket t = null;

        this.routes[r].lock.writeLock().lock();
        try {
            t = this.routes[r].buyTicket(this.tid.getAndIncrement(), passenger, departure, arrival);
            if (t != null) {
                int left = this.routes[r].left(t.coach, t.seat, departure);
                int right = this.routes[r].right(t.coach, t.seat, arrival);
                for (int i = left - 1; i < arrival - 1; i++)
                    for (int j = Math.max(i, departure); j < right; j++)
                        this.inq[r][i][j]--;
            }
        } finally {
            this.routes[r].lock.writeLock().unlock();
            return t;
        }
    }

    public int inquiry(int route, int departure, int arrival) {
        if (route <= 0 || route > this.routeNum || arrival <= 0 || arrival > this.stationNum || departure <= 0 || departure > this.stationNum || arrival <= departure)
            return 0;
        int result = 0;
        //this.routes[route - 1].lock.readLock().lock();
        try {
            result = this.inq[route - 1][departure - 1][arrival - 1];
        } finally {
            //this.routes[route - 1].lock.readLock().unlock();
            return result;
        }
    }

    public boolean refundTicket(Ticket t) {
        if (t == null) return false;
        if (t.route <= 0 || t.route > this.routeNum) return false;
        int r = t.route - 1;
        boolean result = false;

        this.routes[r].lock.writeLock().lock();
        try {
            result = this.routes[r].refundTicket(t);
            if (result) {
                int departure = t.departure;
                int arrival = t.arrival;
                int left = this.routes[r].left(t.coach, t.seat, departure);
                int right = this.routes[r].right(t.coach, t.seat, arrival);
                for (int i = left - 1; i < arrival - 1; i++)
                    for (int j = Math.max(i, departure); j < right; j++)
                        this.inq[r][i][j]++;
            }
        } finally {
            this.routes[r].lock.writeLock().unlock();
            return result;
        }
    }
}
