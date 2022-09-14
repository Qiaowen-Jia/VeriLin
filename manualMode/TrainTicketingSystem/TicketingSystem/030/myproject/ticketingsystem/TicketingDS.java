package ticketingsystem;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TicketingDS implements TicketingSystem {
	
	private final ReentrantReadWriteLock[] locks;
	
	private int[][] remainTickets;
	private int seatNum;
	private int stationNum;
	//private int curr_seatNum;
	private AtomicLong curr_tid;
	private ArrayList<Ticket>[] sold_ticket;
	
	
	private int[] ticketsNum;
	
	
	
	
	@SuppressWarnings("unchecked")
	public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
		
		this.locks = new ReentrantReadWriteLock[routenum];
		for(int k = 0;k<locks.length;k++) {
			locks[k] = new ReentrantReadWriteLock();
		}
		
		this.seatNum =seatnum;
		this.stationNum = stationnum;
		//this.curr_seatNum = 1;
		this.curr_tid = new AtomicLong(1);
		//鍒濆鍖栧凡鍗栫エ浜岀淮鏁扮粍
		this.sold_ticket = (ArrayList<Ticket>[]) new ArrayList[routenum];
		for (int i = 0; i < routenum; i++) {
			this.sold_ticket[i] = new ArrayList<Ticket>();
		}
		this.remainTickets = new int[routenum*(stationnum*(stationnum-1)/2)][coachnum*seatnum];
		for(int i = 0;i<routenum*(stationnum*(stationnum-1)/2);i++) {
			for(int j = 0;j<coachnum*seatnum;j++) {
				this.remainTickets[i][j]=0;
			}
		}
		
		
		
		this.ticketsNum = new int[routenum*(stationnum*(stationnum-1)/2)];
		for(int i=0;i<routenum*(stationnum*(stationnum-1)/2);i++) {
			this.ticketsNum[i]=coachnum*seatnum;
		}
	}
	
	
	//鍒ゆ柇涓ゅ紶绁ㄦ槸鍚︾浉绛�
    private final boolean ticketEquals(Ticket x, Ticket y) {
        if(x == y) return true;
        if(x == null || y == null) return false;
        
        return( 
            (x.tid == y.tid)                    &&
            (x.passenger.equals(y.passenger))   &&
            (x.route == y.route)                &&
            (x.coach == y.coach)                &&
            (x.seat == y.seat)                  &&
            (x.departure == y.departure)        &&
            (x.arrival == y.arrival)
        );
    }
	
	
	
	//鎵惧尯闂村搴旂殑妯潗鏍�
	private int findIndex(int route, int departure, int arrival) {
		return (route-1)*(this.stationNum*(this.stationNum-1)/2)
                +(((this.stationNum-1)+(this.stationNum-(departure-1)))*(departure-1)/2+(arrival-departure)-1);
	}
	//鏍规嵁杩炵画搴т綅鍙锋眰搴旇鍒嗛厤鐨勮溅鍘㈠彿鍜屽骇浣嶅彿
	private int[] findCoachAndSeat(int seatNum) {
		int[] coachAndSeat = new int[2];
		int shang = seatNum / this.seatNum;
		int yushu = seatNum % this.seatNum;
		if(yushu == 0) {
			coachAndSeat[0] = shang;
			coachAndSeat[1] = this.seatNum;
		}else {
			coachAndSeat[0] = shang + 1;
			coachAndSeat[1] = yushu;
		}
		return coachAndSeat;
	}
	
	//鏍规嵁杞﹀帰鍙峰拰搴т綅鍙锋壘鍒板搴旂殑杩炵画搴т綅鍙�
	/*private int findSeatNum(int coach, int seat) {
		if(seat==this.seatNum) {
			return (coach-1)*this.seatNum+seat-1;
		}else {
			
		}
	}*/

	//鎵�鏈夊啿绐佺殑鍖洪棿浣欑エ鏁伴兘+1鐨勫嚱鏁�(涔扮エ鏃惰皟鐢�)
	private void remainIncrement(int route, int departure,int arrival,int seat) {
		for(int i = 1;i<this.stationNum;i++) {
			for(int j =i+1;j<=this.stationNum;j++) {
				if( !(j <= departure || i>=arrival) ) {
					if(this.remainTickets[findIndex(route,i,j)][seat]==0) {
						this.ticketsNum[findIndex(route,i,j)]--;
					}
					this.remainTickets[findIndex(route,i,j)][seat]++;
				}
			}
		}
	}
	
	//鎵�鏈夊啿绐佸尯闂寸エ鏁伴兘-1鐨勫嚱鏁�(閫�绁ㄦ椂璋冪敤)
	private void remainDecrement(int route, int departure,int arrival,int seat) {
		for(int i = 1;i<this.stationNum;i++) {
			for(int j =i+1;j<=this.stationNum;j++) {
				if( !(j <= departure || i>=arrival) ) {
					this.remainTickets[findIndex(route,i,j)][seat]--;
					if(this.remainTickets[findIndex(route,i,j)][seat]==0) {
						this.ticketsNum[findIndex(route,i,j)]++;
					}
				}
			}
		}
	}
	
	
	/*private void Increment(int route, int departure,int arrival) {
		for(int i = 1;i<this.stationNum;i++) {
			for(int j =i+1;j<=this.stationNum;j++) {
				if( !(j <= departure || i>=arrival) ) {
					this.ticketsNum[findIndex(route,i,j)]++;
				}
			}
		}
	}
	
	private void Decrement(int route, int departure,int arrival) {
		for(int i = 1;i<this.stationNum;i++) {
			for(int j =i+1;j<=this.stationNum;j++) {
				if( !(j <= departure || i>=arrival) ) {
					this.ticketsNum[findIndex(route,i,j)]--;
				}
			}
		}
	}*/
	
	@Override
	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
		//this.locks[route-1].writeLock().lock();
		try {
			for(int i = 0;i<this.remainTickets[findIndex(route, departure, arrival)].length;i++) {
				if(this.remainTickets[findIndex(route, departure, arrival)][i]==0) {
					Ticket ticket = new Ticket();
					ticket.tid = this.curr_tid.getAndIncrement();
					ticket.passenger = passenger;
					ticket.route = route;
					ticket.departure = departure;
					ticket.arrival = arrival;
					ticket.coach = findCoachAndSeat(i+1)[0];
					ticket.seat = findCoachAndSeat(i+1)[1];
					this.locks[route-1].writeLock().lock();
					if(this.remainTickets[findIndex(route, departure, arrival)][i]==0) {
						remainIncrement(route, departure, arrival,i);
						
						
						//对应区间余票数减一
						//this.ticketsNum[findIndex(route, departure, arrival)]--;
						//Decrement(route, departure, arrival);
						
						
						this.sold_ticket[route-1].add(ticket);
						//this.curr_seatNum++;
						this.locks[route-1].writeLock().unlock();
						return ticket;
					}else {
						this.locks[route-1].writeLock().unlock();
					}
				}
			}
			return null;
		}finally {
			//this.locks[route-1].writeLock().unlock();
		}
	}

	@Override
	public int inquiry(int route, int departure, int arrival) {
		this.locks[route-1].readLock().lock();
		try {
			/*
			int flag = 0;
			for(int i = 0;i<this.remainTickets[findIndex(route, departure, arrival)].length;i++) {
				if(this.remainTickets[findIndex(route, departure, arrival)][i]==0) {
					flag++;
				}
			}
			return flag;
			*/
			return this.ticketsNum[findIndex(route, departure, arrival)];
		}finally {
			this.locks[route-1].readLock().unlock();
		}
	}

	@Override
	public boolean refundTicket(Ticket ticket) {
		this.locks[ticket.route-1].writeLock().lock();
		try {
			for(int i = 0;i<this.sold_ticket[ticket.route-1].size();i++) {
				if(ticketEquals(ticket, this.sold_ticket[ticket.route-1].get(i))) {
					this.sold_ticket[ticket.route-1].remove(i);
					remainDecrement(ticket.route, ticket.departure, ticket.arrival, (ticket.coach-1)*this.seatNum+ticket.seat-1);
					
					//对应区间余票数加一
					/*if(this.remainTickets[findIndex(ticket.route, ticket.departure, ticket.arrival)][(ticket.coach-1)*this.seatNum+ticket.seat-1]==0) {
						//this.ticketsNum[findIndex(ticket.route, ticket.departure, ticket.arrival)]++;
						Increment(ticket.route, ticket.departure, ticket.arrival);
					}*/
					
					return true;
				}
			}
			return false;
		}finally {
			this.locks[ticket.route-1].writeLock().unlock();
		}
	}
		
	//ToDo

}
