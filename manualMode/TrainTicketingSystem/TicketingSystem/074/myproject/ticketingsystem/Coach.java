package ticketingsystem;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Coach {
    private Seat[] seats;
    ReentrantReadWriteLock readAndwriteLockRefund=new ReentrantReadWriteLock();
    ReentrantReadWriteLock readAndwriteLockBuy=new ReentrantReadWriteLock();
    public Coach(int seatnum) {
        seats=new Seat[seatnum+1];
        for(int i=1;i<(seatnum+1);i++){
            seats[i]=new Seat();
        }
    }

    public Seat[] getSeats() {
        return seats;
    }
}
