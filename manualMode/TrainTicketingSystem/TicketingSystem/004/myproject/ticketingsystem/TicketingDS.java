package ticketingsystem;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicStampedReference;
import java.util.concurrent.locks.ReentrantLock;

public class TicketingDS implements TicketingSystem {

    public class Train {
        private final int routenum;
        private final AtomicInteger[] seats;
        private final ReentrantLock seatLock;
        private final int seatnum;
        private final int stationnum;
        private final int trainSeatnum;
        private final ConcurrentHashMap<Long, Ticket> soldTickets;
        private final AtomicStampedReference<int[][]> tableIndex;
        private final int[][] first;
        private final int[][] second;

        public Train(int routenum, final int coachnum, final int seatnum, final int stationnum) {
            seatLock = new ReentrantLock();
            this.routenum = routenum;
            this.seatnum = seatnum;
            this.stationnum = stationnum;
            this.trainSeatnum = coachnum * seatnum;
            this.seats = new AtomicInteger[this.trainSeatnum];
            for (int i = 0; i < this.trainSeatnum; ++i) {
                this.seats[i] = new AtomicInteger(0);
            }

            soldTickets = new ConcurrentHashMap<>(2000);
            first = new int[stationnum][stationnum];
            for (int j = 0; j < stationnum; j++) {
                for (int k = 1; k < stationnum; k++) {
                    first[j][k] = this.trainSeatnum;
                }
            }
            this.tableIndex = new AtomicStampedReference<>(first, 0);
            second = new int[stationnum][stationnum];
        }

        public final int inquiry(final int departure, final int arrival) {
            int currTimestap;
            int remain;
            do {
                currTimestap = tableIndex.getStamp();
                remain = tableIndex.getReference()[departure][arrival];
            } while (currTimestap != tableIndex.getStamp());
            return remain;
        }

        public Ticket buyTicket(String passenger, int departure, int arrival) {

            departure -= 1;
            arrival -= 1;

            if (inquiry(departure, arrival) == 0){
                return null;
            }

            seatLock.lock();

            if (tableIndex.getReference()[departure][arrival] > 0) {
                for (int i = 0; i < trainSeatnum; ++i) {
                    int tmp = seats[i].get();
                    if ((tmp & ((1 << arrival) - (1 << departure))) == 0) {
                        seats[i].set(tmp | ((1 << arrival) - (1 << departure)));
                        adjustRemainTickets(departure, arrival, tmp, -1);

                        Ticket t = new Ticket();
                        t.tid = shareTid.getAndIncrement();
                        t.passenger = passenger;
                        t.route = routenum;
                        t.departure = departure + 1;
                        t.arrival = arrival + 1;
                        t.coach = (i / seatnum) + 1;
                        t.seat = (i % seatnum) + 1;

                        soldTickets.put(t.tid, t);
                        seatLock.unlock();
                        return t;
                    }
                }
            }
            seatLock.unlock();
            return null;
        }

        public boolean refundTicket(Ticket ticket) {
            if (ticket == null) {
                return false;
            }
            Ticket t = soldTickets.get(ticket.tid);
            if (t == null) {
                return false;
            }
            if ((!t.passenger.equals(ticket.passenger)) || (t.coach != ticket.coach) || (t.seat != ticket.seat) || (t.departure != ticket.departure) || (t.arrival != ticket.arrival)) {
                return false;
            }
            if (!soldTickets.remove(ticket.tid, t)) {
                return false;//refund by others
            }
            int seat = (ticket.coach - 1) * seatnum + (ticket.seat - 1);
            int departure = ticket.departure - 1;
            int arrival = ticket.arrival - 1;
            seatLock.lock();
            int tmp = seats[seat].get();
            int cleanTmp = tmp & (~((1 << arrival) - (1 << departure)));
            seats[seat].set(cleanTmp);
            adjustRemainTickets(departure, arrival, cleanTmp, 1);
            seatLock.unlock();
            return true;
        }

        public void adjustRemainTickets(final int departure, final int arrival, final int rawSeat, int bias) {
            int[][] newTable = first;
            int[][] oldTable = tableIndex.getReference();
            if (oldTable == first) {
                newTable = second;
            }
            int stamp = tableIndex.getStamp();
            for (int i = 0; i < arrival; ++i) {
                for (int j = i + 1; j < stationnum; ++j) {
                    newTable[i][j] = oldTable[i][j];
                }
                for (int j = Math.max(i, departure) + 1; j < stationnum; ++j) {
                    if ((rawSeat == 0) || (((1 << j) - (1 << i) & rawSeat) == 0)) {
                        newTable[i][j] = oldTable[i][j] + bias;
                    }
                }
            }
            for (int i = arrival; i < stationnum; ++i) {
                for (int j = i + 1; j < stationnum; ++j) {
                    newTable[i][j] = oldTable[i][j];
                }
            }
            tableIndex.set(newTable, stamp + 1);
        }
    }

