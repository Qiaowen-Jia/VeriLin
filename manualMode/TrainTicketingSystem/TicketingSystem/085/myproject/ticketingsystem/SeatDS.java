package ticketingsystem;

import java.util.Arrays;

public class SeatDS {
    public int stationnum;
    public int[] seat_occupy_status;
    public int seat_id;//座位编号


    public SeatDS(int seat_id, int stationnum) {
        this.seat_id = seat_id;
        this.stationnum = stationnum;
        this.seat_occupy_status = new int[stationnum + 1];
        Arrays.fill(seat_occupy_status, 0);
    }


}
