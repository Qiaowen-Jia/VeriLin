package ticketingsystem;

import java.util.concurrent.locks.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RouteManagerBitmap {
    private int coachnum, seatnum, stationnum;
    private BitMap [][] bitmap;
    private EmptyNodeSentry [][] queue;
    private int [][] counter; // departure - arrival
                            // 1-2 : 1-3 : 1-4
                            //     : 2-3 : 2-4
                            //           : 3-4
    private SoldTicketNode [][][] soldTicket; // coach - seat - departure

    private int id; // NOTE: just for debug;
    private static int id_s = 0;

    // private ReentrantReadWriteLock [][] cntLock;
    private Lock [][] cntRLock;
    private Lock [][] cntWLock;

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private Lock rlock = lock.readLock();
    private Lock wlock = lock.writeLock();

    public RouteManagerBitmap(int coachnum, int seatnum, int stationnum, int routeId) {
        this.coachnum = coachnum;
        this.seatnum = seatnum;
        this.stationnum = stationnum;
        id_s++;
        id = id_s;
        // TODO
        bitmap = new BitMap[coachnum][seatnum];
        queue = new EmptyNodeSentry[stationnum-1][stationnum-1];
        counter = new int[stationnum-1][stationnum-1];
        // cntLock = new ReentrantReadWriteLock[stationnum-1][stationnum-1];
        cntRLock = new Lock[stationnum-1][stationnum-1];
        cntWLock = new Lock[stationnum-1][stationnum-1];
        soldTicket = new SoldTicketNode[coachnum][seatnum][stationnum-1];
        // [0][0] : 1-2  --- [0][stationnum-2] : 1 - stationnum
        // [i][j] : i+1 - j+2
        // [stationnum-2][stationnum-2] : stationnum-1 - station-2
        counter[getFirst(1)][getSecond(stationnum)] = coachnum * seatnum;

        for (int i = 0; i < stationnum-1; i++ ) { // TODO
            for (int j = 0; j < stationnum-1; j++) {
                ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
                cntRLock[i][j] = rwLock.readLock();
                cntWLock[i][j] = rwLock.writeLock();
                queue[i][j]    = new EmptyNodeSentry();
            }
        }
        for(int i = 1; i <= coachnum; i++) {
            for (int j = 1; j <= seatnum; j++) {
                queue[0][stationnum-2].enq(i, j);
            }
        }
        for (int i = 0; i < coachnum; i++) {
            for (int j = 0; j < seatnum; j++) {
                bitmap[i][j] = new BitMap(stationnum);
            }
        }
        for (int i = 0; i < coachnum; i++) {
            for (int j = 0; j < seatnum; j++) {
                for (int k = 0; k < stationnum-1; k++) {
                    soldTicket[i][j][k] = new SoldTicketNode();
                }
            }
        }

        details("init");
    }

    private int getFirst(int idx) {
        return idx-1;
    }
    private int getSecond(int idx) {
        return idx-2;
    }

    // private int getIndex(int begin, int end) { // begin from 1
    //     assert (begin < end && begin >= 1 && end <= stationnum);
    //     return (((begin - 1) * ((stationnum << 1) - begin)) >> 1) + end - begin -1;
    // }
    // private int getSize(int stationnum) {
    //     return (stationnum * (stationnum - 1)) >> 1;
    // }

    public int inquiry(int departure, int arrival) {
        rlock.lock();
        int cnt = 0;
        for (int i = 1; i <= departure; i++) {
            for (int j = arrival; j <= stationnum; j++) {
                cnt += counter[getFirst(i)][getSecond(j)];
            }
        }
        // details("inquiry");
        rlock.unlock();
        return cnt;
    }

    public Ticket buyTicket(String passenger, int departure, int arrival) {
        wlock.lock();
        for (int i = departure; i >= 1; i--) {
            for (int j = arrival; j <= stationnum; j++) {

                if (counter[getFirst(i)][getSecond(j)] <= 0) continue;

                EmptyNode emptyTicket;
                do {
                    emptyTicket = queue[getFirst(i)][getSecond(j)].deq();
                } while (emptyTicket != null && !bitmap[emptyTicket.coach-1][emptyTicket.seat-1].hasIt(i,j));
                if(i < departure) {
                    queue[getFirst(i)][getSecond(departure)].enq(emptyTicket.coach, emptyTicket.seat);
                    counter[getFirst(i)][getSecond(departure)]++;
                }
                if (j > arrival) {
                    queue[getFirst(arrival)][getSecond(j)].enq(emptyTicket.coach, emptyTicket.seat);
                    counter[getFirst(arrival)][getSecond(j)]++;
                }
                Ticket ticket = new Ticket();
                ticket.coach     = emptyTicket.coach;
                ticket.seat      = emptyTicket.seat;
                ticket.departure = departure;
                ticket.arrival   = arrival;
                ticket.passenger = passenger;
                ticket.tid = TicketID.get();
                soldTicket[ticket.coach-1][ticket.seat-1][departure-1].sold(ticket);
                counter[getFirst(i)][getSecond(j)]--;
                bitmap[ticket.coach-1][ticket.seat-1].sold(departure, arrival);

                // details("buyTicket0");
                wlock.unlock();
                return ticket;
            }
        }
        // details("buyTicket1");
        wlock.unlock();
        return null;
    }

    public boolean refundTicket(Ticket ticket) {
        wlock.lock();
        if (!soldTicket[ticket.coach-1][ticket.seat-1][ticket.departure-1].refund(ticket)) {
            // details("refundTicket0");
            wlock.unlock();
            return false;
        }

        JustInterval interval = bitmap[ticket.coach-1][ticket.seat-1].refund(ticket.departure, ticket.arrival);
        queue[getFirst(interval.departure)][getSecond(interval.arrival)].enq(ticket.coach, ticket.seat);
        if (interval.departure < ticket.departure) {
            counter[getFirst(interval.departure)][getSecond(ticket.departure)]--;
        }
        if (ticket.arrival < interval.arrival) {
            counter[getFirst(ticket.arrival)][getSecond(interval.arrival)]--;
        }
        counter[getFirst(interval.departure)][getSecond(interval.arrival)]++;
        // details("refundTicket1");
        wlock.unlock();
        return true;
    }

    private void details(String msg) {
        if (msg!="debug") return;
        if (msg=="init") {
            System.err.printf("============== Details Dump %d after %s===============\n", id, msg);
        } else {
            System.err.printf("==============%d Details Dump %d after %s===============\n", ReqConuter.get(), id, msg);
        }
        System.err.println("Counter:");
        for(int i = 1; i <= stationnum-1; i++) {
            System.err.printf("from %d: ", i);
            for(int j=2; j <= stationnum; j++) {
                System.err.printf("%d ", counter[getFirst(i)][getSecond(j)]);
            }
            System.err.printf("\n");
        }
        System.err.println("BitMap: ");
        for(int i = 0; i < coachnum; i++) {
            System.err.printf("coach %d: ", i+1);
            for (int j = 0; j < seatnum; j++) {
                System.err.printf("%x|", bitmap[i][j].value);
            }
            System.err.printf("\n");
        }
        System.err.println("Queue: ");
        for(int i = 1; i <= stationnum-1; i++) {
            for(int j = 2; j <= stationnum; j++) {
                System.err.printf("%d to %d:", i, j);
                queue[getFirst(i)][getSecond(j)].dump();
                System.err.printf("\n");
            }
        }
        // System.err.println("==============            end             ===============");
    }
}

