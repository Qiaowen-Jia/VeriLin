package ticketingsystem;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;


class TicketUtil {

    /**
     * 主要判断票的唯一性
     */
    static long getTicketHashCode(Ticket ticket) {
        long ticHashCode = 0;
        ticHashCode |= ticket.tid << 32;
        ticHashCode |= ticket.coach << 24;
        ticHashCode |= ticket.seat << 12;
        ticHashCode |= ticket.departure << 6;
        ticHashCode |= ticket.arrival;
        return ticHashCode;
    }

    /**
     * 复用代码
     */
    static long checkSeal(final int departure, final int arrival) {
        int temp = 0;
        for (int i = departure - 1; i < arrival - 1; i++) {
            long pow = 1;
            pow = pow << i;
            temp |= pow;
        }
        return temp;
    }
}

/**
 * 火车座位类
 */
class TrainSeat {
    /**
     * 座位id
     */
    private final int seatId;
    /**
     * 是否被卖的标识
     */
    private AtomicLong availableSeat;

    TrainSeat(final int seatId) {
        this.seatId = seatId;
        this.availableSeat = new AtomicLong(0);
    }

    /**
     * 卖票（就是卖座位）
     */
    int sealTicket(final int departure, final int arrival) {

        long temp = TicketUtil.checkSeal(departure, arrival);

        long oldAvailSeat;
        long newAvailSeat;
        do {
            oldAvailSeat = this.availableSeat.get();
            long result = temp & oldAvailSeat;
            if (result != 0) {
                return -1;
            } else {
                newAvailSeat = temp | oldAvailSeat;
            }
        } while (!this.availableSeat.compareAndSet(oldAvailSeat, newAvailSeat));

        return this.seatId;

    }

    /**
     * 查票，对于座位类来说就是验证这个座位是否被卖了
     */
    int inquiryTicket(final int departure, final int arrival) {
        long oldAvailSeat = this.availableSeat.get();
        long temp = TicketUtil.checkSeal(departure, arrival);
        long result = temp & oldAvailSeat;

        return (result == 0) ? 1 : 0;

    }

    /**
     * 退票
     */
    boolean refundTicket(final int departure, final int arrival) {
        long oldAvailSeat;
        long newAvailSeat;
        long temp = TicketUtil.checkSeal(departure, arrival);
        temp = ~temp;
        do {
            oldAvailSeat = this.availableSeat.get();
            newAvailSeat = temp & oldAvailSeat;
        } while (!this.availableSeat.compareAndSet(oldAvailSeat, newAvailSeat));

        return true;
    }

}

/**
 * 火车车厢类（车厢有N个座位）
 */
class TrainCoach {

    private final int coachId;
    private final int seatNum;
    private ArrayList<TrainSeat> seatList;

    /**
     * 初始化构造器
     */
    TrainCoach(final int coachId, final int seatNum) {
        this.coachId = coachId;
        this.seatNum = seatNum;
        seatList = new ArrayList<>(seatNum);

        for (int seatId = 1; seatId <= seatNum; seatId++) {
            this.seatList.add(new TrainSeat(seatId));
        }
    }

    /**
     * 卖票
     */
    Ticket sealTicket(final int departure, final int arrival) {
        Ticket ticket = new Ticket();
        int randSeat = ThreadLocalRandom.current().nextInt(this.seatNum);
        for (int i = 0; i < this.seatNum; i++) {
            int resultSeatId = this.seatList.get(randSeat).sealTicket(departure, arrival);
            if (resultSeatId != -1) {
                ticket.coach = this.coachId;
                ticket.seat = resultSeatId;
                return ticket;
            }
            randSeat = (randSeat + 1) % this.seatNum;
        }
        return null;

    }

    /**
     * 查票就是把座位都验证是否被卖了，然后总计起来
     */
    int inquiryTicket(final int departure, final int arrival) {
        return IntStream.range(0, this.seatNum).map(i -> this.seatList.get(i).inquiryTicket(departure, arrival)).sum();
    }

    /**
     * 退票
     */
    boolean refundTicket(final int seatId, final int departure, final int arrival) {
        return this.seatList.get(seatId - 1).refundTicket(departure, arrival);
    }

}

/**
 * 车次类
 */
class TrainRoute {
    private final int routeId;
    private final int coachNum;
    private ArrayList<TrainCoach> coachList;
    private AtomicLong ticketId;
    /**
     * 存车票的hashcode,也是就唯一验证车票
     */
    private Queue<Long> queueSoldTicket;

    /**
     * 初始化构造器
     */
    TrainRoute(final int routeId, final int coachNum, final int seatNum) {
        this.routeId = routeId;
        this.coachNum = coachNum;
        this.coachList = new ArrayList<>(coachNum);
        this.ticketId = new AtomicLong(0);
        this.queueSoldTicket = new ConcurrentLinkedQueue<>();

        for (int coachId = 1; coachId <= coachNum; coachId++) {
            this.coachList.add(new TrainCoach(coachId, seatNum));
        }
    }

    /**
     * 卖票
     */
    Ticket sealTicket(final String passenger, final int departure, final int arrival) {
        int randCoach = ThreadLocalRandom.current().nextInt(this.coachNum);
        for (int i = 0; i < this.coachNum; i++) {
            Ticket ticket = this.coachList.get(randCoach).sealTicket(departure, arrival);
            if (ticket != null) {
                ticket.tid = this.routeId * 10000000 + this.ticketId.getAndIncrement();
                ticket.passenger = passenger;
                ticket.route = this.routeId;
                ticket.departure = departure;
                ticket.arrival = arrival;

                this.queueSoldTicket.add(TicketUtil.getTicketHashCode(ticket));
                return ticket;
            }
            randCoach = (randCoach + 1) % this.coachNum;
        }

        return null;
    }

    /**
     * 查票（每个车厢的每个座位的票的总和）
     */
    int inquiryTicket(final int departure, final int arrival) {
        return IntStream.range(0, this.coachNum).map(i -> this.coachList.get(i).inquiryTicket(departure, arrival)).sum();
    }

    /**
     * 退票
     */
    boolean refundTicket(final Ticket ticket) {

        long ticketHashCode = TicketUtil.getTicketHashCode(ticket);
        if (!this.queueSoldTicket.contains(ticketHashCode)) {
            return false;
        } else {
            this.queueSoldTicket.remove(ticketHashCode);
            return this.coachList.get(ticket.coach - 1).refundTicket(ticket.seat, ticket.departure, ticket.arrival);
        }
    }

}

public class TicketingDS implements TicketingSystem {

    private final int routeNum;
    private final int stationNum;
    private ArrayList<TrainRoute> routeList;

    TicketingDS(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
        this.routeNum = routeNum;
        this.stationNum = stationNum;

        this.routeList = new ArrayList<>(routeNum);
        for (int routeId = 1; routeId <= routeNum; routeId++) {
            this.routeList.add(new TrainRoute(routeId, coachNum, seatNum));
        }
    }

    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        if (route <= 0 || route > this.routeNum || arrival > this.stationNum || departure >= arrival) {
            return null;
        }
        return this.routeList.get(route - 1).sealTicket(passenger, departure, arrival);
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        if (route <= 0 || route > this.routeNum || arrival > this.stationNum || departure >= arrival) {
            return -1;
        }
        return this.routeList.get(route - 1).inquiryTicket(departure, arrival);
    }

    @Override
    public boolean refundTicket(Ticket ticket) {
        final int routeId = ticket.route;
        if (routeId <= 0 || routeId > this.routeNum) {
            return false;
        }
        return this.routeList.get(routeId - 1).refundTicket(ticket);
    }

}