    private final Train[] trains;
    private final AtomicInteger shareTid;
    final int routenum, stationnum;

    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
        if (stationnum > 30) {
            System.out.print("station num too large");
            System.exit(-1);
        }
        this.routenum = routenum;
        this.stationnum = stationnum;
        this.shareTid = new AtomicInteger(0);
        this.trains = new Train[routenum];
        for (int i = 0; i < routenum; i++) {
            this.trains[i] = new Train(i + 1, coachnum, seatnum, stationnum);
        }
    }

    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        if (departure < 1 || arrival < 1 || departure > stationnum || arrival > stationnum || departure >= arrival) {
            return null;
        }
        return this.trains[route - 1].buyTicket(passenger, departure, arrival);
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        if (departure < 1 || arrival < 1 || departure > stationnum || arrival > stationnum || departure >= arrival) {
            return 0;
        }
        return this.trains[route - 1].inquiry(departure - 1, arrival - 1);
    }

    @Override
    public boolean refundTicket(Ticket ticket) {
        return this.trains[ticket.route - 1].refundTicket(ticket);
    }
}

//package ticketingsystem;
//
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.concurrent.atomic.AtomicStampedReference;
//
//public class TicketingDS implements TicketingSystem {
//
//    public class Train {
//        private final int routenum;
//        private final AtomicInteger[] seats;
//        private final AtomicInteger seatLock;
//        private final int seatnum;
//        private final int stationnum;
//        private final int trainSeatnum;
//        private final ConcurrentHashMap<Long, Ticket> soldTickets;
//        private final AtomicStampedReference<int[][]> tableIndex;
//        private final int[][] first;
//        private final int[][] second;
//
//        public Train(int routenum, final int coachnum, final int seatnum, final int stationnum) {
//            seatLock = new AtomicInteger(0);
//            this.routenum = routenum;
//            this.seatnum = seatnum;
//            this.stationnum = stationnum;
//            this.trainSeatnum = coachnum * seatnum;
//            this.seats = new AtomicInteger[this.trainSeatnum];
//            for (int i = 0; i < this.trainSeatnum; ++i) {
//                this.seats[i] = new AtomicInteger(0);
//            }
//
//            soldTickets = new ConcurrentHashMap<>(2000);
//            first = new int[stationnum][stationnum];
//            for (int j = 0; j < stationnum; j++) {
//                for (int k = 1; k < stationnum; k++) {
//                    first[j][k] = this.trainSeatnum;
//                }
//            }
//            this.tableIndex = new AtomicStampedReference<>(first, 0);
//            second = new int[stationnum][stationnum];
//        }
//
//        public final int inquiry(final int departure, final int arrival) {
//            int currTimestap;
//            int remain;
//            do {
//                currTimestap = tableIndex.getStamp();
//                remain = tableIndex.getReference()[departure][arrival];
//            } while (currTimestap != tableIndex.getStamp());
//            return remain;
//        }
//
//        public Ticket buyTicket(String passenger, int departure, int arrival) {
//
//            departure -= 1;
//            arrival -= 1;
//
////            while (!seatLock.compareAndSet(0, 1)) ;
//
//            while (true) {
//                if (seatLock.get() == 0) {
//                    if (seatLock.compareAndSet(0, 1)) {
//                        break;
//                    }
//                }
//            }
//
//            if (inquiry(departure, arrival) > 0) {
//                for (int i = 0; i < trainSeatnum; ++i) {
//                    int tmp = seats[i].get();
//                    if ((tmp & ((1 << arrival) - (1 << departure))) == 0) {
//                        seats[i].set(tmp | ((1 << arrival) - (1 << departure)));
//                        adjustRemainTickets(departure, arrival, tmp, -1);
//
//                        Ticket t = new Ticket();
//                        t.tid = shareTid.getAndIncrement();
//                        t.passenger = passenger;
//                        t.route = routenum;
//                        t.departure = departure + 1;
//                        t.arrival = arrival + 1;
//                        t.coach = (i / seatnum) + 1;
//                        t.seat = (i % seatnum) + 1;
//
//                        soldTickets.put(t.tid, t);
//                        seatLock.set(0);
//                        return t;
//                    }
//                }
//            }
//            seatLock.set(0);
//            return null;
//        }
//
//        public boolean refundTicket(Ticket ticket) {
//            if (ticket == null) {
//                return false;
//            }
//            Ticket t = soldTickets.get(ticket.tid);
//            if (t == null) {
//                return false;
//            }
//            if ((!t.passenger.equals(ticket.passenger)) || (t.coach != ticket.coach) || (t.seat != ticket.seat) || (t.departure != ticket.departure) || (t.arrival != ticket.arrival)) {
//                return false;
//            }
//            if (!soldTickets.remove(ticket.tid, t)) {
//                return false;//refund by others
//            }
//            int seat = (ticket.coach - 1) * seatnum + (ticket.seat - 1);
//            int departure = ticket.departure - 1;
//            int arrival = ticket.arrival - 1;
////            while (!seatLock.compareAndSet(0, 1)) ;
//            while (true) {
//                if (seatLock.get() == 0) {
//                    if (seatLock.compareAndSet(0, 1)) {
//                        break;
//                    }
//                }
//            }
//            int tmp = seats[seat].get();
//            int cleanTmp = tmp & (~((1 << arrival) - (1 << departure)));
//            seats[seat].set(cleanTmp);
//            adjustRemainTickets(departure, arrival, cleanTmp, 1);
//            seatLock.set(0);
//            return true;
//        }
//
//        public void adjustRemainTickets(final int departure, final int arrival, final int rawSeat, int bias) {
//            int[][] newTable = first;
//            int[][] oldTable = tableIndex.getReference();
//            if (oldTable == first) {
//                newTable = second;
//            }
//            int stamp = tableIndex.getStamp();
//            for (int i = 0; i < arrival; ++i) {
//                for (int j = i + 1; j < stationnum; ++j) {
//                    newTable[i][j] = oldTable[i][j];
//                }
//                for (int j = Math.max(i, departure) + 1; j < stationnum; ++j) {
//                    if ((rawSeat == 0) || (((1 << j) - (1 << i) & rawSeat) == 0)) {
//                        newTable[i][j] = oldTable[i][j] + bias;
//                    }
//                }
//            }
//            for (int i = arrival; i < stationnum; ++i) {
//                for (int j = i + 1; j < stationnum; ++j) {
//                    newTable[i][j] = oldTable[i][j];
//                }
//            }
//            tableIndex.set(newTable, stamp + 1);
//        }
//    }
//
//    private final Train[] trains;
//    private final AtomicInteger shareTid;
//    final int routenum, stationnum;
//
//    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
//        if (stationnum > 30) {
//            System.out.print("station num too large");
//            System.exit(-1);
//        }
//        this.routenum = routenum;
//        this.stationnum = stationnum;
//        this.shareTid = new AtomicInteger(0);
//        this.trains = new Train[routenum];
//        for (int i = 0; i < routenum; i++) {
//            this.trains[i] = new Train(i + 1, coachnum, seatnum, stationnum);
//        }
//    }
//
//    @Override
//    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
//        if (departure < 1 || arrival < 1 || departure > stationnum || arrival > stationnum || departure >= arrival) {
//            return null;
//        }
//        return this.trains[route - 1].buyTicket(passenger, departure, arrival);
//    }
//
//    @Override
//    public int inquiry(int route, int departure, int arrival) {
//        if (departure < 1 || arrival < 1 || departure > stationnum || arrival > stationnum || departure >= arrival) {
//            return 0;
//        }
//        return this.trains[route - 1].inquiry(departure - 1, arrival - 1);
//    }
//
//    @Override
//    public boolean refundTicket(Ticket ticket) {
//        return this.trains[ticket.route - 1].refundTicket(ticket);
//    }
//}


