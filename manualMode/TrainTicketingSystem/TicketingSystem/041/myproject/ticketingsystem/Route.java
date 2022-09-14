package ticketingsystem;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Route {
    //
    private int coachNum;
    private int seatNum;
    private int stationNum;
    private Seat[] seats;
    private int []staionToStaionSeatNum;
    private Map<Long, Ticket> ticketHashMap;
   //static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    /*private int computeIndex(int departure, int arrival) {
        return (((departure - 1) * ((stationNum << 1) - departure)) >> 1) + arrival - departure - 1;
    }*/
    public Route(int route, int coachNum, int seatNum, int stationNum) {
        this.coachNum = coachNum;
        this.seatNum = seatNum;
        this.stationNum = stationNum;
        ticketHashMap = new HashMap<Long, Ticket>();
        int seatTotalNum = coachNum * seatNum;
        seats = new Seat[seatTotalNum + 1];
        for (int i = 1; i <= seatTotalNum; ++i) {
            seats[i] = new Seat(stationNum);
        }
        StationMapping stationMapping = new StationMapping(stationNum);
        staionToStaionSeatNum = new int[stationNum * (stationNum - 1) / 2];
        for (int i = 1; i < stationNum; ++i) {
            for (int j = i + 1; j <= stationNum; ++j) {
                staionToStaionSeatNum[StationMapping.stationToStaion[i][j]] = seatTotalNum;
            }
        }

    }

    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        //int index = computeIndex(departure, arrival);
        int temp = stationNum * (stationNum - 1) / 2;
        boolean[] oldValidStatus = new boolean[temp];
        boolean[] newValidStatus = new boolean[temp];
        //可以修改成乐观锁的形式
        //可以维护没两站之间再维护一个数据结构，表示这两站之间那些座位是可用的，直接取，不在需要遍历所有的座位。
        synchronized (this) {
            int index = StationMapping.stationToStaion[departure][arrival];
            if (staionToStaionSeatNum[index] == 0) {
                return null;
            }
            int i;
            for (i = 1; i < seats.length; ++i) {
                if (seats[i].isValid(departure, arrival)) {
                    // System.out.println("车次："+ route + "座位号："+ i + "出发站：" +departure + "到达站" + arrival);
                    for (int j = 0; j < seats[i].isValidStaionToStaion.length; ++j) {
                        oldValidStatus[j] = seats[i].isValidStaionToStaion[j];
                    }
                    if (seats[i].occupy(departure, arrival)) {
                        updateTicket(oldValidStatus, newValidStatus, seats[i], -1);
                    }
                    break;
                }
            }
            if (i < seats.length) {
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
        long ticketId = TicketID.getTicketId();
        Ticket ticket = new Ticket(ticketId, passenger, route, coach, seatOfCoach, departure, arrival);
        ticketHashMap.put(ticketId, ticket);
        return ticket;
    }

    public int inquiry(int route, int departure, int arrival) {
        synchronized (this) {
            return staionToStaionSeatNum[StationMapping.stationToStaion[departure][arrival]];
        }
    }

    public boolean refundTicket(Ticket ticket) {
       // System.out.println(ticket.coach + " " + ticket.seat);
        int temp = stationNum * (stationNum - 1) / 2;
        boolean[] oldValidStatus = new boolean[temp];
        boolean[] newValidStatus = new boolean[temp];
        synchronized (this) {
            if (!isValidTicket(ticket)) {
                return false;
            }
            int seatNo = (ticket.coach - 1) * seatNum + ticket.seat;
            for (int i = 0; i < seats[seatNo].isValidStaionToStaion.length; ++i) {
                oldValidStatus[i] = seats[seatNo].isValidStaionToStaion[i];
            }
            if (seats[seatNo].unoccupy(ticket.departure, ticket.arrival)) {
                updateTicket(oldValidStatus, newValidStatus, seats[seatNo], 1);
                return true;
            }
            return false;
        }

    }
    private boolean isValidTicket(Ticket ticket) {
        Ticket soldTicket = ticketHashMap.remove(ticket.tid);
        return  soldTicket != null &&
                ticket.passenger.equals(soldTicket.passenger) &&
                ticket.departure ==soldTicket.departure &&
                ticket.arrival == soldTicket.arrival &&
                ticket.seat == soldTicket.seat &&
                ticket.coach == soldTicket.coach &&
                ticket.route == soldTicket.route;
    }

    public void updateTicket(boolean[] oldValidStatus, boolean[] newValidStatus, Seat seat, int incOrDec) {
        for (int i = 0; i < seat.isValidStaionToStaion.length; ++i) {
            newValidStatus[i] = seat.isValidStaionToStaion[i];
        }
        for (int i = 1; i < stationNum; ++i) {
            for (int j = i + 1; j <= stationNum; ++j) {
                //int index = computeIndex(i, j);
                int index = StationMapping.stationToStaion[i][j];
                if (oldValidStatus[index] != newValidStatus[index]) {
                    staionToStaionSeatNum[index] += incOrDec;
                }
            }
        }
    }

}
