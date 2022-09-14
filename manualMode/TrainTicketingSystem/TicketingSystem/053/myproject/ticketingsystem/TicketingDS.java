package ticketingsystem;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class TicketingDS implements TicketingSystem {
    
    private int routeNum;
    private int coachNum;
    private int seatNum;
    private int stationNum;
    private int threadNum;
    
    // a lock per seat for buy and refund
    private AtomicBoolean[][][] isSeatLocked;
    // whether basic sections of a seat is sold or not
    private BitSet[][][] isSeatSold;
    // ticket id in a route, to calculate global id
    // TODO: sequential bottleneck?
    // ---> No, an independent ticket id for a coach make it worse
    private AtomicLong[] ticketIds;
    // a sold ticket list for each coach
    private ArrayList<ArrayList<ArrayList<Ticket>>> soldTickets;
    
    
    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
        this.routeNum = routenum;
        this.coachNum = coachnum;
        this.seatNum = seatnum;
        this.stationNum = stationnum;
        this.threadNum = threadnum;
        
        this.isSeatLocked = new AtomicBoolean[routenum][coachnum][seatnum];
        this.isSeatSold = new BitSet[routenum][coachnum][seatnum];
        this.ticketIds = new AtomicLong[routenum];
        this.soldTickets = new ArrayList<>();
        
        for (int i = 0; i < routenum; i++) {
            this.ticketIds[i] = new AtomicLong(0);
            this.soldTickets.add(new ArrayList<ArrayList<Ticket>>());
            for (int j = 0; j < coachnum; j++) {
                this.soldTickets.get(i).add(new ArrayList<Ticket>());
                for (int k = 0; k < seatnum; k++) {
                    this.isSeatLocked[i][j][k] = new AtomicBoolean(false);
                    this.isSeatSold[i][j][k] = new BitSet(stationnum - 1);
                }
            }
        }
        
    }

    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        // check parameters
        if (route > this.routeNum || departure > this.stationNum || arrival > this.stationNum 
                || route <= 0 || departure <= 0 || arrival < 0 || departure >= arrival)
            return null;
        
        int seat = -1, coach = -1;
        BitSet want = new BitSet(this.stationNum - 1);
        want.set(departure - 1, arrival - 1);
        ArrayList<Integer> lockedSeats = new ArrayList<Integer>();
        // TODO: parallel query?
        // ---> too complicated to synchronize, quit
        for (int i = 0; i < this.coachNum; i++) {
            for (int j = 0; j < this.seatNum; j++) {
                // whether this seat is sold?
                if (this.isSeatSold[route - 1][i][j].intersects(want))
                    continue;
                // try to lock available seat
                if (this.isSeatLocked[route - 1][i][j].compareAndSet(false, true)) {
                    // succeed, ensure it is still available
                    if (this.isSeatSold[route - 1][i][j].intersects(want)) {
                        this.isSeatLocked[route - 1][i][j].set(false);
                        continue;
                    }
                    
                    // ensured, buy it!
                    coach = i + 1;
                    seat = j + 1;
                    break;
                } else
                    // failed, try again later
                    lockedSeats.add(i * this.seatNum + j);
            }
            // found and locked, quit query
            if (coach != -1)
                break;
        }
        
        // no seat is found, try again
        if (coach == -1) {
            while (lockedSeats.size() != 0) {
                int tmp = lockedSeats.remove(0);
                int i = tmp / this.seatNum, j = tmp % this.seatNum;
                // basically, the same procedure
                if (this.isSeatSold[route - 1][i][j].intersects(want))
                    continue;
                if (this.isSeatLocked[route - 1][i][j].compareAndSet(false, true)) {
                    if (this.isSeatSold[route - 1][i][j].intersects(want)) {
                        this.isSeatLocked[route - 1][i][j].set(false);
                        continue;
                    }
                    
                    coach = i + 1;
                    seat = j + 1;
                    break;
                } else
                    lockedSeats.add(tmp);
            }
        }
        
        // get the ticket
        if (coach != -1) {
            this.isSeatSold[route - 1][coach - 1][seat - 1].or(want);
            this.isSeatLocked[route - 1][coach - 1][seat - 1].set(false);
            
            Ticket ticket = new Ticket();
            long routeTid = this.ticketIds[route - 1].getAndIncrement();
            ticket.tid = routeTid * this.routeNum + route;
            ticket.passenger = passenger;
            ticket.route = route;
            ticket.coach = coach;
            ticket.seat = seat;
            ticket.departure = departure;
            ticket.arrival = arrival;
            
            // add to the sold record
            ArrayList<Ticket> soldList = this.soldTickets.get(route - 1).get(coach - 1);
            synchronized(soldList) {
                soldList.add(ticket);
            }
            return ticket;
        } else 
            return null;
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        // check parameters
        if (route > this.routeNum || departure > this.stationNum || arrival > this.stationNum 
                || route <= 0 || departure <= 0 || arrival < 0 || departure >= arrival)
            return 0;

        int count = 0;
        BitSet want = new BitSet(this.stationNum - 1);
        want.set(departure - 1, arrival - 1);
        // wait-free traverse
        for (int i = 0; i < this.coachNum; i++)
            for (int j = 0; j < this.seatNum; j++)
                if (!this.isSeatSold[route - 1][i][j].intersects(want))
                    count++;
        return count;
        
    }

    @Override
    public boolean refundTicket(Ticket ticket) {
        int route = ticket.route, coach = ticket.coach, seat = ticket.seat;
        int departure = ticket.departure, arrival = ticket.arrival;
        
        // invalid ticket
        if (route <= 0 || route > this.routeNum || coach <= 0 || coach > this.coachNum)
            return false;
        ArrayList<Ticket> soldList = soldTickets.get(route - 1).get(coach - 1);
        synchronized(soldList) {
            if (!soldList.contains(ticket))
                return false;
            soldList.remove(ticket);
        }

        BitSet sold = new BitSet(this.stationNum - 1);
        sold.set(departure - 1, arrival - 1);
        
        // lock this seat and release it
        // TODO: TTAS -> CLH or MCS?
        // ---> Not necessary, refunding is the most efficient method and the least used
        while (this.isSeatLocked[route - 1][coach - 1][seat - 1].get() || 
                !this.isSeatLocked[route - 1][coach - 1][seat - 1].compareAndSet(false, true)) ;
        this.isSeatSold[route - 1][coach -1][seat - 1].andNot(sold);
        this.isSeatLocked[route - 1][coach - 1][seat - 1].set(false);
        
        return true;
    }


}
