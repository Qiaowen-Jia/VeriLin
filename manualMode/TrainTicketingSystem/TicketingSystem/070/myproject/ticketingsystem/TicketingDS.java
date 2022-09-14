package ticketingsystem;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class TicketingDS implements TicketingSystem {
	//ToDo
    private int routenum, coachnum, seatnum, stationnum,threadnum;
    private long[][][][] soldTicketTid;
    private String[][][][] soldTicketPassenger;
    private boolean[][][][] seats;
    private AtomicLong tid;
    private ReentrantReadWriteLock[] locks;

    TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum){
        this.routenum = routenum;
        this.coachnum = coachnum;
        this.seatnum = seatnum;
        this.stationnum = stationnum;
        this.threadnum = threadnum;
        seats = new boolean[routenum][coachnum][seatnum][stationnum];
        soldTicketTid = new long[routenum][coachnum][seatnum][stationnum];
        soldTicketPassenger = new String[routenum][coachnum][seatnum][stationnum];
        tid = new AtomicLong(99);
        locks = new ReentrantReadWriteLock[routenum];
        for(int i = 0; i < locks.length; i++){
            locks[i] = new ReentrantReadWriteLock();
        }
    }

    private boolean isLegal(int route, int departure, int arrival){
        return !(route < 1 || route > routenum || departure < 1 || departure > stationnum ||
                arrival < 1 || arrival > stationnum || (arrival - departure) < 0);
    }
    private boolean contains(Ticket ticket){
        if(ticket.tid < 99) return false;
        for(int i = ticket.departure-1; i < ticket.arrival-1; i++){
            if((ticket.tid!=soldTicketTid[ticket.route-1][ticket.coach-1][ticket.seat-1][i]) || (!ticket.passenger.contentEquals(soldTicketPassenger[ticket.route-1][ticket.coach-1][ticket.seat-1][i]))) return false;
        }
        return true;
    }
    @Override
    public int inquiry(int route, int departure, int arrival){
        if(!isLegal(route, departure, arrival)) return 0;
        locks[route-1].readLock().lock();
        try{
            int ticketnum = 0;
            for(boolean[][] coach : seats[route-1]){
                for(boolean[] seat : coach){
                    boolean flag = true;
                    for(int i = departure-1; i < arrival-1; i++){
                        if(seat[i]){
                            flag = false;
                            break;
                        }
                    }
                    if(flag) ticketnum++;
                }
            }
            return ticketnum;
        }
        finally {
            locks[route-1].readLock().unlock();
        }
    }

    @Override
    public boolean refundTicket(Ticket ticket) {
        if(!isLegal(ticket.route, ticket.departure, ticket.arrival)) return false;
        locks[ticket.route-1].writeLock().lock();
        try {
            if (contains(ticket)) {
                for (int i = ticket.departure - 1; i < ticket.arrival - 1; i++) {
                    seats[ticket.route - 1][ticket.coach - 1][ticket.seat - 1][i] = false;
                    soldTicketTid[ticket.route - 1][ticket.coach - 1][ticket.seat - 1][i] = 0;
                }
                return true;
            } else {
                return false;
            }
        }
        finally {
            locks[ticket.route - 1].writeLock().unlock();
        }
    }

    private Ticket simplyBuyTicket(Ticket preTicket){
        for(int i = 0; i < seats[preTicket.route-1].length; i++){
            for(int j = 0; j < seats[preTicket.route-1][i].length; j++){
                boolean flag = true;
                for(int k = preTicket.departure-1; k < preTicket.arrival-1; k++){
                    if(seats[preTicket.route-1][i][j][k]){
                        flag = false;
                        break;
                    }
                }
                if(flag){
                    for(int k = preTicket.departure-1; k < preTicket.arrival-1; k++){
                        seats[preTicket.route-1][i][j][k] = true;
                    }
                    preTicket.coach = i+1;
                    preTicket.seat = j+1;
                    preTicket.tid = this.tid.getAndIncrement();
                    for(int k = preTicket.departure-1; k < preTicket.arrival-1; k++){
                        soldTicketTid[preTicket.route-1][i][j][k] = preTicket.tid;
                        soldTicketPassenger[preTicket.route-1][i][j][k] = preTicket.passenger;
                    }
                    return preTicket;
                }
            }
        }
        return null;
    }
    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival){
        if(!isLegal(route, departure, arrival)) return null;
        Ticket preTicket = new Ticket();
        preTicket.passenger = passenger;
        preTicket.route = route;
        preTicket.departure = departure;
        preTicket.arrival = arrival;
        locks[route-1].writeLock().lock();
        try {
            return simplyBuyTicket(preTicket);
        }
        finally {
            locks[route-1].writeLock().unlock();
        }
    }
}
