package ticketingsystem;

import ticketingsystem.train.Station;
import ticketingsystem.train.BitMethod;
import java.util.ArrayList;
import java.util.List;

public class StationArray {
	private int station_num;
	private int route_num;
	private int coach_num;
	private int seat_num;
	private Station[] stations;
	public StationArray(int station_num, int route_num,int coach_num,int seat_num) {
		this.station_num = station_num;
		this.route_num = route_num;
		this.coach_num = coach_num;
		this.seat_num = seat_num;
		this.stations = new Station[station_num -1];
		for (int i = 0; i < stations.length;i++)
			stations[i] = new Station(route_num, coach_num, seat_num);
	}
	
	 private Ticket toTicket(int index, int route, int departure, int arrival) {
	        Ticket t = new Ticket();
	        t.route = route;
	        t.coach = index / seat_num + 1;
	        t.seat = index % seat_num + 1;
	        t.departure = departure;
	        t.arrival = arrival;
	        return t;
	}
	
	public void bit_lock(Ticket temt,int flag) {
		int start = temt.departure;
		int end = temt.arrival;
		for(int i = start -1 ;i<end -1;i++)
			stations[i].bit_lock(temt.route,flag);	
//		for(int i = 0 ;i<station_num -1;i++)
//			stations[i].bit_lock(temt.route,flag);	
	}
	public void bit_unlock(Ticket temt,int flag) {
		int start = temt.departure;
		int end = temt.arrival;
		for(int i = start -1 ;i<end -1;i++)
			stations[i].bit_unlock(temt.route,flag);
//		for(int i = 0 ;i<station_num -1;i++)
//			stations[i].bit_unlock(temt.route,flag);
	}
	
    public void get_seat(Ticket temt) throws IllegalStateException {
    	int start = temt.departure;
		int end = temt.arrival;
		for(int i = start -1 ;i<end -1;i++)
			stations[i].get_seat(temt.route, temt.coach, temt.seat);
    }

    public void refund_seat(Ticket temt) throws IllegalStateException {
    	int start = temt.departure;
		int end = temt.arrival;
		for(int i = start -1 ;i<end -1;i++)
			stations[i].refund_seat(temt.route, temt.coach, temt.seat);
    }
	
    private long[] getBitMap(int route, int departure, int arrival) {
        long[] bitMap = stations[departure - 1].snapshot(route);
        for (int i = departure; i < arrival - 1; ++i) {
            long[] bm = stations[i].snapshot(route);
            for (int j = 0; j < bitMap.length; ++j)
                bitMap[j] |= bm[j];
        }
        return bitMap;
    }

    public List<Ticket> locateSeats(int route, int departure, int arrival) {
        ArrayList<Ticket> location = new ArrayList<>();
        long[] bitMap = getBitMap(route, departure, arrival);
        int size = Long.SIZE;
        for (int i = 0; i < bitMap.length; ++i) {
            List<Integer> l = BitMethod.get_zero_Locate(bitMap[i]);
            for (int index : l) {
            	location.add(toTicket(index + i * size, route, departure, arrival));
            	return location;
            }
        }
        return location;
    }

    public int counticketNums(int route, int departure, int arrival) {
        int ticket_nums = 0;
        long[] bitMap = getBitMap(route, departure, arrival);
        for (int i = 0; i < bitMap.length; ++i)
        	ticket_nums += Long.bitCount(~bitMap[i]); 
        return ticket_nums;
    }
    public boolean read_one(Ticket temt) {
		int start = temt.departure;
		int end = temt.arrival;
		boolean ret = true;
		for(int i = start -1 ;i<end -1;i++) {
			if(stations[i].read_one(temt.route, temt.coach, temt.seat)) {
				ret = false;
				break;
			}
		}
		return ret;
	}
	
}


