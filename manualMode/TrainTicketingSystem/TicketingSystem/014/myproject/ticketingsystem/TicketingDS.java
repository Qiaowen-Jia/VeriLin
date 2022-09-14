package ticketingsystem;



import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


class BitOp {

    public static boolean isZero ( long n , int from , int to) {
        long mask = (-1L << from) & (-1L >>> -to) ;
        return  ( n & mask )== 0L ;
    }

    public static long getBit (long n , int index) {
        long mask = 1L << index ;
        return  n & mask;
    }

    public static long setBits (long n , int from , int to) {
        long mask = (-1L << from) & (-1L >>> -to) ;
        return  n  |= ( mask );
    }

    public static  long clearBits (long n , int from , int to) {
        long mask = (-1L << from) & (-1L >>> -to) ;
        return n &= ( ~mask );
    }
}


class Route {
    final int coachnum;
    final int seatnum;
    final int stationnum;

    boolean seats[][];

    long seats2[];
    Integer seatlock[];


    AtomicInteger remainTickets[][];

    public  Route (int coachnum , int seatnum , int stationnum) {
        this.coachnum = coachnum;
        this.seatnum = seatnum;
        this.stationnum = stationnum;
        seats = new boolean[coachnum*seatnum][stationnum - 1];

        seats2 = new long[coachnum*seatnum];
        seatlock = new Integer[coachnum*seatnum];
        for (int i = 0; i < coachnum * seatnum ; ++i) {
            seatlock[i] = 0;
        }

        remainTickets = new AtomicInteger [stationnum][stationnum];

        for ( int i = 0 ; i < stationnum - 1; ++i)
            for (int j =  i + 1 ; j < stationnum ; ++j) {
                remainTickets[i][j] = new AtomicInteger(coachnum * seatnum);
            }
    }

    public boolean invalidSeat2(int seq , int de ,int arr) {
        return BitOp.isZero(seats2[seq],de,arr);
    }
    public boolean lockAndSetSeat2 ( int seq , int de , int arr) {
        synchronized (seatlock[seq]) {
            if (invalidSeat2( seq , de , arr )) {
                seats2[seq] = BitOp.setBits(seats2[seq],de,arr);
                return true;
            }
        }
        return  false;
    }
    public void lockAndfreeSeat2 (int seq , int de , int arr) {
        synchronized (seatlock[seq]) {
            seats2[seq] = BitOp.clearBits(seats2[seq],de,arr);
        }
    }


    public boolean invalidSeat(int seq , int de ,int arr) {
        for ( int i = de ; i < arr ; ++i)
            if (seats[seq][i])
                return false;
        return true;
    }
    public boolean lockAndSetSeat ( int seq , int de , int arr) {
        synchronized (seats[seq]) {
            if (invalidSeat( seq , de , arr )) {
                for (int i = de; i < arr; ++i)
                    seats[seq][i] = true;
                return true;
            }
        }
        return  false;
    }
    public void lockAndfreeSeat (int seq , int de , int arr) {
        synchronized (seats[seq]) {
            for (int i = de; i < arr; ++i)
                seats[seq][i] = false;
        }
    }

    public void deqRemainTickets ( int seq, int de , int arr) {
        /*
            de arr bg end
            2   4  0   5
     seat            0   1   2   3   4   5
                     1   0   1   1   0   1
     station       0   1   2   3   4   5   6
            1,2
                3,4,5

         [de,arr-1]被置为1
         */
        int bg = de - 1, end = arr;
        while ( bg >= 0 && !seats[seq][bg])
            --bg;
        ++bg;
        while ( end < stationnum - 1 && !seats[seq][end] )
            ++end;
        end--;


        for ( int i = bg ; i <= de ; ++i)
            for ( int j = de + 1 ; j <=  end + 1; ++j)
                remainTickets[i][j].decrementAndGet();
    }

    public void incrRemainTickets ( int seq , int de , int arr) {
        int bg = de - 1, end = arr;
        while ( bg >= 0 && !seats[seq][bg])
            --bg;
        ++bg;
        while ( end < stationnum - 1 && !seats[seq][end] )
            ++end;
        end--;
        for ( int i = bg ; i <= de ; ++i)
            for ( int j = de + 1 ; j <=  end + 1; ++j)
                remainTickets[i][j].incrementAndGet();
    }

