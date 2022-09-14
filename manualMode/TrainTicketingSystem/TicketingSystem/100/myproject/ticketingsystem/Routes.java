package ticketingsystem;

import java.util.concurrent.atomic.AtomicInteger;

public class Routes {
    private int coachNum;
    private Coaches [] coaches;

    public Routes(int coachNum,int seatNum,int stationNum){
        this.coachNum=coachNum;
        this.coaches=new Coaches[coachNum+1];
        for(int i=0;i<=coachNum;i++){
            coaches[i]=new Coaches(seatNum, stationNum);
        }
    }

    public int queryRoutes(int departure,int arrival){
        AtomicInteger availableSeatNumInThisRoute=new AtomicInteger();
        for(int i=1;i<=coachNum;i++){
            int j;
            j=coaches[i].queryCoaches(departure, arrival);
            availableSeatNumInThisRoute.addAndGet(j);
        }
        return availableSeatNumInThisRoute.get();
    }

    public Ticket occupyRoutes(int departure,int arrival){
        int j;
        j=queryRoutes(departure,arrival);
        if(j==0){
            return null;
        }
        for(int i=1;i<=coachNum;i++){
            int seatnumber1=coaches[i].occupyCoaches(departure,arrival);
            if(seatnumber1!=-1){
                Ticket ticketR=new Ticket();
                ticketR.coach=i;
                ticketR.seat=seatnumber1;
                return ticketR;
            }
        }
        return null;
    }

    public boolean releaseRoutes(int departure,int arrival,int coachNumber,int seatNumber){
        return coaches[coachNumber].releaseCoaches(departure,arrival,seatNumber);
    }
}
