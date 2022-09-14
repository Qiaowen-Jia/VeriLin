package ticketingsystem;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class Route {
    private final int routeId;
    private final int coachNum;
    private ArrayList<Coach> coachList;
    private AtomicLong ticketId;
    private Queue<Long> soldTicket;

    public Route(final int routeId, final int coachNum, final int seatNum) {
        this.routeId = routeId;
        this.coachNum = coachNum;
        this.coachList = new ArrayList<Coach>(coachNum);
        this.ticketId = new AtomicLong(0);
        this.soldTicket = new ConcurrentLinkedQueue<Long>();

        for (int coachId = 1; coachId <= coachNum; coachId++)
            this.coachList.add(new Coach(coachId, seatNum));
    }

    public Ticket trySellTicket(final String passenger, final int departure, final int arrival) {
        //遍历所有coach
        int randCoach = ThreadLocalRandom.current().nextInt(this.coachNum);
        for (int i = 0; i < this.coachNum; i++) {
            Ticket ticket = this.coachList.get(randCoach).trySellTicket(departure, arrival);
            if (ticket != null) {
                ticket.tid = this.routeId*10000000 + this.ticketId.getAndIncrement();
                ticket.passenger = passenger;
                ticket.route = this.routeId;
                ticket.departure = departure;
                ticket.arrival = arrival;

                //计算每张车票的hash值
                long ticketHashCode = 0;
                ticketHashCode |= ticket.tid << 32;
                ticketHashCode |= ticket.coach << 24;
                ticketHashCode |= ticket.seat << 12;
                ticketHashCode |= ticket.departure << 6;
                ticketHashCode |= ticket.arrival;
                this.soldTicket.add(new Long(ticketHashCode));
                return ticket;

            }
            randCoach = (randCoach+1) % this.coachNum;
        }

        return null;
    }

    public int inquiryTicket(final int departure, final int arrival) {
        int ticketSum = 0;
        for (int i = 0; i < this.coachNum; i++)
            ticketSum += this.coachList.get(i).inquiryTicket(departure, arrival);
        return ticketSum;
    }

    public boolean tryRefundTicket(final Ticket ticket) {
        long ticHashCode = 0;
        ticHashCode |= ticket.tid << 32;
        ticHashCode |= ticket.coach << 24;
        ticHashCode |= ticket.seat << 12;
        ticHashCode |= ticket.departure << 6;
        ticHashCode |= ticket.arrival;
        if (!this.soldTicket.contains(ticHashCode))
            return false;
        else {
            this.soldTicket.remove(ticHashCode);
            return this.coachList.get(ticket.coach-1).tryRefundTicket(ticket.seat, ticket.departure, ticket.arrival);
        }
    }

}
