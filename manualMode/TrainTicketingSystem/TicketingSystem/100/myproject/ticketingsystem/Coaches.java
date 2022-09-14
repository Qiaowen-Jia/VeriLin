package ticketingsystem;

import java.util.concurrent.atomic.AtomicInteger;

public class Coaches {
    private int seatNum;
    private Seats [] seats;

    public Coaches(int seatNum,int stationNum){
        this.seatNum=seatNum;
        this.seats=new Seats[seatNum+1];
        for(int i=0;i<=seatNum;i++){
            seats[i]=new Seats(stationNum);
        }
    }

    public int queryCoaches(int departure,int arrival){
        AtomicInteger availableSeatsNum= new AtomicInteger(0);
        for(int i=1;i<=seatNum;i++){
            if(!seats[i].querySeats(departure, arrival)){
                availableSeatsNum.addAndGet(1);
            }
        }
        return availableSeatsNum.get();
    }

    public int occupyCoaches(int departure,int arrival){
        int j;
        j=queryCoaches(departure,arrival);
        if(j<=0) {
            return -1;
        }
        for(int i=1;i<=seatNum;i++){
            if(seats[i].occupySeats(departure,arrival)){
                return i;
            }
        }
        return -1;
    }

    public boolean releaseCoaches(int departure,int arrival,int seatNumber){
        return seats[seatNumber].releaseSeats(departure, arrival);
    }
}
