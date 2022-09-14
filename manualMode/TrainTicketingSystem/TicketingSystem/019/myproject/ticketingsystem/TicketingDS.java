package ticketingsystem;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TicketingDS implements TicketingSystem {
	Lock [][]locks;

	int threadnum = 4;
	int routenum = 3; // route is designed from 1 to 3
	int coachnum = 5; // coach is arranged from 1 to 5
	int seatnum = 20; // seat is allocated from 1 to 20
	int stationnum = 5; // station is designed from 1 to 5

	Train[] trains;
	AtomicLong global_tid = new AtomicLong(0);
	ConcurrentHashMap<Long,Ticket> soldTickets;

	public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum){
		this.routenum = routenum;
		this.coachnum = coachnum;
		this.seatnum = seatnum;
		this.stationnum = stationnum-1;
		this.threadnum = threadnum;
		trains = new Train[routenum];
		locks = new ReentrantLock[routenum][stationnum];
		for(int i=0;i<this.routenum;i++){
			trains[i] = new Train(seatnum,coachnum,stationnum);
			for(int j=0;j<stationnum;j++)
				locks[i][j] = new ReentrantLock();
		}
		soldTickets = new ConcurrentHashMap<Long,Ticket>();
	}

	public TicketingDS(){
		trains = new Train[routenum];
		for(int i=0;i<this.routenum;i++){
			trains[i] = new Train(seatnum,coachnum,stationnum);
		}
		soldTickets = new ConcurrentHashMap<Long,Ticket>();
	}

	public void lockStation(int route,int departure,int arrival){
		for(int i=departure;i<=arrival;i++) locks[route][i].lock();
	}
	public void unlockStation(int route,int departure,int arrival){
		for(int i=departure;i<=arrival;i++) locks[route][i].unlock();
	}	

	public Ticket buyTicket(String passenger, int route, int departure, int arrival){
		int seat_position =-1;
		lockStation(route-1,departure-1,arrival-2);
		try{
			if(trains[route-1].findSeat(departure-1, arrival-2)<=0) return null;
			seat_position = trains[route-1].sellSeat(departure-1,arrival-2);
		}finally{
			unlockStation(route-1,departure-1,arrival-2);
		}
		Ticket t = null;
		t = new Ticket();
		t.passenger = passenger;
		t.route = route;
		t.departure = departure;
		t.arrival = arrival;
		t.coach = seat_position/seatnum+1;
		t.seat = seat_position%seatnum+1;
		t.tid = global_tid.getAndIncrement();
		soldTickets.put(t.tid, t);
		return t;
	}
	public int inquiry(int route, int departure, int arrival)
	{
		int remain_seats = trains[route-1].findSeat(departure-1,arrival-2);
		return remain_seats;
	}
	public boolean refundTicket(Ticket ticket){
		if( !soldTickets.containsKey(ticket.tid) || !ticket.equals(soldTickets.get(ticket.tid))){
            return false;
		}
		lockStation(ticket.route-1,ticket.departure-1,ticket.arrival-2);
		try{
			if(trains[ticket.route - 1].spareSeat(ticket)){
				return soldTickets.remove(ticket.tid, ticket);
			}
		}finally{
			unlockStation(ticket.route-1,ticket.departure-1,ticket.arrival-2);
		}
        return false;
	}//ToDo
	
	public void printInfo() {
		for(int i=0;i<routenum;i++){
			System.out.println(i+"th train.");
			System.out.println();
			for(int k=0;k<stationnum;k++){
				for(int j=0;j<coachnum*seatnum;j++){
					System.out.print(trains[i].seat_state_in_station[k][j].get()+" ");
				}
				System.out.println();
			}
		}
		for(int i=0;i<routenum;i++){
			System.out.println(i+"th train.");
			System.out.println();
			for(int k=0;k<stationnum;k++){
				for(int j=0;j<stationnum;j++){
					System.out.print(trains[i].seat_reamin_between_station[k][j].get()+" ");
				}
				System.out.println();
			}
		}
	}

}
