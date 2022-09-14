package ticketingsystem;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Ticket{
	long tid;
	String passenger;
	int route;
	int coach;
	int seat;
	int departure;
	int arrival;
}


/*
public interface TicketingSystem {
	Ticket buyTicket(String passenger, int route, int departure, int arrival);
	int inquiry(int route, int departure, int arrival);
	boolean refundTicket(Ticket ticket);
}
*/

class ReadWriteLock{
	int readers;
	int writers;
	Lock lock;
	Condition condition;
	ReadLock readLock;
	WriteLock writeLock;

	public ReadWriteLock(){
		writers = 0;
		readers = 0;
		lock = new ReentrantLock();
		readLock = new ReadLock();
		writeLock = new WriteLock();
		condition = lock.newCondition();
	}

	public ReadLock readlock(){
		return readLock;
	}

	public WriteLock writelock(){
		return writeLock;
	}

	class ReadLock{
		public void lock(){
			lock.lock();
			try{
				while(writers > 0){
					condition.await();
				}
				readers++;
			}catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				lock.unlock();
			}
		}

		public void unlock(){
			lock.lock();
			try{
				readers--;
				if(readers == 0)
					condition.signalAll();
			}finally{
				lock.unlock();
			}
		}
	}

	class WriteLock {
		public void lock(){
			lock.lock();
			try{
				while(readers > 0){
					condition.await();
				}
				writers++;
			}catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				lock.unlock();
			}
		}

		public void unlock(){
			lock.lock();
			try{
				writers--;
				if(writers == 0)
					condition.signalAll();
			}finally{
				lock.unlock();
			}
		}
	}
}

class Seat{
	public int seatId;
	public AtomicInteger stationInfo;

	public Seat(int seatId){
		this.seatId = seatId;
		this.stationInfo = new AtomicInteger(0);
	}

	public int trySealThisSeat(int dep, int arr){

		int newInfo = 0;
		int oldInfo = this.stationInfo.get();
		int tempInfo = 0;

		int pow = 1 << dep;
		int n = arr - dep;
		for(int i = 0; i < n; i++){
			tempInfo |= pow;
			pow = pow << 1;
		}

		int flag;

		do{
			oldInfo = this.stationInfo.get();
			flag = tempInfo & oldInfo;
			if(flag != 0)
			{
				return -1;
			}
			else{
				newInfo = tempInfo | oldInfo;
			}
		}while(!this.stationInfo.compareAndSet(oldInfo, newInfo));

		return this.seatId;
	}

	public void tryRefundThisSeat(int dep, int arr){
		int newInfo = 0;
		int oldInfo = this.stationInfo.get();
		int tempInfo = 0;

		int pow = 1 << dep;
		int n = arr - dep;
		for(int i = 0; i < n; i++){
			tempInfo |= pow;
			pow = pow << 1;
		}
		
		tempInfo = ~tempInfo;
		
		do{
			oldInfo = this.stationInfo.get();
			newInfo = tempInfo & oldInfo;	
		}while(!this.stationInfo.compareAndSet(oldInfo, newInfo));
	}

	public int inquiryThisSeat(int dep, int arr){
		int oldInfo = this.stationInfo.get();

		int tempInfo = 0;

		int pow = 1 << dep;
		int n = arr - dep;
		for(int i = 0; i < n; i++){
			tempInfo |= pow;
			pow = pow << 1;
		}
		
		int res = tempInfo & oldInfo;
		return (res == 0) ? 1 : 0;
	}

}

class Route{
	public int routeId;
	public int coachNum;
	public int seatNumEveryCoach;
	public int seatNum;
	public ArrayList<Seat> seatList;
	public AtomicLong tickedCount;
	public ConcurrentHashMap<Long, Ticket> selledTicketMap = new ConcurrentHashMap<Long, Ticket>();

	public ReadWriteLock readWriteLock;

	public Route(int routeId, int coachNum, int seatNumEveryCoach)
	{
		this.routeId = routeId;
		this.coachNum = coachNum;
		this.seatNumEveryCoach = seatNumEveryCoach;
		this.seatNum = coachNum * seatNumEveryCoach;
		this.tickedCount = new AtomicLong(0);
		this.readWriteLock = new ReadWriteLock();

		this.seatList = new ArrayList<Seat>(this.seatNum);

		for(int i = 0; i < this.seatNum; ++i){
			this.seatList.add(new Seat(i));
		}
	}
	
