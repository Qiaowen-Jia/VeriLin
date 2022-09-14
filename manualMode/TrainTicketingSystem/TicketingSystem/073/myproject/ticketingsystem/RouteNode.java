package ticketingsystem;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author: fnbory
 * @Date: 4/1/2020 下午 8:53
 */
public class RouteNode {
    private final int routeId;
    private final int coachNum;
    private ArrayList<CoachNode> coachList;
    private AtomicLong ticketId;
    private Queue<Long> queue_SoldTicket;

    public RouteNode(final int routeId, final int coachNum, final int seatNum) {
        this.routeId = routeId;
        this.coachNum = coachNum;
        this.coachList = new ArrayList<CoachNode>(coachNum);
        this.ticketId = new AtomicLong(0);
        this.queue_SoldTicket = new ConcurrentLinkedQueue<Long>();

        for (int coachId = 1; coachId <= coachNum; coachId++)
            this.coachList.add(new CoachNode(coachId, seatNum));
    }

    public Ticket trySealTic(final String passenger, final int departure, final int arrival) {
        //遍历所有coach，第一次是随机的。如果不随机挨个儿遍历的话如果前边的都卖了，则加大时间复杂度
        int randCoach = ThreadLocalRandom.current().nextInt(this.coachNum);
        for (int i = 0; i < this.coachNum; i++) {
            // 第一次是随机的
            Ticket ticket = this.coachList.get(randCoach).trySealTic(departure, arrival);
            // 如果成功买到票，将对应座位加入到已售队列
            if (ticket != null) {
                ticket.tid = this.routeId*10000000 + this.ticketId.getAndIncrement();
                ticket.passenger = passenger;
                ticket.route = this.routeId;
                ticket.departure = departure;
                ticket.arrival = arrival;

                //每张车票hashCode
                long tic_hashCode = 0;
                tic_hashCode |= ticket.tid << 32;
                tic_hashCode |= ticket.coach << 24;
                tic_hashCode |= ticket.seat << 12;
                tic_hashCode |= ticket.departure << 6;
                tic_hashCode |= ticket.arrival;
                this.queue_SoldTicket.add(new Long(tic_hashCode));
                return ticket;

            }
            // 第一次随即到的车厢没有余票的话遍历车厢
            randCoach = (randCoach+1) % this.coachNum;
        }
        // 遍历所有车厢都没有余票返回null
        return null;
    }
    //  在整个车厢中查，最后的结果累加起来返回代表可买的票数
    public int inquiryTic(final int departure, final int arrival) {
        int ticSum = 0;
        for (int i = 0; i < this.coachNum; i++)
            ticSum += this.coachList.get(i).inquiryTic(departure, arrival);
        return ticSum;
    }

    public boolean tryRefundTic(final Ticket ticket) {
        long tic_hashCode = 0;
        tic_hashCode |= ticket.tid << 32;
        tic_hashCode |= ticket.coach << 24;
        tic_hashCode |= ticket.seat << 12;
        tic_hashCode |= ticket.departure << 6;
        tic_hashCode |= ticket.arrival;
        // 如果已售列表中没有该票，直接返回false
        if (!this.queue_SoldTicket.contains(tic_hashCode))
            return false;
        else {
            // 此时可以直接remove退票
            this.queue_SoldTicket.remove(tic_hashCode);
            return this.coachList.get(ticket.coach-1).tryRefundTic(ticket.seat, ticket.departure, ticket.arrival);
        }
    }

}
