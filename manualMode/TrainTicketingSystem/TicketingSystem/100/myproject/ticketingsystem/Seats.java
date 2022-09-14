package ticketingsystem;

import java.util.concurrent.atomic.AtomicBoolean;

public class Seats {

    private AtomicBoolean busy=new AtomicBoolean(false);
    private AtomicBoolean[] stations;

    public Seats(int stationNum){
        this.stations=new AtomicBoolean[stationNum+1];
        for(int i=0;i<=stationNum;i++){
            stations[i]=new AtomicBoolean(false);
        }
    }

    public boolean querySeats(int departure,int arrival){
        for(int i=departure;i<arrival;i++){
            if(stations[i].get()) {
                return true;
            }
        }
        return false;
    }

    public boolean occupySeats(int departure,int arrival) {
        while (!busy.compareAndSet(false, true)) {
            for (int i = 1; i <= 500; i++) ;
        }
        if (querySeats(departure,arrival)) {
            busy.compareAndSet(true, false);
            return false;
        }
        for(int i=departure;i<arrival;i++){
            stations[i].set(true);
        }
        busy.compareAndSet(true, false);
        return true;
    }

    public boolean releaseSeats(int departure, int arrival){
        while (!busy.compareAndSet(false, true)) {
            for (int i = 1; i <= 500; i++) ;
        }
        for(int i=departure;i<arrival;i++){
            if(!stations[i].get()) {
                return false;
            }
        }
        for(int i=departure;i<arrival;i++){
            stations[i].set(false);
        }
        busy.compareAndSet(true, false);
        return true;
    }
}
