package ticketingsystem;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TicketingDS implements TicketingSystem
{
    static  int routenum;
    static  int coachnum;
    static  int seatnum;
    static  int stationnum;
    static  int counter = 0;
    int threadnum;
    Train [] train;

    public  TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum)
    {
        this.routenum = routenum;
        this.stationnum = stationnum;
        this.seatnum = seatnum;
        this.coachnum = coachnum;
        this.threadnum = threadnum;
        this.train = new Train[routenum];
        for(int i=0; i<routenum; i++)
        {
            this.train[i] = new Train();
            this.train[i].Init(coachnum, seatnum, stationnum);
        }
    }
    public Ticket buyTicket(String passenger, int route, int departure, int arrival)
    {
        return train[route-1].buyTicket(passenger, route, departure, arrival);
    }
    public int inquiry(int route, int departure, int arrival)
    {
        return train[route-1].inquiry(route, departure, arrival);
    }

    public boolean refundTicket(Ticket ticket)
    {
        return train[ticket.route-1].refundTicket(ticket);
    }
}

class Train {
    Seat [] seat;
    int seatnum;
    int coachnum;
    int stationnum;
    int counter;
    int base;
    ConcurrentHashMap <Long, Ticket>CHM;
    ReentrantReadWriteLock lock;
    public void Init(int coachnum, int seatnum, int stationnum)
    {
        this.seat = new Seat[seatnum*coachnum];
        for(int i=0; i<seatnum*coachnum; i++)
        {
            this.seat[i] = new Seat();
            seat[i].Init(stationnum);
        }
        this.seatnum = seatnum;
        this.coachnum = coachnum;
        this.stationnum = stationnum;
        this.counter = 0;
        this.lock = new ReentrantReadWriteLock(false);
        this.base = 1073741824/coachnum;
        this.CHM = new ConcurrentHashMap<>();
    }


    public Ticket buyTicket(String passenger, int route, int departure, int arrival)
    {


        int mark=0;
        int res;
        for(int i = departure-1; i<arrival-1;i++)
        {
            mark = mark | 1<<i;
        }

        for(int i = 0; i<seatnum*coachnum; i++)
        {

            res = mark & seat[i].station;
            if(res ==0)
            {
                lock.writeLock().lock();
                try
                {
                    res = mark & seat[i].station;
                    if(res==0)
                    {
                        seat[i].station = seat[i].station | mark;
                        Ticket tkt = new Ticket();
                        tkt.tid = counter+(route-1)*base;
                        //System.out.println(counter);
                        tkt.passenger = passenger;
                        tkt.route = route;
                        tkt.coach = i/seatnum+1;
                        tkt.seat = i-(tkt.coach-1)*seatnum+1;
                        tkt.departure = departure;
                        tkt.arrival = arrival;
                        counter++;
                        CHM.put(tkt.tid, tkt);
                        return tkt;
                    }

                }
                finally {
                    lock.writeLock().unlock();
                }

            }
        }
        return null;
    }

    public int inquiry(int route, int departure, int arrival)
    {
        //synchronized (this)
        int remain = 0;
        int mark=0;
        int res;
        for(int i = departure-1; i<arrival-1;i++)
        {
            mark = mark | 1<<i;
        }

        lock.readLock().lock();
        try
        {
            for(int i = 0; i<seatnum*coachnum; i++)
            {
                res = mark & seat[i].station;
                if(res ==0)
                {
                    remain++;
                }
            }
            return remain;
        }
        finally {
            lock.readLock().unlock();
        }


    }

    public boolean refundTicket(Ticket ticket)
    {
        //synchronized (this)
        lock.writeLock().lock();
        try
        {
            int mark = 0;
            int res;
            for(int i = ticket.departure-1; i<ticket.arrival-1;i++)
            {
                mark = mark | 1<<i;
            }
            res = seat[(ticket.coach-1)*seatnum+ticket.seat-1].station & mark;
            Ticket des = CHM.get(ticket.tid);
            if(res == mark && des!=null && des.arrival==ticket.arrival && des.departure==ticket.departure
                    &&des.route==ticket.route&&des.seat==ticket.seat&&des.coach==ticket.coach&&des.passenger==ticket.passenger)
            {
                seat[(ticket.coach-1)*seatnum+ticket.seat-1].station =
                        seat[(ticket.coach-1)*seatnum+ticket.seat-1].station & ~(mark);
                CHM.remove(ticket.tid);
            }
            else
                return false;


            //counter--;
            return true;
        }
        finally {
            lock.writeLock().unlock();
        }
    }
}

class Seat {
    int  station;
    int stationnum;
    public void Init(int stationnum)
    {
        this.station = 0;
        this.stationnum = stationnum;
    }
}
