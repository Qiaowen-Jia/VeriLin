package ticketingsystem;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class Node {

    final static Long INI = (long) 0x1 << 63;
    private static int stationnum; // equals to stationnum.
    private AtomicLong B = new AtomicLong(Long.valueOf(0));
    private AtomicReferenceArray<Ticket> T; // list if sold tickets.
    // private static AtomicLong tid = new AtomicLong(0);

    Node(int stationnum1) {
        stationnum = stationnum1;
        this.T = new AtomicReferenceArray<Ticket>(stationnum1);
    }

    public long f_get() {
        return this.B.get();
    }

    public boolean add(Ticket ticket){
        this.T.set(ticket.departure, ticket);
        // if(this.T.compareAndSet(ticket.departure, null, ticket)){
        //     return true;
        // }
        return false;
    }

    public boolean remove(Ticket ticket){
        Ticket tkt = T.get(ticket.departure);
        if(f_equals(ticket,tkt)){
            if(T.compareAndSet(ticket.departure, tkt, null)){
                return true;
            }
        }
        return false;
    }

    public int[] f_get_mark(int departure, int arrival) {
        int[] mark = new int[2];
        long bit_arr = this.B.get();
        int left = departure, right = arrival;
        long flag = INI >>> (departure - 1);
        while (left != 1) {
            if ((flag & bit_arr) != 0) {
                break;
            }
            flag <<= 1;
            left--;
        }
        flag = INI >>> (arrival);
        while (right != stationnum) {
            if ((flag & bit_arr) != 0) {
                break;
            }
            flag >>= 1;
            right++;
        }
        mark[0] = left;
        mark[1] = right;
        // if(left != 1 || right != stationnum){
        //     System.out.printf("\n%016x\t%d\t%d", bit_arr, left, right);
        //     System.out.flush();
        // }
        return mark;
    }

    public boolean f_set_on(long c) {
        long d = this.B.get();
        while ((d & c) == 0) {
            long e = c | d;
            if (this.B.compareAndSet(d, e)) {
                return true;
            } else {
                d = this.B.get();
            }
        }
        return false;
    }

    public boolean f_set_off(int departure, int arrival) {
        long a, b, c, e, d;
        a = INI >> (departure - 1);
        b = INI >> (arrival - 1);
        c = a ^ b;
        do {
            d = this.B.get();
            e = ((~c) & d);
            // System.out.printf("\n%016x\n%016x\n%016x\n", c, d, e);
            // System.out.flush();
        } while (!this.B.compareAndSet(d, e));
        return true;
    }

    public boolean f_set_off(long c) {
        long d = this.B.get(), e;
        do {
            d = this.B.get();
            e = ((~c) & d);
        } while (!this.B.compareAndSet(d, e));
        return true;
    }

    public boolean f_NAND(long c) {
        long d = this.B.get();
        if ((d & c) == 0) {
            return true;
        }
        return false;
    }

    public static int f_cvt_idx(int i, int j, int L) {
        int rtn = (int) (-((i * (i + 1)) >> 1) + j - 1 + L * (i - 1));
        return rtn;
    }

    public static Ticket cloneTicket(Ticket ticket) {
        Ticket rtn = new Ticket();
        rtn.arrival = ticket.arrival;
        rtn.coach = ticket.coach;
        rtn.departure = ticket.departure;
        rtn.passenger = ticket.passenger;
        rtn.route = ticket.route;
        rtn.seat = ticket.seat;
        rtn.tid = ticket.tid;
        return rtn;
    }

    public static boolean f_equals(Ticket t1, Ticket t2) {
        if(t1 == null && t2 == null){
            return true;
        } else if (t1 == null || t2 == null){
            return false;
        }
        boolean rtn = (t1.arrival == t2.arrival) && (t1.coach == t2.coach) && (t1.departure == t2.departure)
                && (t1.passenger == t2.passenger) && (t1.route == t2.route) && (t1.seat == t2.seat)
                && (t1.tid == t1.tid);
        return rtn;
    }
}