    public int getRemainTickets(int de , int arr) {
        return remainTickets[de][arr].get();
    }

}



public class TicketingDS implements TicketingSystem {


    private ConcurrentHashMap<Long , Ticket> soldTickets = new ConcurrentHashMap<Long, Ticket>();
    private AtomicLong seqNum = new AtomicLong(0);
    private Random random = new Random();
    Route[] routes = null;



    private int _findSeatSeq(Route currRoute , int de , int arr ) {
        final int totalSeats = currRoute.coachnum * currRoute.seatnum;
        int rndSeq = random.nextInt(totalSeats);
        int startSeq = (rndSeq + 1) % totalSeats;
        for (int i = startSeq; i != rndSeq; i = (i + 1) % totalSeats) {
            if (currRoute.invalidSeat(i, de, arr)) {
                if (currRoute.lockAndSetSeat(i, de, arr)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int _findSeatSeq2(Route currRoute , int de , int arr ) {
        final int totalSeats = currRoute.coachnum * currRoute.seatnum;
        int rndSeq = random.nextInt(totalSeats);
        int startSeq = (rndSeq + 1) % totalSeats;
        for (int i = startSeq; i != rndSeq; i = (i + 1) % totalSeats) {
            if (currRoute.invalidSeat2(i, de, arr)) {
                if (currRoute.lockAndSetSeat2(i, de, arr)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int findSeatSeq (Route currRoute , int de , int arr ) {
//        return _findSeatSeq2(currRoute,de,arr);
      return _findSeatSeq(currRoute,de,arr);

    }


    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
            routes = new Route[routenum];
            for( int i = 0 ; i < routenum ; ++i) {
                routes[i] = new Route(coachnum,seatnum,stationnum);
            }
    }

    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        if(departure >= arrival) {
            return null;
        }

        int ro = route - 1 , de = departure - 1 , arr = arrival -1;
        Route currRoute = routes[ro];

        int trgtSeq = findSeatSeq(currRoute,de,arr);

        //find

        //找到了
        if(trgtSeq != - 1) {
            Ticket t = new Ticket();
            t.route = route;
            t.arrival = arrival;
            t.departure = departure;
            t.passenger = passenger;
            t.coach = (trgtSeq / currRoute.seatnum )+ 1;
            t.seat =  (trgtSeq % currRoute.seatnum )+ 1;

            //更新 tid
            t.tid = seqNum.incrementAndGet();

            //3. 修改受影响的区间的余票数
            currRoute.deqRemainTickets(trgtSeq,de,arr);

            //3. 更新售出票集合
            soldTickets.put(t.tid , t);
            return t;
        }
        else
            //没找着
            return null;
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        return routes[route-1].getRemainTickets(departure-1,arrival-1);
    }

    @Override
    public boolean refundTicket(Ticket ticket) {

        long seq = ticket.tid ;
        Ticket t;
        //过滤伪造的票据
        if( (t = soldTickets.get(seq)) == null)
            return false;
        if(t.arrival != ticket.arrival)return false;
        if(t.coach != ticket.coach)return false;
        if(t.departure != ticket.departure)return false;
        if(t.route != ticket.route)return false;
        if(!t.passenger.equals(ticket.passenger))return false;
        if(t.seat != ticket.seat)return false;

        Route currRoute = routes[t.route - 1];
        int trgtSeq = ( t.coach - 1 )*currRoute.seatnum + (t.seat - 1) ;
        int de = t.departure - 1;
        int arr = t. arrival -1 ;

        //1.清空位图
        currRoute.lockAndfreeSeat(trgtSeq, de, arr);
//        currRoute.lockAndfreeSeat2(trgtSeq, de, arr);

        //2. 增加受影响的区间的余票数
        currRoute.incrRemainTickets(trgtSeq, de , arr );

        soldTickets.remove(seq);

        return true;
    }
}
