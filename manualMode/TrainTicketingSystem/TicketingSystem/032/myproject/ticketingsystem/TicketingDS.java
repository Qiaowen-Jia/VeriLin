package ticketingsystem;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.List;

public class TicketingDS implements TicketingSystem {
	//ToDo
	private int route_num;
	private int coach_num;
	private int seat_num;
	private int station_num;
	private int thread_num;
	private StationArray train_seat;
	private ConcurrentHashMap<Long, Ticket> sell_record;
	private static final int randBuyNumber = 10;
	private AtomicLong count;
	private long final_rand;
	private AtomicLongArray count_long;
	
	public TicketingDS() {
		this.route_num = 20;
		this.coach_num = 10;
		this.seat_num = 100;
		this.station_num = 16;
		this.thread_num = 64;
		this.train_seat = new StationArray(station_num, route_num, coach_num, seat_num);	
		int recodSize = (int) (route_num * coach_num * seat_num * station_num * 0.5);
		this.sell_record = new ConcurrentHashMap<>(recodSize, 0.80f, thread_num);
		this.count = new AtomicLong(0);
		this.final_rand = thread_num * 5;
		this.count_long = new AtomicLongArray((int)final_rand);
		for(long i = 0;i<final_rand;i++) {
			count_long.set((int)i,i);
		}
	}
	
	public TicketingDS(int route_num, int coach_num, int seat_num, int station_num, int thread_num) {
		this.route_num = route_num;
		this.coach_num = coach_num;
		this.seat_num = seat_num;
		this.station_num = station_num;
		this.thread_num = thread_num;
		this.train_seat = new StationArray(station_num, route_num, coach_num, seat_num);	
		int recodSize = (int) (route_num * coach_num * seat_num * station_num * 0.5);
		sell_record = new ConcurrentHashMap<>(recodSize, 0.80f, thread_num);
		final_rand = thread_num * 5;
		count_long = new AtomicLongArray((int)final_rand);
		for(long i = 0;i<final_rand;i++) {
			count_long.set((int)i,i);
		}
	}
	
	public Ticket toTicket(int route, int coach, int seat, int departure,int arrival) {
		Ticket t = new Ticket();
        t.route = route;
        t.coach = coach;
        t.seat = seat;
        t.departure = departure;
        t.arrival = arrival;
        return t;
    }
	
	public Ticket toTicket(int route, int departure,int arrival) {
		Ticket t = new Ticket();
        t.route = route;
        t.departure = departure;
        t.arrival = arrival;
        return t;
    }
	
	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {//购票
		int flag = 1;
		if (passenger == null || passenger.equals("") 
				|| route <= 0 || route > route_num 
				|| departure <= 0 || departure > station_num 
				|| arrival <= 0 || arrival > station_num 
				|| departure >= arrival)
			return null;
		Ticket temt = toTicket(route, departure, arrival);
		try {
			train_seat.bit_lock(temt,flag);
			ThreadLocalRandom rand = ThreadLocalRandom.current();
			Ticket tic = toTicket(route, 0, 0, departure, arrival);
			for (int i = 0; i < randBuyNumber; ++i) {
				tic.coach = rand.nextInt(coach_num) + 1;
				tic.seat = rand.nextInt(seat_num) + 1;
				if (train_seat.read_one(tic)) {
					train_seat.get_seat(tic);
					tic.tid = add_id();
					tic.passenger = passenger;
					sell_record.put(tic.tid, tic);
					return tic;
				}		
			}
			List<Ticket> Ticket_set = train_seat.locateSeats(route, departure, arrival);
			for (Ticket tic1 : Ticket_set) {
//				if (train_seat.read_one(tic1)) {
//					train_seat.get_seat(tic1);
//					tic1.tid = add_id();
//					tic1.passenger = passenger;
//					sell_record.put(tic1.tid, tic1);
//					return tic1;
//				}		
				train_seat.get_seat(tic1);
				tic1.tid = add_id();
				tic1.passenger = passenger;
				sell_record.put(tic1.tid, tic1);
				return tic1;
			}		
			return null;
		} finally {
			train_seat.bit_unlock(temt,flag);
		}
	}
	public int inquiry(int route, int departure, int arrival) {//查询余票
		int flag = 0;
		Ticket temt = toTicket(route, departure, arrival);
		try {
			train_seat.bit_lock(temt,flag);
			return train_seat.counticketNums(route, departure, arrival);
		} finally {
			train_seat.bit_unlock(temt,flag);
		}
	}
	public boolean refundTicket(Ticket ticket) {//退票
		int flag = 1;
		if (ticket != null) {
			long tid = ticket.tid;
			if (ticket.tid < 0 || !sell_record.containsKey(tid)
					|| ticket.coach <= 0 || ticket.coach > coach_num 
					|| ticket.seat <= 0  || ticket.seat > seat_num
					|| ticket.passenger == null || ticket.passenger.equals("") 
					|| ticket.route <= 0 || ticket.route > route_num 
					|| ticket.departure <= 0 || ticket.departure > station_num 
					|| ticket.arrival <= 0 || ticket.arrival > station_num 
					|| ticket.departure >= ticket.arrival)
				return false;
			if (train_seat.read_one(ticket) || !compareTicket(ticket, sell_record.get(tid)))
				return false;
//			if (!compareTicket(ticket, sell_record.get(tid)))
//				return false;
			if (sell_record.remove(tid) == null)
				return false;
			try {
				train_seat.bit_lock(ticket,flag);
				train_seat.refund_seat(ticket);
				return true;
			} finally {
				train_seat.bit_unlock(ticket,flag);
			}
		}else {
			return false;
		}
		
	}
	
	private boolean compareTicket(Ticket a, Ticket b) {
		if (a.tid != b.tid 
				|| a.passenger == null 
				|| b.passenger == null 
				|| !a.passenger.equals(b.passenger)
				|| a.route != b.route 
				|| a.coach != b.coach 
				|| a.seat != b.seat 
				|| a.departure != b.departure
				|| a.arrival != b.arrival)
			return false;
		return true;
	}
   public long add_id() {
	   ThreadLocalRandom rand = ThreadLocalRandom.current();
	   int tem = rand.nextInt((int)final_rand);
	   return count_long.addAndGet(tem, final_rand);
   }
}
