package ticketingsystem;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Route2 {
    private int coachNum;
    private int seatNum;
    private int stationNum;
    private Seat[] seats;
    private int []staionToStaionSeatNum;
    private Stack<Integer>[] stationToStationSeat;
    //static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    /*private int computeIndex(int departure, int arrival) {
        return (((departure - 1) * ((stationNum << 1) - departure)) >> 1) + arrival - departure - 1;
    }*/
    public Route2(int route, int coachNum, int seatNum, int stationNum) {
        this.coachNum = coachNum;
        this.seatNum = seatNum;
        this.stationNum = stationNum;
        int seatTotalNum = coachNum * seatNum;
        seats = new Seat[seatTotalNum + 1];
        //Set<Integer>tempSets = new HashSet<Integer>();
        for (int i = 1; i <= seatTotalNum; ++i) {
            seats[i] = new Seat(stationNum);
            //tempSets.add(i);
        }
        StationMapping stationMapping = new StationMapping(stationNum);
        staionToStaionSeatNum = new int[stationNum * (stationNum - 1) / 2];
        stationToStationSeat = new Stack[stationNum * (stationNum - 1) / 2];
        for (int i = 1; i < stationNum; ++i) {
            for (int j = i + 1; j <= stationNum; ++j) {
                int index = StationMapping.stationToStaion[i][j];
                stationToStationSeat[index] = new Stack<Integer>();
                staionToStaionSeatNum[index] = seatTotalNum;
                for (int k = seatTotalNum; k >= 1; --k) {
                    stationToStationSeat[index].push(k);
                }
            }
        }

    }

    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        //int index = computeIndex(departure, arrival);
        int index = StationMapping.stationToStaion[departure][arrival];
        int temp = stationNum * (stationNum - 1) / 2;
        boolean[] oldValidStatus = new boolean[temp];
        boolean[] newValidStatus = new boolean[temp];
        int i = -1;
        boolean isHaveSeat = false;
        //可以修改成乐观锁的形式
        //可以维护没两站之间再维护一个数据结构，表示这两站之间那些座位是可用的，直接取，不在需要遍历所有的座位。
        synchronized (this) {
            if (staionToStaionSeatNum[index] == 0) {
                return null;
            }
            if (!stationToStationSeat[index].isEmpty()) {
                i = stationToStationSeat[index].peek();
                if (seats[i].isValid(departure, arrival)) {
                    isHaveSeat = true;
                    // System.out.println("车次："+ route + "座位号："+ i + "出发站：" +departure + "到达站" + arrival);
                    for (int j = 0; j < seats[i].isValidStaionToStaion.length; ++j) {
                        oldValidStatus[j] = seats[i].isValidStaionToStaion[j];
                    }
                    if (seats[i].occupy(departure, arrival)) {
                        updateTicket(oldValidStatus, newValidStatus, seats[i], i,-1);
                    }
                }
            }
            if (isHaveSeat) {
                return constructTicket(passenger, route, departure, arrival, i);
            } else {
                return null;
            }
        }


    }

    private Ticket constructTicket(String passenger, int route, int departure, int arrival, int seat) {
        int coach;
        int seatOfCoach;
        if (seat % seatNum == 0) {
            coach = seat / seatNum;
            seatOfCoach = seatNum;
        } else {
            coach = seat / seatNum + 1;
            seatOfCoach = seat % seatNum;
        }
        return new Ticket(TicketID.getTicketId(), passenger, route, coach, seatOfCoach, departure, arrival);
    }

    public int inquiry(int route, int departure, int arrival) {
        synchronized (this) {
            return staionToStaionSeatNum[StationMapping.stationToStaion[departure][arrival]];
        }
    }

    public boolean refundTicket(Ticket ticket) {
        // System.out.println(ticket.coach + " " + ticket.seat);
        int seatNo = (ticket.coach - 1) * seatNum + ticket.seat;
        int temp = stationNum * (stationNum - 1) / 2;
        boolean[] oldValidStatus = new boolean[temp];
        boolean[] newValidStatus = new boolean[temp];
        synchronized (this) {
            for (int i = 0; i < seats[seatNo].isValidStaionToStaion.length; ++i) {
                oldValidStatus[i] = seats[seatNo].isValidStaionToStaion[i];
            }
            if (seats[seatNo].unoccupy(ticket.departure, ticket.arrival)) {
                updateTicket(oldValidStatus, newValidStatus, seats[seatNo], seatNo, 1);
                return true;
            }
            return false;
        }

    }

    public void updateTicket(boolean[] oldValidStatus, boolean[] newValidStatus, Seat seat, int seatIndex, int incOrDec) {
        for (int i = 0; i < seat.isValidStaionToStaion.length; ++i) {
            newValidStatus[i] = seat.isValidStaionToStaion[i];
        }
        for (int i = 1; i < stationNum; ++i) {
            for (int j = i + 1; j <= stationNum; ++j) {
                //int index = computeIndex(i, j);
                int index = StationMapping.stationToStaion[i][j];
                if (oldValidStatus[index] != newValidStatus[index]) {
                    staionToStaionSeatNum[index] += incOrDec;
                    if (incOrDec == 1) {
                        stationToStationSeat[index].push(seatIndex);
                    } else {
                        stationToStationSeat[index].pop();
                    }
                }
            }
        }
    }

}