class BitMap {
    public int value;

    /* if stationnum is 8
     * 0 for empty, 1 for sold
     * if there are 2 stations, then only one bit b1 is needed
     * bit in i means the interval i+1 - i+2 is empty
     */
    public BitMap(int stationnum) {
        assert stationnum < Integer.SIZE : "if stationnum is larger than Int.SIZE, please change value to long and try again";
        assert stationnum > 0 : "stationnum should be larger than 0";
        value = shiftDec(stationnum-1);
    }

    private int shiftDec(int a) {
        return (1 << a) - 1;
    }

    private int tranInterval2Bitmap(int departure, int arrival) {
        return shiftDec(departure-1) ^ shiftDec(arrival-1);
    }

    public boolean hasIt(int departure, int arrival) {
        int bm = tranInterval2Bitmap(departure, arrival);
        return (bm & value) == bm && siteEmpty(value, departure-1) && siteEmpty(value, arrival);
    }

    public void sold(int departure, int arrival) {
        value &= ~tranInterval2Bitmap(departure, arrival);
    }

    private boolean siteEmpty(int va, int site) {
        return site == 0 ? true : (va & (1 << (site - 1))) == 0;
    }

    public JustInterval refund(int departure, int arrival) {
        value |= tranInterval2Bitmap(departure, arrival);
        int low = departure-1;
        int high = arrival;
        for ( ; low >= 1; low-- ) {
            if (siteEmpty(value, low)) break;
        }
        for ( ; ; high++) {
            if (siteEmpty(value, high)) break;
        }
        JustInterval interval = new JustInterval();
        interval.departure = low+1;// TODO
        interval.arrival   = high;
        return interval;
    }
}

