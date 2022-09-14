package ticketingsystem;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

class TTASLock {
    protected AtomicBoolean m_locked;
    public TTASLock() {
        m_locked = new AtomicBoolean(false);
    }
    public void lock() {
        while (true) {
            while (m_locked.get()) {
            }
            if (!m_locked.getAndSet(true)) {
                return;
            }
        }
    }
    public void unlock() {
        m_locked.set(false);
    }
}

public class TicketingDS implements TicketingSystem {
    private static class Route{
        HashMap<Long, Ticket> ticketSys;
        TTASLock lock;
        int[][] inquirySys;
        int[][] seatSys;
        long ttid;
    }
    int routenum;
    int coachnum;
    int seatnum;
    int stationnum;
    int threadnum;
    Route[] routes;
    TicketingDS(int routenum , int coachnum , int seatnum , int stationnum , int threadnum) {
        this.routenum = routenum;
        this.coachnum = coachnum;
        this.seatnum = seatnum;
        this.stationnum = stationnum;
        this.threadnum = threadnum;
        this.routes = new Route[this.routenum + 1];
        int initSeat = 0;
        for(int j = 1; j < this.stationnum; j++) {
            initSeat |= 1 << j;
        }
        long stride = (Long.MAX_VALUE - 1) / this.routenum;
        for(int i = 1; i <= this.routenum; i++) {
            Route route = new Route();
            route.lock = new TTASLock();
            route.ticketSys = new HashMap<>();
            route.inquirySys = new int[this.stationnum + 1][this.stationnum + 1];
            route.seatSys = new int[this.coachnum + 1][this.seatnum + 1];
            for (int j = 1; j <= this.stationnum; j++) {
                for (int k = 1; k <= this.stationnum; k++) {
                    route.inquirySys[j][k] = this.coachnum * this.seatnum;
                }
            }
            for (int j = 1; j <= this.coachnum; j++) {
                for (int k = 1; k <= this.seatnum; k++) {
                    route.seatSys[j][k] = initSeat;
                }
            }
            route.ttid = (i-1) * stride;
            this.routes[i] = route;
        }
    }

    public int inquiry(int route, int departure, int arrival) {
        return this.routes[route].inquirySys[departure][arrival];
    }

    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        if (passenger == "" || departure < 1 || arrival > this.stationnum || departure >= arrival || route < 1 || route > this.routenum) {
            return null;
        }
        int mask = 0;
        for(int i = departure; i < arrival; i++) {
            mask |= 1 << i;
        }
        Route rt = this.routes[route];
        rt.lock.lock();
        try {
            for(int coach = 1; coach <= this.coachnum; ++coach) {
                int found = 0;
                for(int seat = 1; seat <= this.seatnum; ++seat) {
                    int seatStatus = rt.seatSys[coach][seat];
                    if ((seatStatus & mask) == mask) {
                        found = seat;
                        break;
                    }
                }
                if (found > 0) {
                    Ticket ticket = new Ticket();
                    ticket.passenger = passenger;
                    ticket.route = route;
                    ticket.departure = departure;
                    ticket.arrival = arrival;
                    ticket.tid = rt.ttid++;
                    ticket.coach = coach;
                    ticket.seat = found;
                    int a = departure, b = arrival;
                    int seatStatus = rt.seatSys[coach][ticket.seat];
                    for(int i = departure; i < arrival; i++) {
                        seatStatus ^= 1 << i;
                    }
                    for(int i = departure-1; i > 0; i--) {
                        int check = seatStatus & (1 << i);
                        if (check == 0) {
                            break;
                        }
                        a = i;
                    }
                    for(int i = arrival; i < this.stationnum; i++) {
                        int check = seatStatus & (1 << i);
                        if (check == 0) {
                            break;

                        }
                        b = i + 1;
                    }
                    rt.seatSys[coach][ticket.seat] = seatStatus;
                    for(int i = a; i <= b; i++) {
                        for (int j = i + 1; j <= b; j++) {
                            if (j <= departure || i >= arrival) {
                                continue;
                            }
                            rt.inquirySys[i][j]--;
                        }
                    }
                    rt.ticketSys.put(ticket.tid, ticket);
                    return rt.ticketSys.get(ticket.tid);
                }
            }
        } finally {
            rt.lock.unlock();
        }
        return null;
    }

    public boolean refundTicket(Ticket ticket) {
        if (ticket.route < 1 || ticket.route > this.routenum) {
            return false;
        }
        Route rt = this.routes[ticket.route];
        rt.lock.lock();
        try {
            Ticket chk = rt.ticketSys.get(ticket.tid);
            if (chk != null && chk.passenger.equals(ticket.passenger) && chk.coach == ticket.coach && chk.departure == ticket.departure && chk.arrival == ticket.arrival && chk.seat == ticket.seat) {
                rt.ticketSys.remove(ticket.tid);
                int departure = ticket.departure;
                int arrival = ticket.arrival;
                int a = departure, b = arrival;
                int seatStatus = rt.seatSys[ticket.coach][ticket.seat];
                for(int i = departure; i < arrival; i++) {
                    seatStatus |= 1 << i;
                }
                for(int i = departure-1; i > 0; i--) {
                    int check = seatStatus & (1 << i);
                    if (check == 0) {
                        break;
                    }
                    a = i;
                }
                for(int i = arrival; i < this.stationnum; i++) {
                    int check = seatStatus & (1 << i);
                    if (check == 0) {
                        break;

                    }
                    b = i + 1;
                }
                rt.seatSys[ticket.coach][ticket.seat] = seatStatus;
                for(int i = a; i < b; i++) {
                    for (int j = i+1; j <= b; j++) {
                        if ((j <= ticket.departure) || (i >= ticket.arrival)) {
                            continue;
                        }
                        rt.inquirySys[i][j]++;
                    }
                }
                return true;
            }
        } finally {
            rt.lock.unlock();
        }
        return false;
    }
}
