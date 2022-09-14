package ticketingsystem;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
//可重入读写锁
class Train
{
    public ReentrantReadWriteLock rwLock;
    public int coachNums;
    public int seatNums;
    public int stationNums;
    public int ticketSum;
    public int threadNum;
    public int[] seats;

    public ReentrantReadWriteLock.ReadLock r;
    public ReentrantReadWriteLock.WriteLock w;
    public ConcurrentHashMap soldTickets;
    public Train(int coachNums, int seatNums, int stationNums,int threadNum)
    {
        this.coachNums = coachNums;
        this.seatNums = seatNums;
        this.stationNums = stationNums;

        this.threadNum = threadNum;
        ticketSum = coachNums * seatNums;
        seats = new int[ticketSum];
        soldTickets = new ConcurrentHashMap(1024);
        rwLock = new ReentrantReadWriteLock();

        r = rwLock.readLock();
        w = rwLock.writeLock();
        Arrays.fill(seats,0);


    }
}
public class TicketingDS implements TicketingSystem {
    public Train[] trains;
    public int[][] ticketStandards;
    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum)
    {
        trains = new Train[routenum];
        for(int i = 0 ; i < routenum; i++)
            trains[i] = new Train(coachnum,seatnum,stationnum,threadnum);

        ticketStandards = new int[stationnum+1][stationnum+1];
        for(int i =0;i<stationnum+1;i++)
            Arrays.fill(ticketStandards[i],0);
        for(int i =1;i<stationnum+1;i++)
        {
            for(int j =i+1;j<stationnum+1;j++)
                ticketStandards[i][j]=(1<<(j-1))-(1<<(i-1));
        }

    }
    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        Train t = trains[route-1];
        int ticketToSell;
        long timeStamp = System.nanoTime()<<8;
        Ticket ticket = new Ticket();
        ticket.departure=departure;
        ticket.arrival=arrival;
        ticket.passenger=passenger;
        ticket.route=route;
        ticket.tid = timeStamp + (Thread.currentThread().getId()%t.threadNum);

        try {
            t.w.lock();
            for (ticketToSell = 0; ticketToSell < t.ticketSum; ticketToSell++) {
                if ((t.seats[ticketToSell] & ticketStandards[departure][arrival]) == 0)
                    break;
                else
                    continue;
            }
            if(ticketToSell==t.ticketSum)
                return null;
            t.seats[ticketToSell] |= ticketStandards[departure][arrival];
        } finally {
            t.w.unlock();
        }

        ticket.coach = ticketToSell / t.seatNums + 1;
        ticket.seat = ticketToSell % t.seatNums + 1;
        t.soldTickets.put(ticket.tid, ticket);

        return ticket;
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        Train t = trains[route-1];
        int ticketsLeft = 0;
        try
        {
            t.r.lock();
            for(int i = 0;i<t.ticketSum;i++)
            {
                if((t.seats[i]&ticketStandards[departure][arrival])==0)
                    ticketsLeft++;
            }
        }
        finally
        {
            t.r.unlock();
        }
        return ticketsLeft;
    }

    @Override
    public boolean refundTicket(Ticket ticket) {
        if(ticket == null || ticket.route <= 0 || ticket.route > trains.length)
            return false;
        Train t = trains[ticket.route-1];
        Ticket refundTicket = (Ticket) t.soldTickets.get(ticket.tid);
        if(refundTicket == null)
            return false;
        if(!(ticket.coach == refundTicket.coach &&
                ticket.seat == refundTicket.seat &&
                ticket.departure == refundTicket.departure &&
                ticket.arrival == refundTicket.arrival &&
                ticket.passenger.equals(refundTicket.passenger)))
            return false;
        int index = (ticket.coach-1)*t.seatNums + ticket.seat-1;
        try{
            t.w.lock();
            if(t.soldTickets.remove(ticket.tid)!=null)
            {
                t.seats[index] &= ~ticketStandards[ticket.departure][ticket.arrival];
                return true;
            }
            return false;
        }
        finally {
            t.w.unlock();
        }
    }
}

