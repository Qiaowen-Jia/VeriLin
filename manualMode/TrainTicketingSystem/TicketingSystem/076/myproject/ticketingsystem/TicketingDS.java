package ticketingsystem;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class TicketingDS implements TicketingSystem {
	
	private int routenum = 5;
	private int coachnum = 8;
	private int seatnum = 100;
	private int stationnum = 10;
	
	public int[][][] statusOfSeat;
	
	private AtomicLong atomicLong = new AtomicLong (1L); 
	private ReentrantLock[] lockArray;
	
	private void init() {
		this.statusOfSeat = new int[routenum + 1][coachnum + 1][seatnum + 1];
	    
	    this.lockArray = new ReentrantLock[routenum + 1];
	    this.lockArray[0] = new ReentrantLock();
	    for(int i = 1;i < routenum + 1;i++) {
	    	this.lockArray[i] = new ReentrantLock();
	    	for(int j = 1;j < coachnum + 1;j++) {
	    		for(int k = 1;k < seatnum + 1;k++){
	    			this.statusOfSeat[i][j][k] = 0;
	    		}
	    	}
	    } 
	}
	
	public TicketingDS() {
		init();
	}
	
	public TicketingDS(int routenum_para, int coachnum_para, 
			int seatnum_para, int stationnum_para, int threadnum_para) {
		this.routenum = routenum_para;
		this.coachnum = coachnum_para;
		this.seatnum = seatnum_para;
		this.stationnum = stationnum_para;
		init();
	}
	
	private Ticket getTicket(long tid, String passenger, int route, int coach,
			int seat, int departure, int arrival) {
		Ticket ticket = new Ticket();
		ticket.tid = tid;
		ticket.passenger = passenger;
		ticket.route = route;
		ticket.coach = coach;
		ticket.seat = seat;
		ticket.departure = departure;
		ticket.arrival = arrival;
		return ticket;
	}
	
	public boolean seatIsValid(int route, int coach, int seat, int departure, int arrival) {
		int j = 0;
		for(int i = departure - 1;i < arrival - 1;i++) {
			j = this.statusOfSeat[route][coach][seat] >> i;
			j &= 1;
			if(1 == j)
			{
				return false;
			}		
		}
		return true;
	}
	
	public void setSeatStatus(int route, int coach, int seat, int departure, int arrival,int sign) {
		int j = 0;
		for(int i = departure - 1;i < arrival - 1;i++) {
			if(0 == sign) {
				j = 0xFFFF;
				j ^= (1 << i);
				this.statusOfSeat[route][coach][seat] &= j;
			}else {
				j = 1 << i;
				this.statusOfSeat[route][coach][seat] |= j;
			}	
		}
	}

	@Override
	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
		if(passenger == null || "".equals(passenger) 
				|| route < 1 || route > this.routenum
				|| departure >= arrival || departure < 1 || arrival > this.stationnum)
			return null;
		Ticket ticket = null;
		lockArray[route].lock();
		try {
			for(int i = 1;i < coachnum + 1;i++) {
		    	for(int j = 1;j < seatnum + 1;j++) {
	    			if(seatIsValid(route,i,j,departure,arrival))
	    			{
	    				long tid = atomicLong.getAndIncrement();
	    				ticket = getTicket(tid, passenger, route, i, j, departure, arrival);
	    				setSeatStatus(route,i,j,departure,arrival,1);
	    				return ticket;
	    			}
		    	}
		    }
			return null;
		}finally {
			lockArray[route].unlock();
		}	
	}

	@Override
	public int inquiry(int route, int departure, int arrival) {
		if(route < 1 || route > this.routenum
				|| departure >= arrival || departure < 1 || arrival > this.stationnum)
			return 0;
		int result = 0;
		//lockArray[route].lock();
		//try {
			for(int i = 1;i < coachnum + 1;i++) {
		    	for(int j = 1;j < seatnum + 1;j++) {
	    			if(seatIsValid(route,i,j,departure,arrival))
	    			{
	    				result++;
	    			}
		    	}
		    }
			return result;
		//}finally {
		//	lockArray[route].unlock();
		//}
	}

	@Override
	public boolean refundTicket(Ticket ticket) {
		if(ticket.tid < 1 || ticket.tid > atomicLong.get() 
				|| ticket.route < 1 || ticket.route > routenum 
				|| ticket.coach < 1 || ticket.coach > coachnum 
				|| ticket.seat < 1 || ticket.seat > seatnum 
				|| ticket.arrival <= ticket.departure
		        || ticket.departure < 1 || ticket.arrival > stationnum)
			return false;
		lockArray[ticket.route].lock();
		try {
			if(!seatIsValid(ticket.route,ticket.coach,ticket.seat,ticket.departure,ticket.arrival))
			{
				setSeatStatus(ticket.route,ticket.coach,ticket.seat,ticket.departure,ticket.arrival,0);
				return true;
			}
			return false;
		}finally {	
			lockArray[ticket.route].unlock();
		}
	}

}
