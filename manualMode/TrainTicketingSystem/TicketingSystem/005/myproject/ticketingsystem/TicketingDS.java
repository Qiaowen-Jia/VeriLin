package ticketingsystem;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TicketingDS implements TicketingSystem {
    int routenum;//车次总数
    int coachnum;//列车的车厢数目
    int seatnum;//每节车厢的座位数
    int stationnum;//是每个车次经停站的数量
    int threadnum;//并发购票的线程数
    Integer  buyerlock = new Integer(0); //读写锁控制写车
    Train trains[];
    AtomicInteger ticketID = new AtomicInteger(0);
    public TicketingDS() {
    	routenum = 5;
    	coachnum = 8;
    	seatnum = 100;
    	stationnum = 10;
    	threadnum = 16;
    	trains = new Train[routenum+1];
    	for(int i=0; i<=routenum; i++)
    		trains[i] = new Train(i, coachnum, seatnum, stationnum);
    	
    }
	public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
		this.routenum = routenum;
		this.coachnum = coachnum;
		this.seatnum = seatnum;
		this.stationnum = stationnum;
		this.threadnum = threadnum;
		trains = new Train[routenum+1];
    	for(int i=0; i <= routenum; i++) {
    		trains[i] = new Train(i, coachnum, seatnum, stationnum);
    	}
    	
	}
	
	@Override
	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
		// TODO Auto-generated method stub
		//乘客passenger购买route车次，从departure到arrival一张，购票不成功返回null。车票唯一tid,用一个incsy实现。
		
		Ticket ans  = null;
		//synchronized(buyerlock) {
           long tid = ticketID.incrementAndGet();
	        ans = trains[route].getTicket(tid, passenger, departure, arrival);
		//}
		return ans;
		

	}
	@Override
	public int inquiry(int route, int departure, int arrival) {
		// TODO Auto-generated method stub
		//查询余票，查询route车次从departure到arrival站余票数
		//该操作占70% 所以可以直接，车厢，后期加速操作我可以用区间最大值的线段树来整。但是并发emmm。
		//System.out.println("Inquiry thread " + ThreadId.get() + " train "+route + " from " + departure + " to " + arrival);
	     int ans = 0;
		 ans = trains[route].inquiry(route, departure, arrival);
	   
		return ans;
	}

	@Override
	public boolean refundTicket(Ticket ticket) {
		// TODO Auto-generated method stub
		//退票10%，有效ticket返回true，否则false
		 boolean ans = true;
		//synchronized(buyerlock) {
	      
		   if(!trains[ticket.route].hasTicket(ticket))
			 ans = false;
		   if(ans)
			   trains[ticket.route].refundTicket(ticket);
		//}
	     
	  
		return ans;
	}
		
	//ToDo

}