//package ticketingsystem;
//
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.concurrent.atomic.AtomicStampedReference;
//
//public class TicketingDS implements TicketingSystem {
//
//    public class Train {
//        private final int routenum;
//        private final AtomicInteger[] seats;
//        private final AtomicInteger seatLock;
//        private final int seatnum;
//        private final int stationnum;
//        private final int trainSeatnum;
//        private final ConcurrentHashMap<Long, Ticket> soldTickets;
//        private final AtomicStampedReference<int[][]> tableIndex;
//        ThreadLocal<int[][]> first = new ThreadLocal<int[][]>() {
//            @Override
//            protected int[][] initialValue() {
//                return new int[stationnum][stationnum];
//            }
//        };
//        ThreadLocal<int[][]> second = new ThreadLocal<int[][]>() {
//            @Override
//            protected int[][] initialValue() {
//                return new int[stationnum][stationnum];
//            }
//        };
//
//        public Train(int routenum, final int coachnum, final int seatnum, final int stationnum) {
//            seatLock = new AtomicInteger(0);
//            this.routenum = routenum;
//            this.seatnum = seatnum;
//            this.stationnum = stationnum;
//            this.trainSeatnum = coachnum * seatnum;
//            this.seats = new AtomicInteger[this.trainSeatnum];
//            for (int i = 0; i < this.trainSeatnum; ++i) {
//                this.seats[i] = new AtomicInteger(0);
//            }
//
//            soldTickets = new ConcurrentHashMap<>(2000);
//            this.tableIndex = new AtomicStampedReference<>(new int[stationnum][stationnum], 0);
//            int[][] temp = this.tableIndex.getReference();
//            for (int j = 0; j < stationnum; j++) {
//                for (int k = 1; k < stationnum; k++) {
//                    temp[j][k] = this.trainSeatnum;
//                }
//            }
//
//        }
//
//        public final int inquiry(final int departure, final int arrival) {
//            int currTimestap;
//            int remain;
//            do {
//                currTimestap = tableIndex.getStamp();
//                remain = tableIndex.getReference()[departure][arrival];
//            } while (currTimestap != tableIndex.getStamp());
//            return remain;
//        }
//
//        public Ticket buyTicket(String passenger, int departure, int arrival) {
//
//            departure -= 1;
//            arrival -= 1;
//
//            while (seatLock.compareAndSet(0, 1));
//
//            while (inquiry(departure, arrival) > 0) {
//                for (int i = 0; i < trainSeatnum; ++i) {
//                    int tmp = seats[i].get();
//                    while ((tmp & ((1 << arrival) - (1 << departure))) == 0) {
//                        if (seats[i].compareAndSet(tmp, tmp | ((1 << arrival) - (1 << departure)))) {
//                            adjustRemainTickets(departure, arrival, tmp, -1);
//
//                            Ticket t = new Ticket();
//                            t.tid = shareTid.getAndIncrement();
//                            t.passenger = passenger;
//                            t.route = routenum;
//                            t.departure = departure + 1;
//                            t.arrival = arrival + 1;
//                            t.coach = (i / seatnum) + 1;
//                            t.seat = (i % seatnum) + 1;
//
//                            soldTickets.put(t.tid, t);
//                            seatLock.set(0);
//                            return t;
//                        }
//                        tmp = seats[i].get();
//                    }
//                }
//            }
//            seatLock.set(0);
//            return null;
//        }
//
//        public boolean refundTicket(Ticket ticket) {
//            if (ticket == null) {
//                return false;
//            }
//            Ticket t = soldTickets.get(ticket.tid);
//            if (t == null) {
//                return false;
//            }
//            if ((!t.passenger.equals(ticket.passenger)) || (t.coach != ticket.coach) || (t.seat != ticket.seat) || (t.departure != ticket.departure) || (t.arrival != ticket.arrival)) {
//                return false;
//            }
//            if (!soldTickets.remove(ticket.tid, t)) {
//                return false;//refund by others
//            }
//            int seat = (ticket.coach - 1) * seatnum + (ticket.seat - 1);
//            int departure = ticket.departure - 1;
//            int arrival = ticket.arrival - 1;
//            while (seatLock.compareAndSet(0, 1));
//            while (true) {
//                int tmp = seats[seat].get();
//                int cleanTmp = tmp & (~((1 << arrival) - (1 << departure)));
//                if (seats[seat].compareAndSet(tmp, cleanTmp)) {
//                    adjustRemainTickets(departure, arrival, cleanTmp, 1);
//                    seatLock.set(0);
//                    return true;
//                }
//            }
//        }
//
//        public void adjustRemainTickets(final int departure, final int arrival, final int rawSeat, int bias) {
//            while (true) {
//                int[][] newTable = first.get();
//                int[][] oldTable = tableIndex.getReference();
//                if (oldTable == first.get()) {
//                    newTable = second.get();
//                }
//                int stamp = tableIndex.getStamp();
//                for (int i = 0; i < arrival; ++i) {
//                    for (int j = i + 1; j < stationnum; ++j) {
//                        newTable[i][j] = oldTable[i][j];
//                    }
//                    for (int j = Math.max(i, departure) + 1; j < stationnum; ++j) {
//                        if ((rawSeat == 0) || (((1 << j) - (1 << i) & rawSeat) == 0)) {
//                            newTable[i][j] = oldTable[i][j] + bias;
//                        }
//                    }
//                }
//                for (int i = arrival; i < stationnum; ++i) {
//                    for (int j = i + 1; j < stationnum; ++j) {
//                        newTable[i][j] = oldTable[i][j];
//                    }
//                }
//                if (tableIndex.compareAndSet(oldTable, newTable, stamp, stamp + 1)) {
//                    return;
//                }
//            }
//        }
//    }
//
//    private final Train[] trains;
//    private final AtomicInteger shareTid;
//    final int routenum, stationnum;
//
//    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
//        if (stationnum > 30) {
//            System.out.print("station num too large");
//            System.exit(-1);
//        }
//        this.routenum = routenum;
//        this.stationnum = stationnum;
//        this.shareTid = new AtomicInteger(0);
//        this.trains = new Train[routenum];
//        for (int i = 0; i < routenum; i++) {
//            this.trains[i] = new Train(i + 1, coachnum, seatnum, stationnum);
//        }
//    }
//
//    @Override
//    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
//        if (departure < 1 || arrival < 1 || departure > stationnum || arrival > stationnum || departure >= arrival) {
//            return null;
//        }
//        return this.trains[route - 1].buyTicket(passenger, departure, arrival);
//    }
//
//    @Override
//    public int inquiry(int route, int departure, int arrival) {
//        if (departure < 1 || arrival < 1 || departure > stationnum || arrival > stationnum || departure >= arrival) {
//            return 0;
//        }
//        return this.trains[route - 1].inquiry(departure - 1, arrival - 1);
//    }
//
//    @Override
//    public boolean refundTicket(Ticket ticket) {
//        return this.trains[ticket.route - 1].refundTicket(ticket);
//    }
//}