class JustInterval {
    public int departure;
    public int arrival;
}

class EmptyNodeSentry {
    private EmptyNode head;
    private EmptyNode tail;

    /* Head                            tail
     *   |                              |
     *  node -> node -> ... -> node -> node
     */

    public void enq(int coach, int seat) {
        EmptyNode node = new EmptyNode(coach, seat);
        if (tail != null) { tail.next = node; }
        if (head == null) { head = node; }
        tail = node;
    }

    public EmptyNode deq() {
        if (head == null) {
            System.err.println("Error, Should not deq null");
            return null;
        }
        EmptyNode node = head;
        head = head.next;
        if (head == null) { tail = null; }
        return node;
    }

    public boolean isEmpty() {
        return head==null;
    }

    public void dump() {
        EmptyNode tmp;
        System.err.printf("head->");
        if(head==null) System.err.printf("none");
        for (tmp = head; tmp != null; tmp = tmp.next) {
            System.err.printf("(%d|%d)->", tmp.coach, tmp.seat);
        }
        System.err.printf("tail");
    }
}

class EmptyNode {
    public int coach;
    public int seat;
    public EmptyNode next;

    public EmptyNode(int coach, int seat) {
        this.coach = coach;
        this.seat  = seat;
        this.next  = null;
    }

    public EmptyNode(int coach, int seat, EmptyNode next) {
        this.coach = coach;
        this.seat  = seat;
        this.next  = next;
    }
}

class SoldTicketNode {
    private PartTicket ticket;
    private boolean sold;

    public SoldTicketNode() {
        sold = false;
        ticket = null;
    }

    private boolean hasIt(Ticket ticket) {
        return (this.ticket != null &&
                this.ticket.tid       == ticket.tid       &&
                this.ticket.passenger == ticket.passenger &&
                this.sold);
    }

    public void sold(Ticket ticket) {
        if (this.ticket == null) {
            this.ticket = new PartTicket(ticket.tid, ticket.passenger);
            this.sold = true;
            return ;
        }
        this.ticket.tid = ticket.tid;
        this.ticket.passenger = ticket.passenger;
        this.sold = true;
    }

    public boolean refund(Ticket ticket) {
        if (!hasIt(ticket)) {
            return false;
        }
        sold = false;
        return true;
    }
}

class PartTicket {
    public long tid;
    public String passenger;

    public PartTicket(long tid, String passenger) {
        this.tid = tid;
        this.passenger = passenger;
    }
}

class ReqConuter {
    private static int tmp = 1;
    public static long get() {
        return tmp++; // NOTE: should not access concurrently;
    }
}