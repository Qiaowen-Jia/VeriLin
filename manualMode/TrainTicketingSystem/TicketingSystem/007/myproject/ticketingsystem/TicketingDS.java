package ticketingsystem;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;



public class TicketingDS implements TicketingSystem {

    private int routeNum;
    private int coachNum;
    private int seatNum;
    private int stationNum;
    private int threadNum;
    public static AtomicLong ticketId;

    public Route[] routeArray;
    public static ConcurrentHashMap<Long, Boolean> allTicket;
    //private Node head;

    public TicketingDS(){
        routeNum = 5;
        coachNum = 8;
        seatNum = 100;
        stationNum = 10;
        threadNum = 16;
        ticketId = new AtomicLong(1);
        routeArray = new Route[routeNum];
        for (int i = 0; i < routeNum; i++){
            routeArray[i] = new Route(i, coachNum, seatNum, stationNum);
        }
        allTicket = new ConcurrentHashMap<>(this.routeNum * 1000);
    }
    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum){
        this.routeNum = routenum;
        this.coachNum = coachnum;
        this.seatNum = seatnum;
        this.stationNum = stationnum;
        this.threadNum = threadnum;
        ticketId = new AtomicLong(1);
        this.routeArray = new Route[routeNum];
        for (int i = 0; i < routeNum; i++){
            this.routeArray[i] = new Route(i, coachNum, seatNum, stationNum);
        }
        allTicket = new ConcurrentHashMap<>(this.routeNum * 1000);
        //System.out.println("Finish Create Ticket DataBase");
    }
	//ToDo
    public Ticket buyTicket(String passenger, int route, int departure, int arrival){
        if (checkTicket(route, departure, arrival) == false){//参数不符合规范
            return null;
        }
        Ticket ticket;
        int[] couple = routeArray[route - 1].buyTicket(departure, arrival);
        if (couple != null){
            ticket = new Ticket();
            ticket.tid = ticketId.getAndIncrement();
            ticket.passenger = passenger;
            ticket.route = route;
            ticket.coach = couple[0];
            ticket.seat = couple[1];
            ticket.departure = departure;
            ticket.arrival = arrival;
            allTicket.put(ticket.tid, true);
            return ticket;
        }    
        //System.out.println("Finish buyTicket");
        return null;
    }
    public int inquiry(int route, int departure, int arrival){
        if (checkTicket(route, departure, arrival) == false){
            return -1;//参数不符合规范
        }
        int leftSeat = -1;
        leftSeat = routeArray[route - 1].inquiryTicket(departure, arrival);
        return leftSeat;
    }
    public boolean refundTicket(Ticket ticket){
        if (checkTicket(ticket.route, ticket.departure, ticket.arrival) == false){
            return false;
        }
        boolean isRefund = false;
        if (allTicket.remove(ticket.tid) != null){
            //System.out.printf("RefundTicket %d, %d, %d\n", ticket.tid, ticket.coach, ticket.seat);
            isRefund = routeArray[ticket.route - 1].refundTicket(ticket);
            return isRefund;
        }
        return false;
    }

    private boolean checkTicket(int route, int departure, int arrival){
        if (route < 1 || route > routeNum || departure < 1 || departure >= stationNum || arrival <= 1 || arrival > stationNum || arrival <= departure){
            return false;
        }
        return true;
    }
}
