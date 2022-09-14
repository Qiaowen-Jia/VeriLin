package ticketingsystem;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

public class List {
    private static final Long INI = (long) 0x1 << 63;
    // private static int routenum;
    private static int seatnum;
    private static int stationnum;
    // private static int threadnum;
    private static int TrainSeatNum;
    private static AtomicInteger tid = new AtomicInteger(0);
    // private static hopscotch hash;
    private Node nd_lst[];
    // private CounterArray counterArray;
    private LongAdder counters[];

    List(int TrainSeatNum1, int stationnum1, int seatnum1, int routenum1, int threadnum1) {
        // routenum = routenum1;
        seatnum = seatnum1;
        stationnum = stationnum1;
        // threadnum = threadnum1;
        TrainSeatNum = TrainSeatNum1;
        // hash = new hopscotch(64, TrainSeatNum, TrainSeatNum * stationnum * routenum);
        this.nd_lst = new Node[TrainSeatNum];
        for (int i = 0; i < TrainSeatNum; i++) {
            this.nd_lst[i] = new Node(stationnum);
        }
        // int nanos1 = (int) (Math.sqrt(threadnum) * 30), range1 = Math.max(threadnum>>1,1);
        // this.counterArray = new CounterArray(stationnum, TrainSeatNum,nanos1, range1);
        int L = stationnum*(stationnum-1)/2;
        counters = new LongAdder[L];
        for(int i = 0;i<L;i++){
            counters[i] = new LongAdder();
            counters[i].add(TrainSeatNum);
        }
    }

    public boolean f_add(Ticket ticket) {
        long a = INI >> (ticket.departure - 1);
        long b = INI >> (ticket.arrival - 1);
        long c = a ^ b;
        int div = ThreadLocalRandom.current().nextInt(TrainSeatNum);// avoid competetion
        for (int i = div; i < TrainSeatNum; i++) {
            if (this.nd_lst[i].f_set_on(c)) {
                ticket.coach = (int) i / seatnum + 1;
                ticket.seat = i % seatnum + 1;
                ticket.tid = tid.incrementAndGet();
                // if(!hash.add((ticket))){
                //     this.nd_lst[i].f_set_off(c);
                //     continue;
                // } 
                Ticket tkt = Node.cloneTicket(ticket);
                this.nd_lst[i].add(tkt);
                // For inquiry!
                int[] mark = this.nd_lst[i].f_get_mark(ticket.departure, ticket.arrival);
                // this.counterArray.f_decrement(ticket.departure, ticket.arrival, mark);
                this.f_decrement(ticket.departure, ticket.arrival, mark);
                return true;
            }
        }
        for (int i = div - 1; i >= 0; i--) {
            if (this.nd_lst[i].f_set_on(c)) {
                ticket.coach = (int) i / seatnum + 1;
                ticket.seat = i % seatnum + 1;
                ticket.tid = tid.incrementAndGet();
                // if (!hash.add((ticket))) {
                //     this.nd_lst[i].f_set_off(c);
                //     continue;
                // }
                Ticket tkt = Node.cloneTicket(ticket);
                this.nd_lst[i].add(tkt);
                // For inquiry!
                int[] mark = this.nd_lst[i].f_get_mark(ticket.departure, ticket.arrival);
                // this.counterArray.f_decrement(ticket.departure, ticket.arrival, mark);
                this.f_decrement(ticket.departure, ticket.arrival, mark);
                return true;
            }
        }
        return false;
    }

    public boolean f_remove(Ticket ticket) {
        // Ticket rtn = hash.remove(ticket);
        int idx = (ticket.coach - 1) * seatnum + ticket.seat - 1;
        if (this.nd_lst[idx].remove(ticket)) {
            this.nd_lst[idx].f_set_off(ticket.departure, ticket.arrival);
            // For inquiry!
            int[] mark = this.nd_lst[idx].f_get_mark(ticket.departure, ticket.arrival);
            // this.counterArray.f_increment(ticket.departure, ticket.arrival, mark);
            this.f_increment(ticket.departure, ticket.arrival, mark);
            return true;
        }
        return false;
    }

    public int f_get_left(int i) {
        // return this.counterArray.f_get_left(i);
        return this.counters[i].intValue();
    }

    public int f_get_left(long c) {
        int rtn2 = 0;
        for (int j = 0; j < TrainSeatNum; j++) {
            if (nd_lst[j].f_NAND(c)) {
                rtn2++;
            }
        }
        return rtn2;
    }
    
    public int f_decrement(int dep, int arr, int[] mark) {
        for (int i = mark[0]; i < arr; i++) {
            int flag = Math.max(i + 1, dep + 1);
            for (int j = flag; j <= mark[1]; j++) {
                int k = Node.f_cvt_idx(i, j, stationnum);
                this.counters[k].decrement();
            }
        }
        return 0;
    }

    public int f_increment(int dep, int arr, int[] mark) {
        for (int i = mark[0]; i < arr; i++) {
            int flag = Math.max(i + 1, dep + 1);
            for (int j = flag; j <= mark[1]; j++) {
                int k = Node.f_cvt_idx(i, j, stationnum);
                this.counters[k].increment();
            }
        }
        return 0;
    }

}