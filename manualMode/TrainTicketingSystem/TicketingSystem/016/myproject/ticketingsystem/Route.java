package ticketingsystem;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 车次
 * @author ZH
 * @date 2018-12-06-17:51
 */
public class Route {
    /**
     * seatstatus[k][i][j] 座位k 在i到j区间被占用情况，0代表可用，n代表有n章票导致了这个区间不可买
     * remainTicketNum[i][j] i到j区间剩余的票数
     * totalSeatNum 总座位数 = 车厢数 * 车厢座位数
     * seatnum 车厢座位数 用于重新定位所属车厢
     * stationnum 车站数
     * buyLock 买票和退票时加的锁
     * random 分配座位时用
     */
    private int[][][] seatstatus;
    private AtomicInteger[][] remainTicketNum;
    private final int totalSeatNum;
    private final int routeId;
    private final int seatnum;
    private final int stationnum;

    private Lock[] coachLocks;

    private final ThreadLocalRandom random;

    public Route(int routeId, int coachnum, int seatnum, int stationnum) {
        this.routeId = routeId;
        this.seatnum = seatnum;
        this.stationnum = stationnum;
        totalSeatNum = coachnum * seatnum;
        seatstatus = new int[totalSeatNum + 1][stationnum][stationnum + 1];
        coachLocks = new ReentrantLock[coachnum + 1];
        for (int i = 0; i < coachnum; i ++) {
            coachLocks[i] = new ReentrantLock();
        }

        remainTicketNum = new AtomicInteger[stationnum][stationnum + 1];
        for (int i = 1; i < stationnum; i ++) {
            for (int j = i + 1; j <= stationnum; j ++) {
                remainTicketNum[i][j] = new AtomicInteger(totalSeatNum);
            }
        }

        random = ThreadLocalRandom.current();
    }

    public Ticket buyTicket(int departure, int arrival) {
        if (remainTicketNum[departure][arrival].get() == 0) {
            return null;
        }
        int seat = random.nextInt(totalSeatNum) + 1;
        int coach = (seat - 1) / seatnum;
        coachLocks[coach].lock();
        while (true) {
            if (remainTicketNum[departure][arrival].get() == 0) {
                coachLocks[coach].unlock();
                return null;
            }
            boolean findSeat = false;
            if (seatstatus[seat][departure][arrival] == 0) {
                for (int i = 1; i < arrival; i ++) {
                    for (int j = Math.max(i + 1, departure + 1); j <= stationnum; j ++) {
                        if (seatstatus[seat][i][j] == 0) {
                            remainTicketNum[i][j].decrementAndGet();
                        }
                        seatstatus[seat][i][j] ++;
                    }
                }
                findSeat = true;
            }
            if (findSeat) {
                coachLocks[coach].unlock();
                break;
            }
            seat ++;
            if (seat > totalSeatNum) {
                seat = 1;
            }
            int nextcoach = (seat - 1) / seatnum;
            if (nextcoach != coach) {
                coachLocks[coach].unlock();
                coachLocks[nextcoach].lock();
                coach = nextcoach;
            }
        }

        seat = seat - coach * seatnum;

        Ticket ticket = new Ticket();
        ticket.route = routeId;
        ticket.coach = coach + 1;
        ticket.seat = seat;
        ticket.departure = departure;
        ticket.arrival = arrival;
        return ticket;
    }

    public boolean refundTicket(Ticket ticket) {
        int seat = ticket.seat + (ticket.coach - 1) * seatnum;
        int coach = (seat - 1) / seatnum;
        coachLocks[coach].lock();
        for (int i = 1; i < ticket.arrival; i ++) {
            for (int j = Math.max(i + 1, ticket.departure + 1); j <= stationnum; j ++) {
                seatstatus[seat][i][j] --;
                if (seatstatus[seat][i][j] == 0) {
                    remainTicketNum[i][j].incrementAndGet();
                }

            }
        }
        coachLocks[coach].unlock();
        return true;
    }

    public int inquiry(int departure, int arrival) {
        return remainTicketNum[departure][arrival].get();
    }
}