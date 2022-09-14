package ticketingsystem;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class TicketingDS implements TicketingSystem {
    private int RouteNum = 5;
    private int CoachNum = 8;
    private int SeatNum = 100;
    private int TotalSeat = 1000;
    private int StationNum = 10;
    private int ThreadNum = 16;
    private AtomicLong[][] SeatMap;
    private AtomicLong TicketId = new AtomicLong(1);
    //private int[] Previous;
    private ConcurrentHashMap<Long,Ticket>[] Check;

    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum){
        this.RouteNum = routenum;
        this.CoachNum = coachnum;
        this.SeatNum = seatnum;
        this.StationNum = stationnum;
        this.ThreadNum = threadnum;
        this.TotalSeat = this.CoachNum*this.SeatNum;
        this.SeatMap = new AtomicLong[this.RouteNum][this.TotalSeat];
        this.Check = new ConcurrentHashMap[this.RouteNum];
        for(int i=0;i<this.RouteNum;i++){
            this.Check[i] = new ConcurrentHashMap<>();
            for(int j=0;j<this.TotalSeat;j++){
                this.SeatMap[i][j] = new AtomicLong(0);
            }
        }
        //this.Previous = new int[this.RouteNum];
    }

    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival){
        if(route>this.RouteNum || route<=0)
            return null;
        if(departure>=this.StationNum || departure<=0 || arrival>this.StationNum || arrival<=1 || departure>arrival){
            return null;
        }
        Ticket back = null;

        long require = 0;
        require = require << (departure-1);
        for(int i=departure;i<arrival;i++){
            require++;
            if(i!=arrival-1){
                require = require << 1;
            }
        }
        require = require << (this.StationNum-arrival);
        //int getseat = this.Previous[route-1]%this.TotalSeat;
        int getseat = ThreadLocalRandom.current().nextInt(TotalSeat);
        boolean get = false;
        for(int i=0;i<this.TotalSeat;i++){
            long getmap = this.SeatMap[route-1][getseat].get();
            while((getmap & require) == 0) {
                if (this.SeatMap[route-1][getseat].compareAndSet(getmap, getmap + require)) {
                    back = new Ticket();
                    back.passenger = passenger;
                    back.route = route;
                    back.departure = departure;
                    back.arrival = arrival;
                    back.tid = this.TicketId.getAndIncrement();
                    back.coach = (getseat / this.SeatNum) + 1;
                    back.seat = ((getseat+1)%this.SeatNum == 0)? this.SeatNum: (getseat+1) % this.SeatNum;
                    //this.Previous[route - 1] = getseat % this.TotalSeat;
                    get = true;
                    Check[back.route-1].put(back.tid,back);
                }
                else {
                    getmap = this.SeatMap[route-1][getseat].get();
                }
            }
            if(get)
                break;
            getseat = (getseat + 1)% this.TotalSeat;
        }
        return back;
    }

    @Override
    public int inquiry(int route, int departure, int arrival){
        int rest = 0;
        if(route>this.RouteNum || route<=0)
            return 0;
        if(departure>=this.StationNum || departure<=0 || arrival>this.StationNum || arrival<=1 || departure>arrival)
            return 0;
        long require = 0;
        require = require << (departure-1);
        for(int i=departure;i<arrival;i++){
            require++;
            if(i!=arrival-1){
                require = require << 1;
            }
        }
        require = require << (this.StationNum-arrival);

        for(int i=0;i<this.TotalSeat;i++){
            if((this.SeatMap[route-1][i].get() & require) == 0)
                rest++;
        }
        return rest;
    }

    @Override
    public boolean refundTicket(Ticket ticket){
        long require = 0;
        require = require << (ticket.departure-1);
        for(int i=ticket.departure;i<ticket.arrival;i++){
            require++;
            if(i!=ticket.arrival-1){
                require = require << 1;
            }
        }
        require = require << (this.StationNum-ticket.arrival);
        int seatnum = (ticket.coach-1)*this.SeatNum+ticket.seat-1;
        long getmap = this.SeatMap[ticket.route-1][seatnum].get();
        Ticket compare;
        if((compare = Check[ticket.route-1].get(ticket.tid))==null){
            System.out.println("check out!!!!!!!!!");
            return false;
        }
        else{
            if(compare.arrival != ticket.arrival || compare.departure != ticket.departure || compare.seat != ticket.seat
                    || compare.coach != ticket.coach || compare.route != ticket.route || !compare.passenger.equals(ticket.passenger)){
                System.out.println("check out");
                return false;
            }
        }

        while((getmap & require) != 0){
            if(this.SeatMap[ticket.route-1][seatnum].compareAndSet(getmap,getmap-require)){
                Check[ticket.route-1].remove(ticket.tid,ticket);
                return true;
            }
            else{
                getmap = this.SeatMap[ticket.route-1][seatnum].get();
            }
        }
        return false;
    }
    //ToDo
    /*public long getnum(){
        return this.Check.size();
    }*/

}
