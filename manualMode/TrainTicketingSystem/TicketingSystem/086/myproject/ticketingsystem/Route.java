package ticketingsystem;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

class Route {
    int departure;
    int routeID;
    int coachNum;
    int seatNum;
    int total_seat;
    int stationNum;
    State[][] states; //states[1][2]: 1->2, states[2][5]: 2->5, 以此类推

    Route(int departure, int stationNum, int routID, int coachNum, int seatNum) {
        this.departure = departure;
        this.routeID = routID;
        this.coachNum = coachNum;
        this.seatNum = seatNum;
        total_seat = coachNum * seatNum;
        this.stationNum = stationNum;
        states = new State[stationNum + 1][stationNum + 1];
        for (int i = 1; i < stationNum; i++) {
            for (int j = i + 1; j <= stationNum; j++) {
                states[i][j] = new State(coachNum, seatNum);
            }
        }
    }
}

class State {
    private static final long serialVersionUID = 3L;
    ConcurrentHashMap<Integer, Boolean>[] coach;
    AtomicInteger left;

    State(int coachNum, int seatNum) {
        //coach在代码中从0开始计数
        coach = (ConcurrentHashMap<Integer, Boolean>[]) new ConcurrentHashMap[coachNum];
        for (int i = 0; i < coachNum; i++) {
            coach[i] = new ConcurrentHashMap<>(seatNum);
            for (int j = 1; j <= seatNum; j++) {
                coach[i].put(j, true);
            }
        }
        left = new AtomicInteger(coachNum * seatNum);
    }
}