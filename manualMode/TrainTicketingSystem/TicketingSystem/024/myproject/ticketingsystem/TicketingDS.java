package ticketingsystem;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.*;

class Seat{
    int coach;
    int seat;
}

public class TicketingDS implements TicketingSystem {

    private int coachnum;//车厢个数
    private int seatnum;//每个车厢座位个数
    private int routenum;//车次个数
    private int stationnum;//站个数
    private int threadnum;//线程数
    private ReadWriteLock[][] routeLock;
    private ReentrantLock[][][] seatLock;
    private Lock[][] readlock;
    private Lock[][] writelock;
    private long[][][] trainseat;
    //private long[][] coachSeatAnd;
    private long[][] coachSeatOr;
    private AtomicInteger ID = new AtomicInteger(0);
    private AtomicInteger nonLock_Station[][];
    private ConcurrentHashMap<Ticket, Integer> soldTicket = new ConcurrentHashMap<>();

    private AtomicInteger BRstate[][];//0:buy;1:refund
    private Condition condition[];
    private Lock buyrefundLock[];
    private AtomicInteger buyAcquires[],buyReleases[],refundAcquires[],refundReleases[];

    //ToDo
    TicketingDS (int routenum ,int coachnum ,int seatnum ,int stationnum ,int threadnum )
    {
        this.coachnum = coachnum;
        this.routenum = routenum;
        this.seatnum = seatnum;
        this.stationnum = stationnum;
        this.threadnum = threadnum;
        routeLock = new ReentrantReadWriteLock[routenum][stationnum-1];
        readlock = new Lock[routenum][stationnum-1];
        writelock = new Lock[routenum][stationnum-1];
        trainseat = new long[routenum][coachnum][seatnum];
        //coachSeatAnd = new long[routenum][coachnum];
        coachSeatOr = new long[routenum][coachnum];
        nonLock_Station = new AtomicInteger[routenum][stationnum-1];
        seatLock = new ReentrantLock[routenum][coachnum][seatnum];

        BRstate = new AtomicInteger[routenum][2];
        buyrefundLock = new ReentrantLock[routenum];
        condition = new Condition[routenum];
        buyAcquires = new AtomicInteger[routenum];
        buyReleases = new AtomicInteger[routenum];
        refundAcquires = new AtomicInteger[routenum];
        refundReleases = new AtomicInteger[routenum];
        for(int i = 0; i < routenum; i++)
        {
            buyrefundLock[i] = new ReentrantLock();
            condition[i] = buyrefundLock[i].newCondition();
            buyAcquires[i]=buyReleases[i]=refundAcquires[i]=refundReleases[i]=new AtomicInteger(0);
        }


        for(int i = 0; i < routenum; i++)
        {
            BRstate[i][0] = new AtomicInteger(0);//[0]:buy's number;[1]:refund's number
            BRstate[i][1] = new AtomicInteger(0);
            for(int j = 0; j < stationnum-1; j++)
            {
                routeLock[i][j] = new ReentrantReadWriteLock();
                readlock[i][j] = routeLock[i][j].readLock();
                writelock[i][j] = routeLock[i][j].writeLock();

                nonLock_Station[i][j] = new AtomicInteger(0);
            }
        }
        for(int i = 0; i < routenum; i++)
            for(int j = 0; j < coachnum; j++) {
                //coachSeatAnd[i][j] = 0;
                coachSeatOr[i][j] = 0;
                for (int k = 0; k < seatnum; k++) {
                    trainseat[i][j][k] = 0;
                    seatLock[i][j][k] = new ReentrantLock();
                }
            }
    }

    public Ticket buyTicket(String passenger, int route, int departure, int arrival)
    {
        departure -= 1;
        arrival -= 1;
        route -= 1;

        Seat seat = new Seat();
        if(check_seat(route, departure, arrival, seat) == 0)
            return null;

        //实现buy refund互斥：加锁
        buyrefundLock[route].lock();
        try {
            while (!refundAcquires[route].equals(refundReleases[route]))
                condition[route].await();
            buyAcquires[route].getAndIncrement();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            buyrefundLock[route].unlock();
        }

        seatLock[route][seat.coach][seat.seat].lock();//加锁

        while(!check_buy(route, seat.coach, seat.seat, departure, arrival)){
            seatLock[route][seat.coach][seat.seat].unlock();//解锁
            if(check_seat(route, departure, arrival, seat) == 0)
                return null;
            seatLock[route][seat.coach][seat.seat].lock();//加锁，注意这时候seat变了
        }

        Ticket ticket = new Ticket();
        ticket.tid = (route+1) * 1000000L + ID.getAndIncrement();
        ticket.coach = seat.coach + 1;
        ticket.seat = seat.seat + 1;
        ticket.route = route+1;
        ticket.departure = departure+1;
        ticket.arrival = arrival+1;
        ticket.passenger = passenger;

        soldTicket.putIfAbsent(ticket,1);

        decide_buy(route, seat.coach, seat.seat, departure, arrival);
        for(int i = departure; i < arrival; i++)
        {
            nonLock_Station[route][i].getAndIncrement();
        }

        seatLock[route][seat.coach][seat.seat].unlock();//释放锁

        //实现buy refund互斥：解锁
        buyrefundLock[route].lock();
        try {
            buyReleases[route].getAndIncrement();
            if (buyReleases[route].equals(buyAcquires[route])) {
                condition[route].signalAll();
            }
        } finally {
            buyrefundLock[route].unlock();
        }

        return ticket;

    }

