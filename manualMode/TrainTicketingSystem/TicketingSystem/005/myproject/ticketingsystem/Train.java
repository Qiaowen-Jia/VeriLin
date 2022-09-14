package ticketingsystem;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicStampedReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;



public class Train {
    int id;//车次
    int coachnum;//列车的车厢数目
    int seatnum;//每节车厢的座位数
    int totalSeatnum;
    int stationnum;
    int nowMaxSeat;//当前最大座位号
    ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();
    ReentrantLock  ticketLock = new ReentrantLock (); //锁
    int hasPassager[];//转成2进制，i位0表示空，i位1表示有人
    public Train(int id, int coachnum, int seatnum, int stationnum) {
    	this.id = id;
    	this.coachnum = coachnum;
    	this.seatnum = seatnum;
    	this.stationnum = stationnum;
    	this.nowMaxSeat = 0;
    	this.totalSeatnum = coachnum * seatnum;
    	hasPassager = new int[totalSeatnum + 1];
    	for(int i=0; i<=coachnum * seatnum; i++) {
    		hasPassager[i] = 0;	

    	}
    }
    int getBinary(int departure, int arrival) {
    	int seatBinary = (1<<arrival) - 1;
  		seatBinary = seatBinary ^ ((1<<departure) - 1); //现在转为二进制只有(arrival-1 -> departure位为1了）
  		return seatBinary;
	}
    public Ticket getTicket(long tid, String passenger, int departure, int arrival ) {
    	//之后可以给退票单独建一个队列，先从里面拿。
    	int newSeatNum = getSeat(departure, arrival);
    	if(newSeatNum == -1) {
    		return null;
    	}
    	int coach = (int) ((newSeatNum - 1) / seatnum + 1);
    	int seat = (int) ((newSeatNum - 1) % seatnum + 1);
    	Ticket ticket = new MyTicket(tid, passenger, id, coach ,seat,departure, arrival);
    	try {
         	ticketLock.lock();	
    	    soldTicket.add(ticket); //加票
    	} finally { 
        	ticketLock.unlock();
    	}
    	return ticket;
    }
    private synchronized int getSeat(int departure, int arrival) { //遍历每个位子，看看哪个位子在当前区间是空的
      	for(int i = 1 ; i<= coachnum * seatnum; i++) { 
      		boolean hasFound = false;
      		int seatBinary = getBinary(departure, arrival); //现在转为二进制只有(arrival-1 -> departure位为1了）
      		   if((hasPassager[i] & seatBinary) == 0){
      			//找到了，占座！
      			hasPassager[i] = hasPassager[i] | seatBinary;
      		    hasFound = true;
      		    nowMaxSeat =  Math.max(nowMaxSeat, i);
      		   }

      		if(hasFound)
      			return i;
      	}
      	return -1;
      
    }
    public synchronized void refundTicket(Ticket ticket){
    	int seati = (ticket.coach - 1 ) * this.seatnum + ticket.seat;
    	int seatBinary = getBinary(ticket.departure, ticket.arrival); 
    	//seatLock[seati].writeLock().lock(); 
    	hasPassager[seati] = hasPassager[seati] - seatBinary;   //退票
    	//seatLock[seati].writeLock().unlock(); 
    }
    public int inquiry(int route, int departure, int arrival) {
    	//int ans = tree.quiry(1, departure, arrival-1);ans = coachnum * seatnum - ans;
    	int ans = 0;
    	for(int i = 1; i <= this.nowMaxSeat; i++) {
    		long seatBinary =  getBinary(departure, arrival);
     		 if((hasPassager[i] & seatBinary) > 0){   //不需要加锁
     			ans ++;
      		 }
    	}
    	ans = coachnum * seatnum - ans;
    	return Math.max(ans, 0);
    }
    public  boolean hasTicket(Ticket ticket) {
    	boolean has = false;
    	try {
    	  ticketLock.lock();
    	  for(Ticket tick : soldTicket) {
    		 if(tick.equals(ticket)) {
    			has = soldTicket.remove(tick);  //移除，有没有
    			break;
    		}
    	  }
    	}finally{
    	     ticketLock.unlock();
    	}
    	return has;
    }
}
