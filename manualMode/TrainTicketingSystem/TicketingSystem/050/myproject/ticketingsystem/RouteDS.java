package ticketingsystem;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.*;


public class RouteDS{

	public int route, coachnum, seatnum, stationnum, rangenum;
	
	public ArrayList<ConcurrentHashMap<Long, Ticket>> soldTickets;
	public AtomicInteger[] timeStamps;
	
	protected AtomicInteger[][] rangeStates;

	//protected AtomicInteger[][][] rangeStates;
	
	protected Lock rangeLock;
	//protected AtomicIntegerArray[] seatStates;
	protected Tuple[] seatTuples; 
	protected ArrayList<LinkedList<Tuple>> rangeSeatList;
	protected Lock[] rangeSeatLocks;
	
	protected ReentrantReadWriteLock[][] rwl;
	//protected StampedLock[][] rwl;
	//protected readWriteLock[][] rwl;

	public RouteDS(int route, int coachnum, int seatnum, int stationnum){
		this.route = route;
		this.coachnum = coachnum;
		this.seatnum = seatnum;
		this.stationnum = stationnum;
		this.rangenum = ((stationnum-1)*stationnum)/2;
		
		// hashmap初始化
		this.soldTickets = new ArrayList<ConcurrentHashMap<Long, Ticket>>();
		this.timeStamps = new AtomicInteger[coachnum + 1];
		for(int i =0; i <= coachnum; i++){
			this.soldTickets.add(new ConcurrentHashMap<Long, Ticket>((stationnum - 1) * seatnum));
			this.timeStamps[i] = new AtomicInteger(1);
		}
		
		
		// 区间表初始化
		this.rwl = new ReentrantReadWriteLock[stationnum + 1][stationnum + 1];
		//this.rwl = new StampedLock[stationnum + 1][stationnum + 1];
		//this.rwl = new readWriteLock[stationnum + 1][stationnum + 1];
		
		this.rangeStates = new AtomicInteger[stationnum + 1][stationnum + 1];
		for(int i = 1; i < stationnum; i++){
			for (int j = i+1; j <= stationnum; j++) {
				rangeStates[i][j] = new AtomicInteger(coachnum * seatnum);
				rwl[i][j] = new ReentrantReadWriteLock(true);
				//rwl[i][j] = new StampedLock();
				//rwl[i][j] = new readWriteLock();
			} 
		}

		/*
		this.rwl = new ReentrantReadWriteLock[stationnum + 1][stationnum + 1];
		this.rangeStates = new AtomicInteger[coachnum + 1][stationnum + 1][stationnum + 1];
		for(int k = 1; k < coachnum + 1; k++){
			for(int i = 1; i < stationnum; i++){
				for (int j = i+1; j <= stationnum; j++) {
					rangeStates[k][i][j] = new AtomicInteger(seatnum);
					
				}
			}
		}

		for(int i = 1; i < stationnum; i++){
			for (int j = i+1; j <= stationnum; j++) {
				
				rwl[i][j] = new ReentrantReadWriteLock();
			} 
		}
		*/
		this.rangeLock = new ReentrantLock();
		
		// seats元组表初始化
		//this.seatStates = new AtomicIntegerArray(coachnum * seatnum + 1);
		this.seatTuples = new Tuple[coachnum * seatnum + 1];
		for (int i = 1; i < coachnum * seatnum + 1; i++) {
			//seatStates.set(i, 0);
			seatTuples[i] = new Tuple(i, new AtomicInteger(0), new int[rangenum + 1]);
		}




		
		// 各区间的saet链表初始化
		this.rangeSeatList = new ArrayList<LinkedList<Tuple>>();
		this.rangeSeatLocks = new ReentrantLock[rangenum + 1];
		rangeSeatList.add(new LinkedList<Tuple>());
		for (int i = 1; i <= rangenum; i++) {
			//rangeSeatList[i] = new LinkedList<Tuple>();
			rangeSeatList.add(new LinkedList<Tuple>());
			for (int j=1; j < coachnum * seatnum + 1; j++) {
				rangeSeatList.get(i).add(seatTuples[j]);
			}
			rangeSeatLocks[i] = new ReentrantLock();

		}

		
	}