//package ticketingsystem;
//
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.concurrent.atomic.AtomicStampedReference;
//
//public class TicketingDS implements TicketingSystem {
//
//    public class Train {
//        private final int routenum;
//        private final AtomicInteger[] seats;
//        private final int seatnum;
//        private final int stationnum;
//        private final int trainSeatnum;
//        private final ConcurrentHashMap<Long, Ticket> soldTickets;
//        private final AtomicStampedReference<int[][]> tableIndex;
//        ThreadLocal<int[][]> first = new ThreadLocal<int[][]>() {
//            @Override
//            protected int[][] initialValue() {
//                return new int[stationnum][stationnum];
//            }
//        };
//        ThreadLocal<int[][]> second = new ThreadLocal<int[][]>() {
//            @Override
//            protected int[][] initialValue() {
//                return new int[stationnum][stationnum];
//            }
//        };
//
//        public Train(int routenum, final int coachnum, final int seatnum, final int stationnum) {
//            this.routenum = routenum;
//            this.seatnum = seatnum;
//            this.stationnum = stationnum;
//            this.trainSeatnum = coachnum * seatnum;
//            this.seats = new AtomicInteger[this.trainSeatnum];
//            for (int i = 0; i < this.trainSeatnum; ++i) {
//                this.seats[i] = new AtomicInteger(0);
//            }
//
//            soldTickets = new ConcurrentHashMap<>(2000);
//            this.tableIndex = new AtomicStampedReference<>(new int[stationnum][stationnum], 0);
//            int[][] temp = this.tableIndex.getReference();
//            for (int j = 0; j < stationnum; j++) {
//                for (int k = 1; k < stationnum; k++) {
//                    temp[j][k] = this.trainSeatnum;
//                }
//            }
//
//        }
//
//        public final int inquiry(final int departure, final int arrival) {
//            int currTimestap;
//            int remain;
//            do {
//                currTimestap = tableIndex.getStamp();
//                remain = tableIndex.getReference()[departure][arrival];
//            } while (currTimestap != tableIndex.getStamp());
//            return remain;
//        }
//
//        public Ticket buyTicket(String passenger, int departure, int arrival) {
//
//            departure -= 1;
//            arrival -= 1;
//
//            while (inquiry(departure, arrival) > 0) {
//                for (int i = 0; i < trainSeatnum; ++i) {
//                    int tmp = seats[i].get();
//                    while ((tmp & ((1 << arrival) - (1 << departure))) == 0) {
//                        if (seats[i].compareAndSet(tmp, tmp | ((1 << arrival) - (1 << departure)))) {
//                            adjustRemainTickets(departure, arrival, tmp, -1);
//
//                            Ticket t = new Ticket();
//                            t.tid = shareTid.getAndIncrement();
//                            t.passenger = passenger;
//                            t.route = routenum;
//                            t.departure = departure + 1;
//                            t.arrival = arrival + 1;
//                            t.coach = (i / seatnum) + 1;
//                            t.seat = (i % seatnum) + 1;
//
//                            soldTickets.put(t.tid, t);
//                            return t;
//                        }
//                        tmp = seats[i].get();
//                    }
//                }
//            }
//
//            return null;
//        }
//
//        public boolean refundTicket(Ticket ticket) {
//            if (ticket == null) {
//                return false;
//            }
//            Ticket t = soldTickets.get(ticket.tid);
//            if (t == null) {
//                return false;
//            }
//            if ((!t.passenger.equals(ticket.passenger)) || (t.coach != ticket.coach) || (t.seat != ticket.seat) || (t.departure != ticket.departure) || (t.arrival != ticket.arrival)) {
//                return false;
//            }
//            if (!soldTickets.remove(ticket.tid, t)) {
//                return false;//refund by others
//            }
//            int seat = (ticket.coach - 1) * seatnum + (ticket.seat - 1);
//            int departure = ticket.departure - 1;
//            int arrival = ticket.arrival - 1;
//            while (true) {
//                int tmp = seats[seat].get();
//                int cleanTmp = tmp & (~((1 << arrival) - (1 << departure)));
//                if (seats[seat].compareAndSet(tmp, cleanTmp)) {
//                    adjustRemainTickets(departure, arrival, cleanTmp, 1);
//                    return true;
//                }
//            }
//        }
//
//        public void adjustRemainTickets(final int departure, final int arrival, final int rawSeat, int bias) {
//            while (true) {
//                int[][] newTable = first.get();
//                int[][] oldTable = tableIndex.getReference();
//                if (oldTable == first.get()) {
//                    newTable = second.get();
//                }
//                int stamp = tableIndex.getStamp();
//                for (int i = 0; i < arrival; ++i) {
//                    for (int j = i + 1; j < stationnum; ++j) {
//                        newTable[i][j] = oldTable[i][j];
//                    }
//                    for (int j = Math.max(i, departure) + 1; j < stationnum; ++j) {
//                        if ((rawSeat == 0) || (((1 << j) - (1 << i) & rawSeat) == 0)) {
//                            newTable[i][j] = oldTable[i][j] + bias;
//                        }
//                    }
//                }
//                for (int i = arrival; i < stationnum; ++i) {
//                    for (int j = i + 1; j < stationnum; ++j) {
//                        newTable[i][j] = oldTable[i][j];
//                    }
//                }
//                if (tableIndex.compareAndSet(oldTable, newTable, stamp, stamp + 1)) {
//                    return;
//                }
//            }
//        }
//    }
//
//    private final Train[] trains;
//    private final AtomicInteger shareTid;
//    final int routenum, stationnum;
//
//    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
//        if (stationnum > 30) {
//            System.out.print("station num too large");
//            System.exit(-1);
//        }
//        this.routenum = routenum;
//        this.stationnum = stationnum;
//        this.shareTid = new AtomicInteger(0);
//        this.trains = new Train[routenum];
//        for (int i = 0; i < routenum; i++) {
//            this.trains[i] = new Train(i + 1, coachnum, seatnum, stationnum);
//        }
//    }
//
//    @Override
//    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
//        if (departure < 1 || arrival < 1 || departure > stationnum || arrival > stationnum || departure >= arrival) {
//            return null;
//        }
//        return this.trains[route - 1].buyTicket(passenger, departure, arrival);
//    }
//
//    @Override
//    public int inquiry(int route, int departure, int arrival) {
//        if (departure < 1 || arrival < 1 || departure > stationnum || arrival > stationnum || departure >= arrival) {
//            return 0;
//        }
//        return this.trains[route - 1].inquiry(departure - 1, arrival - 1);
//    }
//
//    @Override
//    public boolean refundTicket(Ticket ticket) {
//        return this.trains[ticket.route - 1].refundTicket(ticket);
//    }
//}
