package ticketingsystem;

import java.util.concurrent.atomic.AtomicInteger;

public class Route {

    int coachNum;
    int seatNum;
    int stationNum;


    Coach[] coaches;


    AtomicInteger[][] remainTicketNum;
    int maxTicketNum;


    public Route(int coachNum, int seatNum, int stationNum) {
        this.coachNum = coachNum;
        this.seatNum = seatNum;
        this.stationNum = stationNum;

        coaches = new Coach[coachNum + 1];
        for(int i = 1; i <= coachNum; i++) {
            coaches[i] = new Coach(seatNum, stationNum);
        }


        remainTicketNum = new AtomicInteger[stationNum + 1][stationNum + 1];


        for(int i = 1; i <= stationNum; i++) {
            for(int j = 1; j <= stationNum; j++) {
                remainTicketNum[i][j] = new AtomicInteger(coachNum * seatNum);

            }
        }

        maxTicketNum = coachNum * seatNum;

    }


    // 是否需要放到别的地方？
    public int getRemainTicketNum(int departure, int arrival) {
        return remainTicketNum[departure][arrival].get();
    }
}