	public Ticket trySealTic(String passenger, int dep, int arr){
		int randSeat = ThreadLocalRandom.current().nextInt(this.seatNum);
		int seatGlobalId ;
		this.readWriteLock.readLock.lock();
		try{
			for(int i = 0; i < this.seatNum; ++i){
				seatGlobalId = this.seatList.get(randSeat).trySealThisSeat(dep, arr);
				if(seatGlobalId >=0 )
				{
					Ticket ticket = new Ticket();
					ticket.tid = this.routeId*10000000 + this.tickedCount.getAndIncrement();
					ticket.passenger = passenger;
					ticket.route = this.routeId;
					ticket.coach = seatGlobalId / seatNumEveryCoach + 1;
					ticket.seat = seatGlobalId % seatNumEveryCoach + 1;
					ticket.departure = dep;
					ticket.arrival = arr;

					Ticket ticketCopy = new Ticket();
					ticketCopy.tid = ticket.tid;
					ticketCopy.passenger = ticket.passenger;
					ticketCopy.route = this.routeId;
					ticketCopy.coach = ticket.coach;
					ticketCopy.seat = ticket.seat;
					ticketCopy.departure = dep;
					ticketCopy.arrival = arr;

					selledTicketMap.put(ticketCopy.tid, ticketCopy);

					return ticket;
				}
				
				randSeat = (randSeat+1) % this.seatNum;
			}
			//this.readWriteLock.writeLock.unlock();
			return null;
		}finally{
			this.readWriteLock.readLock.unlock();
		}
	}

	public boolean tryRefundTic(Ticket ticket){
		long tempTid = ticket.tid;
		boolean flag = false;
		Ticket tempTicket = this.selledTicketMap.get(tempTid);

		if(tempTicket == null) return false;

		int tempRoute = tempTicket.route;
		int tempCoach = tempTicket.coach;
		int tempSeat = tempTicket.seat;
		int tempDeparture = tempTicket.departure;
		int tempArrival = tempTicket.arrival;
		

		if((ticket.passenger.equals(tempTicket.passenger))
			&&(tempTicket.route == ticket.route)&&(tempCoach==ticket.coach)
			&&(tempSeat == ticket.seat)&&(tempDeparture == ticket.departure)&&(tempArrival == ticket.arrival))
			flag = true;

		if(flag){
			if(this.selledTicketMap.remove(tempTid) != null){
				this.readWriteLock.writeLock.lock();
				try{
				int tempSeatId = (ticket.coach-1)*this.seatNumEveryCoach + (ticket.seat-1); 
				this.seatList.get(tempSeatId).tryRefundThisSeat(ticket.departure, ticket.arrival);
				return true;
				}finally{
				this.readWriteLock.writeLock.unlock();
				}
			}
			else{
				return false;
			}
		}
		else{
			return false;
		}
	}

	public int inquiryTic(int dep, int arr) {
		//int randSeat = ThreadLocalRandom.current().nextInt(this.seatNum);
		int countSeat = 0;
		this.readWriteLock.readLock.lock();
		try{
		for(int i = 0; i < this.seatNum; ++i){
			countSeat += this.seatList.get(i).inquiryThisSeat(dep, arr);
			//randSeat = (randSeat + 1) % this.seatNum;
		}
		return countSeat;
		}finally{
			this.readWriteLock.readLock.unlock();
		}
	}

}

//class TicketingDS implements TicketingSystem{
class TicketingDS {
	public int routeNum;
	public int stationNum;
	public int coachNum;
	public int seatNumEveryCoach;
	public ArrayList<Route> routeList;
	
	public int threadNum;
	public TicketingDS(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum)
	{
		this.routeNum = routeNum;
		this.stationNum = stationNum;
		this.coachNum = coachNum;
		this.seatNumEveryCoach = seatNum;

		this.threadNum = threadNum;

		this.routeList = new ArrayList<Route>(routeNum);
		for(int i = 1; i <= routeNum; ++i){
			this.routeList.add(new Route(i, coachNum, seatNum));
		}
	}

	public Ticket buyTicket(String passenger, int route, int departure, int arrival){
		if(route < 1 || route > this.routeNum || arrival > this.stationNum || departure > arrival || departure <= 0)
			return null;
		return this.routeList.get(route-1).trySealTic(passenger, departure, arrival);
	}

	public boolean refundTicket(Ticket ticket){
		if(ticket == null || ticket.route < 1 || ticket.route > this.routeNum)
			return false;
		return this.routeList.get(ticket.route-1).tryRefundTic(ticket);
	}

	public int inquiry(int route, int departure, int arrival){
		if(route < 1 || route > this.routeNum || arrival > this.stationNum || departure > arrival || departure <= 0)
			return 0;
		return this.routeList.get(route-1).inquiryTic(departure, arrival);
	}

}

