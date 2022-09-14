package ticketingsystem.train;
import java.util.concurrent.atomic.AtomicLong;

public class Station {
	private int route_num;
	private int coach_num;
	private int seat_num;
	private BitMap[] bitMap;
	public Station(int route_num,int coach_num,int seat_num) {
		this.route_num = route_num;
		this.coach_num = coach_num;
		this.seat_num = seat_num;
		bitMap = new BitMap[route_num];
        for (int i = 0; i < route_num; ++i)
            bitMap[i] = new BitMap(coach_num * seat_num);
	}
    private int getBitIndex(int coach, int seat) {
        return seat_num * (coach-1)  + seat-1;
    }
    public void get_seat(int route,int coach,int seat) throws IllegalStateException {
    	bitMap[route - 1].set_one(getBitIndex(coach, seat));
    }
    public void refund_seat(int route,int coach,int seat) throws IllegalStateException {
    	bitMap[route - 1].set_zero(getBitIndex(coach, seat));
    }
    public long[] snapshot(int route) {
        return bitMap[route - 1].bitSnapshot();
    }
    public void bit_lock(int route,int flag) {
    	bitMap[route - 1].lock(flag);
	}
	public void bit_unlock(int route,int flag) {
		bitMap[route - 1].unlock(flag);
	}
    public boolean read_one(int route,int coach,int seat) {
    	return bitMap[route - 1].read_one(getBitIndex(coach, seat));
    }
	
}
