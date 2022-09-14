package ticketingsystem;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import ticketingsystem.*;

class Ticket{
        long tid;
        String passenger;
        int route;
        int coach;
        int seat;
        int departure;
        int arrival;
}


//SeatNode 维护一个 seatId 和一个 AtomicLong 型 64 位的 availableSeat，
//availableSeat 的每一位表示座位对应的每一站， 0 表示未售出， 1 表示售出。
//购票查询退票时均采用从 route-\>coach-\>seat 的方式调用方法，
//在 seatNode 操作时，用原语 compareAndSet 构造非阻塞式的自旋锁来保证并发操作的原子性。

class SeatNode {
    private final int seatId;
    private AtomicLong availableSeat;           //对长整形进行原子操作

    public SeatNode(final int seatId) {
        this.seatId = seatId;
        this.availableSeat = new AtomicLong(0);
    }

    public int trySealTic(final int departure, final int arrival) {         //Try卖票
        long oldAvailableSeat = 0;
        long newAvailableSeat = 0;
        long ticketExpress = 0;

        for (int i = departure-1; i < arrival-1; i++) {
            long temp = 1;
            temp = temp << i;
            ticketExpress |= temp;
        }

        do {
            oldAvailableSeat = this.availableSeat.get();//读
            long result = ticketExpress & oldAvailableSeat;
            if (result != 0) {
                return -1;
            }
            else {
                newAvailableSeat = ticketExpress | oldAvailableSeat;
            }
        } while (!this.availableSeat.compareAndSet(oldAvailableSeat, newAvailableSeat));
        return this.seatId;

    }

    public int inquiryTic(final int departure, final int arrival) {
        long oldAvailableSeat = this.availableSeat.get();
        long ticketExpress = 0;
        long temp;

        for (int i = departure-1; i < arrival-1; i++) {
            temp = 1;
            temp = temp << i;
            ticketExpress |= temp;
        }

        long result = ticketExpress & oldAvailableSeat;

        return (result == 0) ? 1 : 0;

    }

    public boolean tryRefundTic(final int departure, final int arrival) {
        long oldAvailableSeat = 0;
        long newAvailableSeat = 0;
        long temp = 0;

        for (int i = departure-1; i < arrival-1; i++) {
            long pow = 1;
            pow = pow << i;
            temp |= pow;
        }

        temp = ~temp;
        do {
            oldAvailableSeat = this.availableSeat.get();
            newAvailableSeat = temp & oldAvailableSeat;
        } while(!this.availableSeat.compareAndSet(oldAvailableSeat, newAvailableSeat));
        return true;
    }

}

//## CoachNode 类
//CoachNode 维护一个 seatList， seatNum 和 coachId，具体实现上述三个方法。
class CoachNode {
    private final int coachId;
    private final int seatNum;
    private ArrayList<SeatNode> seatList;

    public CoachNode(final int coachId, final int seatNum) {
        this.coachId = coachId;
        this.seatNum = seatNum;
        seatList = new ArrayList<SeatNode>(seatNum);

        for (int seatId = 1; seatId <= seatNum; seatId++)
            this.seatList.add(new SeatNode(seatId));
    }

    public Ticket trySealTic(final int departure, final int arrival) {
        Ticket ticket = new Ticket();
        //遍历所有seat
        int randSeat = ThreadLocalRandom.current().nextInt(this.seatNum);
        for (int i = 0; i < this.seatNum; i++) {
            int resultSeatId = this.seatList.get(randSeat).trySealTic(departure, arrival);
            if (resultSeatId != -1) {
                ticket.coach = this.coachId;
                ticket.seat = resultSeatId;
                return ticket;
            }
            randSeat = (randSeat+1) % this.seatNum;
        }
        return null;

    }

    public int inquiryTic(final int departure, final int arrival) {
        int ticSum = 0;
        for (int i = 0; i < this.seatNum; i++)
            ticSum += this.seatList.get(i).inquiryTic(departure, arrival);
        return ticSum;
    }

