package ticketingsystem;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.ReentrantLock;

//import ticketingsystem.VeriTmp.TraceLine;


public class TicketingDS implements TicketingSystem {
		
	int routenum; 
	int coachnum; 
	int seatnum; 
	int stationnum;
	int threadnum;
	static long tcount = 1;
	ReentrantLock lock;
	Ticket[][][][][]  ticketSys;
	ArrayList<Long> soldTid = new ArrayList<Long>(); //Used for replay
	static boolean debug = false;
		
	TicketingDS(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
		routenum = routeNum;
		coachnum = coachNum;
		seatnum = seatNum;
		stationnum = stationNum;
		threadnum = threadNum;
		lock = new ReentrantLock();
		ticketSys = new Ticket[routenum + 1][coachnum + 1][stationnum + 1][stationnum + 1][seatnum + 1];		
	}
	
    static  AtomicInteger rLock = new AtomicInteger(0); //Resource Lock

    static void RLOCK_TAKE() {
    	while (rLock.compareAndSet(0, 1) == false) {}
    }

    static void RLOCK_GIVE() {
        rLock.set(0);
    }

    static boolean RLOCK_TRY() {
        return (rLock.get() == 0);
    }

	
	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
		RLOCK_TAKE();
			for (int coach = 1; coach <= coachnum; coach++) {
				int retinq = inquiryCoach(route, coach, departure, arrival);
				if (retinq > 0) {
				//if (inquiryCoach(route, coach, departure, arrival) > 0) {
					//System.out.println("Debug retinq" + " " + route + " " + coach + " " + departure + " " + arrival + " " + retinq);
					Ticket ticket = new Ticket();
					ticket.passenger = passenger;
					ticket.route = route;
					ticket.departure = departure;
					ticket.arrival = arrival;		

					ticket.tid = tcount++;
					ticket.coach = coach;
					ticket.seat = allocNewSeat(route, coach, departure, arrival);
					//System.out.println("Debug seat" + " " + ticket.tid + " " + route + " " + coach + " " + departure + " " + arrival + " " + ticket.seat);
					ticketSys[route][coach][departure][arrival][ticket.seat] = ticket;
					RLOCK_GIVE();
					return ticket;	
				}
			}
			RLOCK_GIVE();
			return null;
	}
	
	public boolean buyTicketReplay(Ticket ticket) {
		RLOCK_TAKE();
			Ticket myticket = ticketSys[ticket.route][ticket.coach][ticket.departure][ticket.arrival][ticket.seat];
			if (myticket == null &&
				(inquiryCoach(ticket.route, ticket.coach, ticket.departure, ticket.arrival) > 0) 
				&& (isValidTicket(ticket) == true) 
				&& ((soldTid.contains(ticket.tid) == false))) {
				ticketSys[ticket.route][ticket.coach][ticket.departure][ticket.arrival][ticket.seat] = ticket;
				soldTid.add(ticket.tid);
				RLOCK_GIVE();
				return true;	
			}
			RLOCK_GIVE();
			return false;
	}
	
	
	
	private boolean isValidTicket(Ticket ticket) {
		int[] seatArray = new int[seatnum + 1];
		
		lock.lock();
		try {
		
			/* Find all sold tickets which are overlapped with the interval of ticket, record their seat information into seatArray */
			for (int start = 1; start < stationnum; start++) {
				for (int end = start + 1; end <= stationnum; end++) {
					if (ticket.departure < end && start < ticket.arrival ) {
						for (int i = 1; i <= seatnum; i++ ) {
							if (ticketSys[ticket.route][ticket.coach][start][end][i] != null) {
								seatArray[i] = 1;
							}
						}
					}
				}
			}
		
			
			if (seatArray[ticket.seat] == 0) { //Check whether the seat of the ticket is sold
				return true;
			}
			return false;

		} finally {
			lock.unlock();			
		}
	}

	
	
	public int inquiry(int route, int departure, int arrival) {
		RLOCK_TAKE();
			int remain = 0;
			for (int coach = 1; coach <= coachnum; coach++) {
				remain = remain + inquiryCoach(route, coach, departure, arrival);
			}
			RLOCK_GIVE();
			return remain;
	}

	private int inquiryCoach(int route, int coach, int departure, int arrival) {
		
		lock.lock();
		try {
			
			int remain = seatnum;
			for (int i = 1; i <= seatnum; i++ ) {
				boolean seatSold = false;
				for (int start = 1; start < stationnum; start++) {
					for (int end = start + 1; end <= stationnum; end++) {
						if (departure < end && start < arrival ) {
							if (ticketSys[route][coach][start][end][i] != null) {
								seatSold = true;
							}
						}
					}
				}
				if (seatSold == true) {
					if (debug) {
						System.out.println("Debug Invalid seat" + " " + i + " "+ route + " " + coach + " " + departure + " " + arrival);
						System.out.flush();
					}
					remain--;
				} else {
					if (debug) {
						System.out.println("Debug Valid seat" + " " + i + " "+ route + " " + coach + " " + departure + " " + arrival);
						System.out.flush();
					}
				}
			}
			return remain;
		} finally {
			lock.unlock();
		}
		
	}
	
	private int allocNewSeat(int route, int coach, int departure, int arrival) {
		int[] seatArray = new int[seatnum + 1];
		int[] seatSoldArray = new int[seatnum + 1];
		
		lock.lock();
		try {
		
			int sold = 1;
			for (int i = 1; i <= seatnum; i++ ) {
				boolean seatSold = false;
				for (int start = 1; start < stationnum; start++) {
					for (int end = start + 1; end <= stationnum; end++) {
						if (departure < end && start < arrival ) {
							if (ticketSys[route][coach][start][end][i] != null) {
								seatSold = true;
							}
						}
					}
				}
				if (seatSold == true) {
					seatArray[sold++] = i;
				}
			}
		
			//Arrays.sort(seatArray);
			int idx = 0;
			for (int i = 1; i <= seatnum; i++ ) {
				if (seatArray[i] != 0) {
					idx = seatArray[i];
					seatSoldArray[idx] = 1;
				}
			}
			
			int nextSeat = 0;
			for (int i = 1; i <= seatnum; i++ ) { // Find the first valid seat number
				if (seatSoldArray[i] == 0) {
					nextSeat = i;
					break;						
				}
			}
			
			if (nextSeat == 0) {
				System.out.println("Bug: allocate new seat");
				System.out.flush();
			}
			
			return nextSeat;
		} finally {
			lock.unlock();			
		}
	}

	public boolean refundTicketReplay(Ticket ticket) {
		RLOCK_TAKE();
		Ticket myticket = ticketSys[ticket.route][ticket.coach][ticket.departure][ticket.arrival][ticket.seat];
			if (myticket != null 
				&& myticket.tid == ticket.tid && myticket.passenger.equals(ticket.passenger) && myticket.route == ticket.route 
				&& myticket.coach == ticket.coach && myticket.departure == ticket.departure 
				&& myticket.arrival == ticket.arrival && myticket.seat == ticket.seat
				&& soldTid.contains(ticket.tid) == true && soldTid.indexOf(ticket.tid) == soldTid.lastIndexOf(ticket.tid)) {
			
				ticketSys[ticket.route][ticket.coach][ticket.departure][ticket.arrival][ticket.seat] = null;
				for(int i=0; i<soldTid.size(); i++) {
					if(soldTid.get(i) == ticket.tid) {
						soldTid.remove(i);
					}
				}
				RLOCK_GIVE();
				return true;
			}
			if(soldTid.indexOf(ticket.tid) != soldTid.lastIndexOf(ticket.tid)) {
				System.out.println("Bug in Replay: duplicated tid");
			}
			RLOCK_GIVE();
			return false;
	}

	

	public boolean refundTicket(Ticket ticket) {
		RLOCK_TAKE();
			Ticket myticket = ticketSys[ticket.route][ticket.coach][ticket.departure][ticket.arrival][ticket.seat];
			if (myticket != null && myticket.tid == ticket.tid && myticket.passenger.equals(ticket.passenger) && myticket.route == ticket.route 
					&& myticket.coach == ticket.coach && myticket.departure == ticket.departure 
					&& myticket.arrival == ticket.arrival && myticket.seat == ticket.seat) {
				ticketSys[ticket.route][ticket.coach][ticket.departure][ticket.arrival][ticket.seat] = null;
				RLOCK_GIVE();
				return true;
			
			}
			RLOCK_GIVE();		
			return false;
	}

}