package ticketingsystem;

/**
 * @author Jianyong Feng
 **/
class TicketingSystemInfo {
    private int routeNum;
    private int stationNum;
    private int coachNum;
    private int seatNum;

    public TicketingSystemInfo(int routeNum, int stationNum, int coachNum, int seatNum) {
        this.routeNum = routeNum;
        this.stationNum = stationNum;
        this.coachNum = coachNum;
        this.seatNum = seatNum;
    }

    public int getRouteNum() {
        return routeNum;
    }

    public int getStationNum() {
        return stationNum;
    }

    public int getCoachNum() {
        return coachNum;
    }

    public int getSeatNum() {
        return seatNum;
    }
}
