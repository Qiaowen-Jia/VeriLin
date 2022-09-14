package ticketingsystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TicketingDS implements TicketingSystem{
	
	protected List<Ticket>[] route;
	private int Routenum, Coachnum, Seatnum, Stationnum, Threadnum;
	private AtomicLong tid_num;
	
	private boolean[][][][] seat_station;
	
	private ReadWriteLock[][][] table_lock;
	private Lock[] list_lock;
	private void RouteSet() {
		route=(List<Ticket>[]) new List[Routenum];
		seat_station=new boolean[Routenum][Coachnum][Seatnum][Stationnum];
		list_lock=new Lock[Routenum];
		table_lock=new ReadWriteLock[Routenum][Coachnum][Seatnum];
		
		tid_num = new AtomicLong();
		for(int i=0;i<Routenum;i++) {
			route[i]=new ArrayList<Ticket>();
			list_lock[i]=new ReentrantLock();
		}
		for(int i=0;i<Routenum;i++) {
			for(int j=0;j<Coachnum;j++) {
				 for(int k=0;k<Seatnum;k++) {
					 table_lock[i][j][k]=new ReentrantReadWriteLock();
					 for(int s=0;s<Stationnum;s++) {
						 seat_station[i][j][k][s]=true;
					 }
				 }
			}
		}
	}
	
	public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum,int threadnum) {
		Routenum = routenum;
		Coachnum = coachnum;
		Seatnum = seatnum;
		Stationnum = stationnum;
		Threadnum = threadnum;
		RouteSet();
	}
	
	
	
	public List<Ticket> getRoute(int i) {
		return route[i-1];
	}
	


	
	public Ticket buyTicket(String passenger,int route,int departure,int arrival) {

		Ticket t=new Ticket();
		boolean flag=true;
		int seat=0,coach=0;
		for(int j=0;j<Coachnum;j++) {
			 for(int k=0;k<Seatnum;k++) {
				 flag=true;
				 for(int s=departure-1;s<arrival;s++) {
					 if(seat_station[route-1][j][k][s]==false) {
						 flag=false;
				     }
					 if(s==arrival-1&&flag==true) {
						 table_lock[route-1][j][k].writeLock().lock();
						 try {
						 for(int b=departure-1;b<arrival;b++) {
							 
								 seat_station[route-1][j][k][b]=false;
					    } }finally {
							 table_lock[route-1][j][k].writeLock().unlock();
					     }
						 seat=k+1;
						 coach=j+1;
			        }
		       }
				 if(flag==true) {
					 break;
				 }
		}
			 if(flag==true) {
				 break;
			 }
	}
		
		if(flag==true) {
		list_lock[route-1].lock();
		try {
			t.tid=tid_num.incrementAndGet();
			t.passenger=passenger;
			t.route=route;
			t.coach=coach;
			t.seat=seat;
			t.departure=departure;
			t.arrival=arrival;
			getRoute(route).add(t);
		}finally {
		   list_lock[route-1].unlock();
	   }
	  }
		else {
			t=null;
		}
		return t;
    }
	
	
	public int inquiry(int route,int departure,int arrival) {
		
		AtomicInteger count=new AtomicInteger();
		count.set(Seatnum*Coachnum);
	        
	        	for(int j=0;j<Coachnum;j++) {
	        		for(int k=0;k<Seatnum;k++) {
	        			for(int s=departure-1;s<arrival;s++) {
	        				if(seat_station[route-1][j][k][s]==false) {
	        					count.getAndDecrement();
	        					break;
	        				}
	        			}
	        		}
	        	}
	        
		
        return count.get();
    }
	
	
	public boolean refundTicket(Ticket ticket) {

		
			boolean flag=true;
			list_lock[ticket.route-1].lock();
			try {
			      flag=getRoute(ticket.route).remove(ticket);
			}finally {
				list_lock[ticket.route-1].unlock();
			}
			if(flag==true) {
				table_lock[ticket.route-1][ticket.coach-1][ticket.seat-1].writeLock().lock();
				try {
				for(int s=ticket.departure-1;s<ticket.arrival;s++) {
						seat_station[ticket.route-1][ticket.coach-1][ticket.seat-1][s]=true;
				}}finally {
					table_lock[ticket.route-1][ticket.coach-1][ticket.seat-1].writeLock().unlock();
				}
			}
			return flag;
			
	}
			
}