    public boolean tryRefundTic(final int seatId, final int departure, final int arrival) {
        return this.seatList.get(seatId-1).tryRefundTic(departure, arrival);
    }

}

//## RouteNode车次类
//RouteNode 包括 coachList， coachNum 和 routeId成员变量，具体实现上述三个方法。

class RouteNode {
    private final int routeId;      //车次号
    private final int coachNum;     //车厢总数量
    private ArrayList<CoachNode> coachList;     //车厢链表
    private AtomicLong ticketId;        //车票ID：原子长整形变量
    private Queue<Ticket> queue_SoldTicket;   //已卖出票队列

    public RouteNode(final int routeId, final int coachNum, final int seatNum) {
        this.routeId = routeId;
        this.coachNum = coachNum;
        this.coachList = new ArrayList<CoachNode>(coachNum);
        this.ticketId = new AtomicLong(0);
        this.queue_SoldTicket = new ConcurrentLinkedQueue<Ticket>();

        for (int coachId = 1; coachId <= coachNum; coachId++)
            this.coachList.add(new CoachNode(coachId, seatNum));
    }

    public Ticket trySealTic(final String passenger, final int departure, final int arrival) {
        for (int i = 0; i < this.coachNum; i++) {
            Ticket ticket = this.coachList.get(i).trySealTic(departure, arrival);
            if (ticket != null) {
                ticket.tid = this.routeId*10000000 + this.ticketId.getAndIncrement();
                ticket.passenger = passenger;
                ticket.route = this.routeId;
                ticket.departure = departure;
                ticket.arrival = arrival;
                this.queue_SoldTicket.add(ticket);
                return ticket;
            }
        }
        return null;
    }

    public int inquiryTic(final int departure, final int arrival) {
        int ticSum = 0;
        for (int i = 0; i < this.coachNum; i++)
            ticSum += this.coachList.get(i).inquiryTic(departure, arrival);
        return ticSum;
    }

    public boolean tryRefundTic(final Ticket ticket) {
        if (!this.queue_SoldTicket.contains(ticket))
            return false;
        else {
            Boolean RefundFlag = this.coachList.get(ticket.coach-1).tryRefundTic(ticket.seat, ticket.departure, ticket.arrival);
            if(RefundFlag == true)
                this.queue_SoldTicket.remove(ticket);
            return RefundFlag;
        }
    }
}

//TicketingDS 类的私有属性是一个 routeList 和 routeNum，包含三个方法分别是
//public Ticket buyTicket(String passenger, int route, int departure, int arrival)
//public int inquiry(int route, int departure, int arrival)
//public boolean refundTicket(Ticket ticket)
//实现的是 TicketingSystem 接口中的方法。分别是购票，查询当前余票和退票。

//public class TicketingDS implements TicketingSystem {
public class TicketingDS {
    private final int routeNum;     //车次总数
    private final int stationNum;   //车站数量
    private ArrayList<RouteNode> routeList;         //车次链表

    public TicketingDS(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
        this.routeNum = routeNum;
        this.stationNum = stationNum;
        this.routeList = new ArrayList<RouteNode>(routeNum);

        for (int routeId = 1; routeId <= routeNum; routeId++)           //routeId从1开始
            this.routeList.add(new RouteNode(routeId, coachNum, seatNum));
    }

    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        if (route <=0 || route > this.routeNum || arrival > this.stationNum || departure >= arrival)
            return null;
        return this.routeList.get(route-1).trySealTic(passenger, departure, arrival);
    }

    public int inquiry(int route, int departure, int arrival) {
        if (route <=0 || route > this.routeNum || arrival > this.stationNum || departure >= arrival)
            return -1;
        return this.routeList.get(route-1).inquiryTic(departure, arrival);
    }

    public boolean refundTicket(Ticket ticket) {
        final int routeId = ticket.route;
        if (ticket == null || routeId <=0 || routeId > this.routeNum)
            return false;
        return this.routeList.get(routeId-1).tryRefundTic(ticket);
    }
}
