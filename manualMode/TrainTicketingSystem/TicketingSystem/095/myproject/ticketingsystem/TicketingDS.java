package ticketingsystem;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class TicketingDS implements TicketingSystem {
    private int threadNum;
    private Train[] trains;
    private int coachNum;
    private int seatNum;
    private AtomicLong buyTicketID;

    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
        this.threadNum = threadnum;
        this.coachNum = coachnum;
        this.seatNum = seatnum;
        trains = new Train[routenum];
        buyTicketID = new AtomicLong(0);
        for (int i = 0; i < routenum; i++) {
            trains[i] = new Train(coachnum, seatnum, stationnum);
        }
    }

    @Override

    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {

        Ticket ticket = new Ticket();
        Train t = trains[route - 1];
        int globalSeat = t.getSeat(departure, arrival);
        if (globalSeat == -1)
            return null;
        ticket.tid = buyTicketID.getAndAdd(1);
        ticket.passenger = passenger;
        ticket.route = route;
        ticket.coach = ((globalSeat - 1) / seatNum) + 1;
        ticket.seat = ((globalSeat - 1) % seatNum) + 1;
        ticket.departure = departure;
        ticket.arrival = arrival;
        t.addSoldTicket(ticket);
        return ticket;

    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        int remainTicket = trains[route - 1].remainSeat(departure, arrival);
        return remainTicket;
    }

    @Override
    public boolean refundTicket(Ticket ticket) {

        Train currTrain = trains[ticket.route - 1];
        // 判断要删除的票是否存在
        if (!currTrain.containAndRemove(ticket)) {
            return false;
        }
        // 恢复座位
        int globalSeat = (ticket.coach - 1) * seatNum + ticket.seat;
        return currTrain.setSeat(globalSeat, ticket.departure, ticket.arrival);
    }
}