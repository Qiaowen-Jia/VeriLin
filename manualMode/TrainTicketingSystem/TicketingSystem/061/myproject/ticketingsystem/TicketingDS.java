package ticketingsystem;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TicketingDS implements TicketingSystem {
    private int routeNum;
    private int coachNum;
    private int seatNum;
    private int stationNum;
    private int threadNum;
    private Train[] trains;
    private AtomicLong ticketID = new AtomicLong(1);
    private ConcurrentHashMap<Long, Ticket> soldTicket;
	//ToDo

    /**
     *
     * @param routeNum 车次总数
     * @param coachNum 列车车厢总数
     * @param seatNum 每节车厢的座位数
     * @param stationNum 每个车次经停站的数量（含始发站和终点站）
     * @param threadNum 并发线程数
     */
    public TicketingDS(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
        this.routeNum = routeNum;
        this.coachNum = coachNum;
        this.seatNum = seatNum;
        this.stationNum = stationNum;
        this.threadNum = threadNum;

        //for循环，初始化车次数组
        trains = new Train[routeNum];
        for(int i = 0; i < routeNum; i++)
            trains[i] = new Train(coachNum, seatNum, stationNum);
        soldTicket = new ConcurrentHashMap<Long, Ticket>();
    }

    /*Train中实现的departure和arrival都是从下标0开始，因此这里需要-1*/
    public Ticket buyTicket(String passenger, int route, int departure, int arrival){
        if(isIllegal(passenger, route, departure, arrival))
            return null;
        int seat = trains[route - 1].lockSeat(departure - 1, arrival - 1);
        if(seat < 0)
            return null;
        Ticket ticket = new Ticket();
        ticket.tid = ticketID.getAndIncrement();
        ticket.passenger = passenger;
        ticket.route = route;
        ticket.coach = seat / seatNum + 1;
        ticket.seat = seat % seatNum + 1;
        ticket.departure = departure;
        ticket.arrival = arrival;
        soldTicket.put(ticket.tid, ticket);
        return ticket;
    }

    public int inquiry(int route, int departure, int arrival) {
        if(isIllegal("passenger", route, departure, arrival))
            return 0;
        return trains[route - 1].querySeat(departure - 1, arrival - 1);
    }

    public boolean refundTicket(Ticket ticket){
        if(ticket == null)
            return false;
        Ticket ticket_map = soldTicket.get(ticket.tid);
        if(!isEqual(ticket, ticket_map))
            return false;
        int num = (ticket.coach - 1) * seatNum + (ticket.seat - 1);
        if(trains[ticket.route - 1].unlockSeat(num, ticket.departure - 1, ticket.arrival - 1))
            return soldTicket.remove(ticket.tid, ticket);
        return false;
    }

    private boolean isIllegal(String passenger, int route, int departure, int arrival) {
        if (passenger == null || passenger.equals("") || route <= 0 || route > routeNum || departure <= 0
                || departure > stationNum || arrival <= 0 || arrival > stationNum || departure >= arrival)
            return true;
        return false;
    }

    private boolean isEqual(Ticket a, Ticket b){
        if(a == b) return true;
        if(a == null || b == null) return false;
        return ((a.tid == b.tid) &&
                (a.passenger.equals(b.passenger)) &&
                (a.route == b.route) &&
                (a.coach == b.coach) &&
                (a.seat == b.seat) &&
                (a.departure == b.departure) &&
                (a.arrival == b.arrival));
    }
}

