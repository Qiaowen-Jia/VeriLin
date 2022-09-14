package ticketingsystem;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class TicketingDS implements TicketingSystem {

	private int routenum;
	private int seatnum;
	private int stationnum;
	private Route[] routes;
	//检查退票；分段锁
	private ConcurrentHashMap<Long,Ticket> bought=new ConcurrentHashMap<>();
	private AtomicLong tid=new AtomicLong(0);
	private Object refundLock=new Object();
	
	public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
		this.routenum=routenum;
		this.seatnum=seatnum;
		this.stationnum=stationnum;
		routes=new Route[routenum+1];
		for(int i=1;i<=routenum;i++) {
			routes[i]=new Route(stationnum, coachnum, seatnum);
		}
	}

	@Override
	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
		int coach,seat;
		if(check(route, departure, arrival)){
			seat=routes[route].buy(departure, arrival);
			if(seat>0) {
				Ticket ticket=new Ticket();
				//同步tid
				ticket.tid=tid.incrementAndGet();
				ticket.passenger=passenger;
				ticket.route=route;
				ticket.departure=departure;
				ticket.arrival=arrival;
				coach=seat/seatnum;
				seat=seat%seatnum;
				if(seat==0) {
					seat=seatnum;
				}else {
					coach++;
				}
				ticket.seat=seat;
				ticket.coach=coach;
				//同步bought
				bought.put(ticket.tid, ticket);
				return ticket;
			}
		}
		return null;
	}

	@Override
	public int inquiry(int route, int departure, int arrival) {
		if(check(route, departure, arrival)) {
			return routes[route].inqury(departure,arrival);
		}
		return -1;
	}

	@Override
	//同步refund-ticket
	public boolean refundTicket(Ticket ticket) {
		boolean check;
		synchronized (refundLock) {
			check=checkTicket(ticket);
		}
		if(check) {
			int seat=(ticket.coach-1)*seatnum+ticket.seat;
			routes[ticket.route].refund(seat,ticket.departure, ticket.arrival);
			return true;
		}
		return false;
	}
		
	public boolean check(int route, int departure, int arrival) {
		if(route<=0||route>routenum||
			departure<=0||departure>stationnum||
			arrival<=departure||arrival>stationnum) {
			return false;
		}
		return true;
	}
	
	public boolean checkTicket(Ticket ticket) {
		long id;
		Ticket t;
		if(ticket==null) {
			return false;
		}
		id=ticket.tid;
		if(id>tid.get()) {
			return false;
		}
		//同步bought
		if(bought.containsKey(id)) {
			t=bought.get(id);
			if(ticket.equals(t)) {
				bought.remove(id);
				return true;
			}
		}
		return false;
	}

}
