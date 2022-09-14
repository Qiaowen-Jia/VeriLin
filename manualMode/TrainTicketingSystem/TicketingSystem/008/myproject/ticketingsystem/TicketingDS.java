package ticketingsystem;

import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;

class Train{
	int coach_num = 8;
	int total_seat_num = 100;
	int station_num = 10;
	AtomicInteger[] remain_seat = new AtomicInteger[station_num+1];
	// int[] remain_seat = new int[station_num+1];
	boolean[][] seat_state;
	ReentrantLock[] lock_array_seat;
	
	Train(int coachnum, int seatnum, int stationnum){
		coach_num = coachnum;
		total_seat_num = coachnum * seatnum;
		station_num = stationnum;
		seat_state = new boolean[total_seat_num+1][station_num+1]; // true/false: seat has/not been booked
		lock_array_seat = new ReentrantLock[total_seat_num+1];
		for (int i = 0; i < lock_array_seat.length; i++)
			lock_array_seat[i] = new ReentrantLock();
		for (int i = 0; i < remain_seat.length; i++)
			remain_seat[i] = new AtomicInteger(total_seat_num);
	}
}

public class TicketingDS implements TicketingSystem {
	AtomicInteger TID = new AtomicInteger(1);
	int route_num = 5;
	int coach_num = 8;
	int seat_num = 100;
	int station_num = 10;
	int thread_num = 16;
	Train[] train;
	ConcurrentHashMap<Long, Ticket> sold_ticket;
	Random rand;

	TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum){
		route_num = routenum;
		coach_num = coachnum;
		seat_num = seatnum;
		station_num = stationnum;
		thread_num = threadnum;
		train = new Train[route_num + 1];
		sold_ticket = new ConcurrentHashMap<Long, Ticket>();
		for (int i = 0; i < train.length; i++){
			train[i] = new Train(coach_num, seat_num, station_num);
		}
		rand = new Random();
	}

	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
		int rand_part = train[route].total_seat_num;
		int index_seat = rand.nextInt(rand_part) + 1;
		int seat = 0;
		for(int i = 0; i < 2; i++){
			int begin = (i == 0) ? 1 : index_seat+1;
			int end = (i == 0) ? index_seat : train[route].total_seat_num;
			for(seat = begin; seat <= end; seat++){
				if(isValidSeat(route, seat, departure, arrival)){
					train[route].lock_array_seat[seat].lock();
					try{
						if(isValidSeat(route, seat, departure, arrival)){
							setSeat(route, seat, departure, arrival);
							break;
						}
					}
					finally{
						train[route].lock_array_seat[seat].unlock();
					}
				}
			}
			if(seat <= end)
				break;
		}
		if(seat > 0 && seat <= train[route].total_seat_num){
			Ticket ticket = new Ticket();
			ticket.departure = departure;
			ticket.arrival = arrival;
			ticket.passenger = passenger;
			ticket.route = route;
			ticket.coach = (seat-1) / seat_num + 1;
			ticket.seat = (seat-1) % seat_num + 1;
			ticket.tid = TID.getAndIncrement();
			sold_ticket.put(ticket.tid, ticket);
			return ticket;
		}
		else
			return null;
	}

	public int inquiry(int route, int departure, int arrival) {
		//System.out.printf("inquiry ticket\n");
		int available_seat_num = train[route].total_seat_num;
		for(int station = departure; station < arrival; station++){
			// available_seat_num = Math.min(train[route].remain_seat[station], available_seat_num);
			int remain = train[route].remain_seat[station].get();
			if(remain < available_seat_num)
				available_seat_num = remain;
		}
		// for(int seat = 1; seat <= train[route].total_seat_num; seat++){
		// 	int station;
		// 	for(station = departure; station < arrival; station++){
		// 		if(train[route].seat_state[seat][station])
		// 			break;
		// 	}
		// 	if(station == arrival)
		// 		available_seat_num++;
		// }
		return available_seat_num;
	}

	public boolean refundTicket(Ticket ticket) {
		int seat = (ticket.coach-1)*seat_num + ticket.seat;
		if(!isValidTicket(ticket)){
			return false;
		}
		train[ticket.route].lock_array_seat[seat].lock();
		try{
			refundSeat(ticket.route, seat, ticket.departure, ticket.arrival);
		}
		finally{
			train[ticket.route].lock_array_seat[seat].unlock();
		}
		sold_ticket.remove(ticket.tid);
		return true;
	}

	
	public void setSeat(int route, int seat, int departure, int arrival){
		for(int station = departure; station < arrival; station++){
			train[route].seat_state[seat][station] = true;
			train[route].remain_seat[station].getAndDecrement();
		}
	}

	public void refundSeat(int route, int seat, int departure, int arrival){
		for(int station = departure; station < arrival; station++){
			train[route].seat_state[seat][station] = false;
			train[route].remain_seat[station].getAndIncrement();
		}
	}

	public boolean isValidSeat(int route, int seat, int departure, int arrival){
		for(int station = departure; station < arrival; station++){
			if(train[route].seat_state[seat][station])
				return false;
		}
		return true;
	}

	public boolean isValidTicket(Ticket ticket){
		Ticket old_ticket = sold_ticket.get(ticket.tid);
		int seat = (ticket.coach-1)*seat_num + ticket.seat;
		int route = ticket.route;
		if(old_ticket == null || old_ticket.route != ticket.route ||
				old_ticket.coach != ticket.coach ||
				old_ticket.seat != ticket.seat ||
				old_ticket.departure != ticket.departure ||
				old_ticket.arrival != ticket.arrival ||
				seat <= 0 || seat > train[route].total_seat_num
		){
			return false;
		}
		return true;
	}

}