// 乐观锁
//package ticketingsystem;
//import java.util.Arrays;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReentrantLock;
//class Train
//{
//    public int coachNums;
//    public int seatNums;
//    public int stationNums;
//    public int ticketSum;
//    public int threadNums;
//    public int[] seats;
//    public ConcurrentHashMap soldTickets;
//    public Lock[] seatLocks;
//
//    public Train(int coachNums, int seatNums, int stationNums, int threadNums)
//    {
//        this.coachNums = coachNums;
//        this.seatNums = seatNums;
//        this.stationNums = stationNums;
//        this.threadNums = threadNums;
//        ticketSum = coachNums * seatNums;
//        seats = new int[ticketSum];
//        soldTickets = new ConcurrentHashMap(1024);
//        seatLocks = new Lock[ticketSum];
//        Arrays.fill(seats,0);
//        for(int i = 0;i<ticketSum;i++)
//            seatLocks[i] = new ReentrantLock();
//    }
//}
//public class TicketingDS implements TicketingSystem {
//    public Train[] trains;
//    public int[][] ticketStandards;
//    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum)
//    {
//        trains = new Train[routenum];
//        for(int i = 0 ; i < routenum; i++)
//            trains[i] = new Train(coachnum,seatnum,stationnum,threadnum);
//        ticketStandards = new int[stationnum+1][stationnum+1];
//        for(int i =0;i<stationnum+1;i++)
//            Arrays.fill(ticketStandards[i],0);
//        for(int i =1;i<stationnum+1;i++)
//        {
//            for(int j =i+1;j<stationnum+1;j++)
//                ticketStandards[i][j]=(1<<(j-1))-(1<<(i-1));
//        }
//
//    }
//    @Override
//    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
//        Train t = trains[route-1];
//        int ticketToSell;
//        long timeStamp = System.nanoTime()<<8;
//        Ticket ticket = new Ticket();
//        ticket.departure=departure;
//        ticket.arrival=arrival;
//        ticket.passenger=passenger;
//        ticket.route=route;
//        ticket.tid = timeStamp + (Thread.currentThread().getId()%t.threadNums);
//
//        while(true)
//        {
//            for(ticketToSell = 0; ticketToSell<t.ticketSum;ticketToSell++)
//            {
//                if((t.seats[ticketToSell]&ticketStandards[departure][arrival])==0)
//                    break;
//                else
//                    continue;
//            }
//
//            if(ticketToSell==t.ticketSum)
//                return null;
//            try{
//
//                t.seatLocks[ticketToSell].lock();
//                if((t.seats[ticketToSell]&ticketStandards[departure][arrival])!=0)
//                    continue;
//                t.seats[ticketToSell]|=ticketStandards[departure][arrival];
//                ticket.coach = ticketToSell/t.seatNums +1;
//                ticket.seat = ticketToSell%t.seatNums +1;
//                t.soldTickets.put(ticket.tid,ticket);
//                return ticket;
//            }
//
//            finally {
//                t.seatLocks[ticketToSell].unlock();
//            }
//        }
//    }
//
//    @Override
//    public int inquiry(int route, int departure, int arrival) {
//        Train t = trains[route-1];
//        int ticketsLeft = 0;
//        int ticketsLeftNew= 0;
//
//            for(int i = 0;i<t.ticketSum;i++)
//            {
//                if((t.seats[i]&ticketStandards[departure][arrival])==0)
//                {
//                    ticketsLeft++;
//                }
//
//            }
//            while (true)
//            {
//                ticketsLeftNew = 0;
//                for(int i = 0;i<t.ticketSum;i++)
//                {
//                    if((t.seats[i]&ticketStandards[departure][arrival])==0)
//                    {
//                        ticketsLeftNew++;
//                    }
//
//                }
//                if(ticketsLeft==ticketsLeftNew)
//                    break;
//                else
//                {
//                    ticketsLeft = ticketsLeftNew;
//                }
//            }
//
//
//        return ticketsLeft;
//    }
//
//    @Override
//    public boolean refundTicket(Ticket ticket) {
//        if(ticket == null||ticket.route<=0||ticket.route>trains.length)
//            return false;
//        Train t = trains[ticket.route-1];
//        Ticket refundTicket = (Ticket) t.soldTickets.get(ticket.tid);
//        if(refundTicket == null)
//            return false;
//        boolean flag = ticket.coach == refundTicket.coach &&
//                ticket.seat == refundTicket.seat &&
//                ticket.departure == refundTicket.departure &&
//                ticket.arrival == refundTicket.arrival &&
//                ticket.passenger.equals(refundTicket.passenger);
//        if(!flag)
//            return false;
//        int index = (ticket.coach-1)*t.seatNums + ticket.seat-1;
//
//        try{
//            t.seatLocks[index].lock();
//            if(t.soldTickets.remove(ticket.tid)!=null)
//            {
//                t.seats[index]&=~ticketStandards[ticket.departure][ticket.arrival];
//                return true;
//            }
//        }
//        finally {
//            t.seatLocks[index].unlock();
//        }
//        return false;
//
//    }
//
//
//}