    public int inquiry(int route, int departure, int arrival)
    {
        departure -= 1;
        arrival -= 1;
        route -= 1;

        int[] tmp = new int[stationnum-1];
        int flag;
        int seatnum;
        while(true){
            for(int i = departure; i < arrival; i++)
            {
                tmp[i] = nonLock_Station[route][i].get();
            }

            flag = 1;
            seatnum = check_inquiry(route, departure, arrival);

            for(int i = departure; i < arrival; i++)
            {
                if(!nonLock_Station[route][i].compareAndSet(tmp[i], tmp[i])) {
                    flag = 0;
                    break;
                }
            }
            if(flag == 1)
                return seatnum;
        }
    }

    public boolean refundTicket(Ticket ticket)
    {
        int route = ticket.route-1;
        int coach = ticket.coach-1;
        int seat = ticket.seat-1;
        int departure = ticket.departure-1;
        int arrival = ticket.arrival-1;

        //实现buy refund互斥：加锁
        buyrefundLock[route].lock();
        try {
            while (!buyAcquires[route].equals(buyReleases[route]))
                condition[route].await();
            refundAcquires[route].getAndIncrement();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            buyrefundLock[route].unlock();
        }

        seatLock[route][coach][seat].lock();

        if (!soldTicket.remove(ticket,1)) {
            seatLock[route][coach][seat].unlock();
            return false;
        }

        earse_refund(route, coach, seat, departure, arrival);
        for(int i = departure; i < arrival; i++)
        {
            nonLock_Station[route][i].getAndIncrement();
        }
        seatLock[route][coach][seat].unlock();

        //实现buy refund互斥：解锁
        buyrefundLock[route].lock();
        try {
            refundReleases[route].getAndIncrement();
            if (refundReleases[route].equals(refundAcquires[route]))
                condition[route].signalAll();
        } finally {
            buyrefundLock[route].unlock();
        }

        return true;
    }

    //找空的seat
    private int check_seat(int route, int departure, int arrival, Seat seat)
    {
        long check = bitSet(departure, arrival);
        for(int j = 0; j < coachnum; j++) {
            for (int k = 0; k < seatnum; k++)
                if ((check & trainseat[route][j][k]) == 0) {
                    seat.coach = j;
                    seat.seat = k;
                    //System.out.println(Long.toBinaryString(trainseat[route][j][k]));
                    return 1;
                }
        }
        return 0;
    }

    //检查seat是否还是空着的，1表示还空着，否则没有
    private boolean check_buy(int route, int coach, int seat, int departure, int arrival)
    {
        long seatcheck = bitSet(departure, arrival);
        return ((trainseat[route][coach][seat] & seatcheck)==0);
    }

    //买座位
    private void decide_buy(int route, int coach, int seat, int departure, int arrival)
    {
        long buy = bitSet(departure, arrival);
        trainseat[route][coach][seat] |= buy;
        coachSeatOr[route][coach] |= buy;
    }

    private int check_inquiry(int route, int departure, int arrival)
    {
        int num = coachnum*seatnum;
        //System.out.println("num: "+num);
        long check = bitSet(departure, arrival);
        for(int j = 0; j < coachnum; j++) {
            if((check & coachSeatOr[route][j]) == 0)
                continue;
            for (int k = 0; k < seatnum; k++) {
                if ((check & trainseat[route][j][k]) != 0) {
                    //System.out.println(Long.toBinaryString(trainseat[route][j][k]));
                    num--;
                }
            }
        }
        return num;
    }

    private void earse_refund(int route, int coach, int seat, int departure, int arrival)
    {
        long earse = ~bitSet(departure, arrival);
        trainseat[route][coach][seat] &= earse;
        for(int i = 0; i < seatnum; i++)
        {
            coachSeatOr[route][coach] |= trainseat[route][coach][seat];
        }
        //System.out.println(Long.toBinaryString(trainseat[route][coach][seat]));
    }

    private long bitSet(int departure, int arrival)
    {
        long tmp = 0;
        for(int i = departure; i < arrival; i++){
            tmp |= (1L <<i);
        }
        return tmp;
    }
}
