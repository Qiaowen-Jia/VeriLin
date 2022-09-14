package ticketingsystem;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TicketingDS implements TicketingSystem {
    Integer Routenum;
    Integer Coachnum;
    Integer Seatnum;
    Integer Stationnum;

    private static ArrayList<SeatsStateArray> ssalist;
    private static ArrayList<leftSeatsArray> lsalist;

    public TicketingDS(Integer routenum, Integer coachnum, Integer seatnum, Integer stationnum, Integer threadnum) {
        this.Routenum = routenum;
        this.Coachnum = coachnum;
        this.Seatnum = seatnum;
        this.Stationnum = stationnum;

        ssalist = new ArrayList<>(routenum * coachnum);
        lsalist = new ArrayList<>(routenum);
        for (int i = 0; i < routenum; i++)
            lsalist.add(new leftSeatsArray(stationnum, coachnum * seatnum));
        for (int i = 0; i < routenum * coachnum; i++)
            ssalist.add(new SeatsStateArray(stationnum, seatnum));
    }

    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        try {
            leftSeatsArray lsa = lsalist.get(route - 1);
            long currenttid;
            int ticketsleft = lsa.seatsleft[departure - 1][arrival - 2];

            if (ticketsleft <= 0) {
                return null;//没票，直接返回
            }
            Ticket t = new Ticket();
            t.passenger = passenger;
            t.route = route;
            t.departure = departure;
            t.arrival = arrival;

            ArrayList<SeatsStateArray> ss = ssalist;
            int i;
            for (i = 0; i < this.Coachnum; i++) {
                t.coach = i + 1;
                int index = (route - 1) * this.Coachnum + i;
                SeatsStateArray ssa = ssalist.get(index);

                ssa.lock();//进入临界区
                boolean b = ssa.SeekValidSeat(t, lsa);
                if (b) {
                    ssa.unlock();//退出临界区
                    return t;
                }
                ssa.unlock();//退出临界区
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        try {
            leftSeatsArray lsa = lsalist.get(route - 1);

            lsa.readLock().lock();
            int lt = lsa.seatsleft[departure - 1][arrival - 2];
            lsa.readLock().unlock();
            return lt;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public boolean refundTicket(Ticket ticket) {
        try {
            leftSeatsArray lsa = lsalist.get(ticket.route - 1);
            SeatsStateArray ssa = ssalist.get((ticket.route - 1) * this.Coachnum + ticket.coach - 1);

            ssa.lock();//进入临界区,确认车票合法性
            boolean b = ssa.ReturnSeat(ticket, lsa);
            ssa.unlock();//退出临界区
            return b;
        } catch (Exception e) {
            return false;
        }

    }
}

class leftSeatsArray extends ReentrantReadWriteLock {
    public int[][] seatsleft;
    int DorAstationnum;

    public leftSeatsArray(int stationnum, int tatalseats) {
        this.DorAstationnum = stationnum - 1;
        seatsleft = new int[this.DorAstationnum][this.DorAstationnum];
        for (int i = 0; i < this.DorAstationnum; i++)
            for (int j = 0; j < this.DorAstationnum; j++)
                seatsleft[i][j] = tatalseats;
    }
}

//存储票数信息的数据结构，对应一节车厢，继承了ReentrantLock
class SeatsStateArray extends ReentrantLock {
    int blocknum;//区间数
    int maxseatnum;
    public long tidcount;//当前出票tid，用于唯一标识车票

    public boolean[][] validblocks;//记录每个座位上每个区间段是否被占用，被占用为true
    public long[][] currenttids;//记录每个座位上已售的车票id

    public SeatsStateArray(Integer stationnum, Integer Seatnum) {
        tidcount = 0;
        maxseatnum = Seatnum;
        blocknum = stationnum - 1;

        currenttids = new long[maxseatnum][blocknum];
        validblocks = new boolean[maxseatnum][blocknum];
    }

    //查询并分配可用座位
    public boolean SeekValidSeat(Ticket t, leftSeatsArray lsa) {
        int seat = 0;
        do {
            int i = t.departure - 1;
            do {
                if (validblocks[seat][i])
                    break;
            } while (++i < t.arrival - 1);

            if (i == t.arrival - 1)
                break;//该座位合适
        } while (++seat < this.maxseatnum);

        if (seat == this.maxseatnum)
            return false;//没有空座了

        tidcount++;
        t.seat = seat + 1;
        t.tid = tidcount * 1000000 + t.route * 10000 + (t.coach) * 100 + t.seat;
        for (int i = t.departure - 1; i < t.arrival - 1; i++) {
            validblocks[seat][i] = true;
            currenttids[seat][i] = t.tid;
        }

        int d = t.departure - 2, a = t.arrival - 1;
        while (d > -1) {
            if (validblocks[seat][d])
                break;
            d--;
        }
        while (a < this.blocknum) {
            if (validblocks[seat][a])
                break;
            a++;
        }

        lsa.writeLock().lock();
        for (int p = d + 1; p < t.arrival - 1; p++)
            for (int q = t.departure - 1; q < a; q++)
                lsa.seatsleft[p][q] -= 1;
        lsa.writeLock().unlock();
        return true;
    }

    //返还座位
    public boolean ReturnSeat(Ticket ticket, leftSeatsArray lsa) {
        //检查车票是否合法
        boolean islegal = true;

        for (int i = ticket.departure - 1; i < ticket.arrival - 1; i++) {
            if (currenttids[ticket.seat - 1][i] != ticket.tid)
                return false;
            islegal &= validblocks[ticket.seat - 1][i];
            if (!islegal)
                return false;
        }

        for (int j = ticket.departure - 1; j < ticket.arrival - 1; j++)
            validblocks[ticket.seat - 1][j] = false;

        int d = ticket.departure - 2, a = ticket.arrival - 1;
        while (d > -1) {
            if (validblocks[ticket.seat - 1][d])
                break;
            d--;
        }
        while (a < this.blocknum) {
            if (validblocks[ticket.seat - 1][a])
                break;
            a++;
        }

        lsa.writeLock().lock();
        for (int p = d + 1; p < ticket.arrival - 1; p++)
            for (int q = ticket.departure - 1; q < a; q++)
                lsa.seatsleft[p][q] += 1;
        lsa.writeLock().unlock();
        return true;
    }
}