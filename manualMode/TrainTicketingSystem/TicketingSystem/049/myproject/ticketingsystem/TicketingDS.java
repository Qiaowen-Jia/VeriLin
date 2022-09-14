package ticketingsystem;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TicketingDS implements TicketingSystem {
	public class seat{
		boolean[] state;
		int stationnum;

		public seat(int stationnum){
			//座位初始化，n个站有n-1个区间，使用下标1...n-1表示，state[i] =true表示i->i+1区间可用
			this.stationnum =stationnum;
			this.state=new boolean[stationnum+1];
			for(int i=0;i<=stationnum;i++){
				state[i]=true;
			}
		}
		public boolean buyseat(int departure, int arrival,int []tc) {
			lock.lock();
			try {
				for(int i=departure; i<arrival; i++)
					if(state[i] == false) return false;
				for(int i=departure; i<arrival; i++)
					state[i] = false;
				for(int i=departure-1;i>0;i--)
					if(state[i] == false) {tc[0] = i+1;break;}
				for(int i=arrival;i<stationnum;i++) 
					if(state[i] == false) {tc[1] =i;break;}
			}finally {
				lock.unlock();
			}
			return true;
		}
		public void reseat(int departure, int arrival,int [] tc ) {
			lock.lock();
			try{
				for(int i=departure; i<arrival; i++)
					state[i] = true;
				for(int i=departure-1;i>0;i--)
					if(state[i] == false) {tc[0] = i+1;break;}
				for(int i=arrival;i<stationnum;i++) 
					if(state[i] == false) {tc[1] = i;break;}
		}finally {
			lock.unlock();}
		}
	}
	private int [][][] itl;
	private seat[][] Seat;	
	AtomicLong tid;
	Lock lock = new ReentrantLock();
	//ToDo
	private int routenum,coachnum,seatnum,stationnum,threadnum;
	public TicketingDS (int routenum ,int coachnum ,int seatnum ,int stationnum ,int threadnum){
		
		this.routenum=routenum;//车次
		this.coachnum=coachnum; //车厢数
		this.seatnum=seatnum;//座位数
		this.stationnum=stationnum;//站数
		this.threadnum=threadnum;//线程数
		tid = new AtomicLong(1);
		itl = new int [routenum+1][stationnum+1][stationnum+1];// itl 即inquirytable
		//存储从X->Y所剩余的票数，这样预查询时，就需要遍历全部seat
		for(int i =0;i<= routenum;i++)
			for(int j =0;j<= stationnum ;j++)
				for(int k =0;k<= stationnum;k++)
					if(k<=j) itl[i][j][k] = 0;//不存在从后面站到前面站的车票。-1表示非法
					else itl[i][j][k] = coachnum*seatnum;//初始状态全票都有
		Seat = new seat[routenum+1][coachnum*seatnum];
		//使用一个二维数组存储所有的座位信息，coach号可以通过一维下标进行整除操作获得 0.。s-1 为1号车厢  s....2s-1为2号车厢
		for(int i=1;i<=routenum;i++) {
			for(int j=0;j<coachnum*seatnum;j++) {
				Seat[i][j]=new seat(stationnum);
			}
		}	
	}
	
	
	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
		if( inquiry(route,departure,arrival) == 0 ) return null;
		Ticket ticket = new Ticket();
		int i;
		int tc[] ={1,stationnum};
		for( i = 0;i<coachnum*seatnum;i++) 
			if(Seat[route][i].buyseat(departure, arrival,tc)) 
				break;
		if(i == coachnum*seatnum) return null;	
		
		ticket.route =route;
		ticket.tid = tid.getAndIncrement();
		ticket.arrival =arrival;
		ticket.departure = departure;
		ticket.coach = i/seatnum +1;
		ticket.seat = i - seatnum * (i/seatnum)+1;
		ticket.passenger=passenger;
		
		lock.lock();
		try {
		for( i = tc[0];i<arrival;i++)
			for(int j = i+1;j<=tc[1];j++)
				if(j>departure) itl[route][i][j]--;
		}finally {
			lock.unlock();
		}
		return ticket;
		
	}
	
	public int inquiry(int route, int departure, int arrival) {
		//查询只要求静态一致性，只需要查询itl即可
		 return itl[route][departure][arrival];
		
	}
	public boolean refundTicket(Ticket ticket) {
		int tc[] ={1,stationnum};
		int s = ticket.seat+(ticket.coach-1)*seatnum -1;
		Seat[ticket.route][s].reseat(ticket.departure, ticket.arrival,tc);
		lock.lock();
		try {
		for(int i = tc[0];i<ticket.arrival;i++)
			for(int j = i+1;j<=tc[1];j++)
				if(j>ticket.departure) itl[ticket.route][i][j]++;
		}finally {
			lock.unlock();
		}
		return true;
		
	}
}

