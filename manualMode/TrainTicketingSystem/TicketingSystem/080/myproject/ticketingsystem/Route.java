package ticketingsystem;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;


public class Route {
    private ArrayList<Coach> CoachList; //车厢列表
    private final int RouteId; //车次号
    private final int CoachNum; //车厢数
    private AtomicLong TicketId; //车票id
    private Queue<Long> SoldTicketQueue; //保存已售出的车票信息

    public Route(final int routeId, final int coachNum, final int seatNum) {
        this.RouteId = routeId;
        this.CoachNum = coachNum;
        this.CoachList = new ArrayList<>(coachNum);
        this.TicketId = new AtomicLong(0);
        this.SoldTicketQueue = new ConcurrentLinkedQueue<>();

        for (int i = 1; i <= coachNum; i++) {
            this.CoachList.add(new Coach(i, seatNum));
        }
    }

    public Ticket buyTicket(final String passenger, final int departure, final int arrival) {

        int randomCoach = ThreadLocalRandom.current().nextInt(this.CoachNum);
        for (int i = 0; i < this.CoachNum; i++) {
            Ticket ticket = this.CoachList.get(randomCoach).buyTicket(departure, arrival);
            if (ticket != null) {
                ticket.tid = this.RouteId*10000000 + this.TicketId.getAndIncrement();
                ticket.passenger = passenger;
                ticket.route = this.RouteId;
                ticket.departure = departure;
                ticket.arrival = arrival;

                //每张车票使用一个long型数据来保存基本信息
                long ticketInformation = 0;
                ticketInformation = ticketInformation | (ticket.tid << 32);
                ticketInformation = ticketInformation | (ticket.coach << 24);
                ticketInformation = ticketInformation | (ticket.seat << 12);
                ticketInformation = ticketInformation | (ticket.departure << 6);
                ticketInformation = ticketInformation | (ticket.arrival);
                this.SoldTicketQueue.add(ticketInformation);
                return ticket;
            }
            randomCoach = (randomCoach+1) % this.CoachNum;
        }
        return null;
    }

    public int inquiry(final int departure, final int arrival) {
        int result = 0;
        for (int i = 0; i < this.CoachNum; i++)
            result += this.CoachList.get(i).inquiry(departure, arrival);
        return result;
    }

    public boolean refundTicket(final Ticket ticket) {
        long ticketInformation = 0;
        ticketInformation = ticketInformation | (ticket.tid << 32);
        ticketInformation = ticketInformation | (ticket.coach << 24);
        ticketInformation = ticketInformation | (ticket.seat << 12);
        ticketInformation = ticketInformation | (ticket.departure << 6);
        ticketInformation = ticketInformation | (ticket.arrival);
        if (!this.SoldTicketQueue.contains(ticketInformation))
            return false;
        else {
            this.SoldTicketQueue.remove(ticketInformation);
            return this.CoachList.get(ticket.coach-1).refundTicket(ticket.seat, ticket.departure, ticket.arrival);
        }
    }

}