	public int[] buyTicket(int departure, int arrival){
		//int range = getRange(departure, arrival);
		
		if(rangeStates[departure][arrival].get() <= 0) return null;
		//if(inquiry(departure, arrival) <= 0) return null;
		
		//rangeLock.lock();
		//if(rangeStates[departure][arrival] <= 0) {
			//rangeLock.unlock();
		//	return null;		
		//}
		int range = getRange(departure, arrival);
		rangeLock.lock();
		LinkedList<Tuple> nowRangeList = rangeSeatList.get(range);
		rangeLock.unlock();
		Tuple nowSeat;// = rangeSeatList[range].geFirst();
		int cnt = 0;
		while(true){
			if(rangeStates[departure][arrival].get() <= 0) return null;
			//if(inquiry(departure, arrival) <= 0) return null;
			/*			
			nowSeat = rangeSeatList[range].getFirst();  
			if(nowSeat.second.compareAndSet(0, 1))
				if(nowSeat == rangeSeatList[range].getFirst())
					break;
			*/

			rangeSeatLocks[range].lock();
			if(nowRangeList.size() == 0){
				rangeSeatLocks[range].unlock();
				return null; 
			}
			if(cnt == nowRangeList.size()) cnt = 0;
	
			nowSeat = nowRangeList.get(cnt);
			
			rangeSeatLocks[range].unlock();
			if(nowSeat.second.compareAndSet(0, 1)){
				if(nowSeat.third[range] == 0){
					break;
				}
				else nowSeat.second.compareAndSet(1, 0);  
			}
		}
		moveRangeStates(departure, arrival, nowSeat, true);
		//rangeLock.unlock();

		// 已经拿到自己的seat,准备修改相关表
		for(int i = 1; i < stationnum; i++){
			if(i >= arrival) continue;
			for (int j = i+1; j <= stationnum; j++) {
				if(j <= departure) continue;
				
				int tempRange = getRange(i, j);
				if(nowSeat.third[tempRange] > 0){
					nowSeat.third[tempRange]++;
					continue;
				}
				rangeSeatLocks[tempRange].lock();
				//rangeSeatList[tempRange].remove(nowSeat);
				rangeSeatList.get(tempRange).remove(nowSeat);
				rangeSeatLocks[tempRange].unlock();
				nowSeat.third[tempRange]++;
			}
		}
		if(!nowSeat.second.compareAndSet(1, 0)){
			System.out.println("错误: RoundDS::buyTicket::94");
			System.exit(-1); 
		}
		//if(route == 1) printRangeStates();
		return new int[]{nowSeat.first, range};
	}

	public boolean refundTicket(Ticket ticket){
		int departure = ticket.departure;
		int arrival = ticket.arrival;
		int coach = ticket.coach;
		int seat = ticket.seat;
		
		int coachAndSeat = (coach-1) * seatnum + seat;
		Tuple nowSeat = seatTuples[coachAndSeat];
		
		while(true){
			if(nowSeat.second.compareAndSet(0, 1)) break;
		}
		
		for(int i = 1; i < stationnum; i++){
			if(i >= arrival) continue;
			for (int j = i+1; j <= stationnum; j++) {
				if(j <= departure) continue;
				int tempRange = getRange(i, j);
				if(--nowSeat.third[tempRange] > 0) continue;
				rangeSeatLocks[tempRange].lock();
				//rangeSeatList[tempRange].add(nowSeat);
				rangeSeatList.get(tempRange).add(nowSeat);
				rangeSeatLocks[tempRange].unlock();
			}
		}


		//rangeLock.lock();
		moveRangeStates(departure, arrival, nowSeat, false);
		//rangeLock.unlock();

		if(!nowSeat.second.compareAndSet(1, 0)){
			System.out.println("错误: RoundDS::buyTicket::126");
			System.exit(-1); 
		}
		
		return true;
	}


	public int inquiry(int departure, int arrival){		
		rwl[departure][arrival].readLock().lock();
		//int res;
		//rwl[departure][arrival].writeLock().lock();
		//rwl[departure][arrival].readlock();
		int res =  rangeStates[departure][arrival].get();
		//rwl[departure][arrival].readunlock();		
		//rwl[departure][arrival].writeLock().unlock();
		rwl[departure][arrival].readLock().unlock();
		/*long tempStamp;
		do{
			tempStamp = rwl[departure][arrival].tryOptimisticRead();
			res =  rangeStates[departure][arrival].get();
			
		}
		while(!rwl[departure][arrival].validate(tempStamp));
*/
		return res;
	}

	private void moveRangeStates(int departure, int arrival, Tuple nowSeat, boolean isBuy){
		
		//int coach = (nowSeat.first - 1) / this.seatnum + 1;
		
		long[][] tempStamps = new long[stationnum + 1][stationnum + 1] ;
		for(int i = 1; i < stationnum; i++){
			if(i >= arrival) continue;
			for (int j = i+1; j <= stationnum; j++) {
				if(j <= departure) continue;
				if(nowSeat.third[getRange(i, j)] == 0)
					//rwl[i][j].readLock().lock();
					//tempStamps[i][j] = rwl[i][j].writeLock();
					//rwl[i][j].writelock();
					rwl[i][j].writeLock().lock();
			} 
		}
		
		for(int i = 1; i < stationnum; i++){
			if(i >= arrival) continue;
			for (int j = i+1; j <= stationnum; j++) {
				if(j <= departure) continue;
				if(nowSeat.third[getRange(i, j)] == 0){
					if(isBuy) rangeStates[i][j].getAndDecrement();
					else rangeStates[i][j].getAndIncrement();
					//rwl[i][j].unlockWrite(tempStamps[i][j]);
					//rwl[i][j].readLock().unlock();
					//rwl[i][j].writeunlock();
					rwl[i][j].writeLock().unlock();
				} 
			}
		}
	}
	private int getRange(int departure, int arrival){
		return (departure - 1) * (2 * stationnum - departure) / 2  + (arrival - departure);
	} 

	
	

}
