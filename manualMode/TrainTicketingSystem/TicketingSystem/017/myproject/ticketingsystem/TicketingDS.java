package ticketingsystem;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class Ticket{
        long tid;
        String passenger;
        int route;
        int coach;
        int seat;
        int departure;
        int arrival;
}



class QueryTable {
	static final int[] bitHelper = {
		0x0,
		0x1, 0x3, 0x7, 0xF,
		0x1F, 0x3F, 0x7F, 0xFF,
		0x1FF, 0x3FF, 0x7FF, 0xFFF,
		0x1FFF, 0x3FFF, 0x7FFF, 0xFFFF,
		0x1FFFF, 0x3FFFF, 0x7FFFF, 0xFFFFF,
		0x1FFFFF, 0x3FFFFF, 0x7FFFFF, 0xFFFFFF,
		0x1FFFFFF, 0x3FFFFFF, 0x7FFFFFF, 0xFFFFFFF,
		0x1FFFFFFF, 0x3FFFFFFF, 0x7FFFFFFF, 0xFFFFFFFF,
	};
	private AtomicInteger[][] elements;
	private ReadWriteLock[][] locks;
	private int maxStationNum;

	QueryTable(int stationNum, int ticketNum){
		this.maxStationNum = stationNum - 1;
		elements = new AtomicInteger[stationNum - 1][];
		for(int i = 0; i < stationNum - 1; ++i){
			elements[i] = new AtomicInteger[stationNum - i - 1];
			for(int j = 0; j < stationNum - i - 1; ++j)elements[i][j] = new AtomicInteger(ticketNum);
		}

		locks = new ReentrantReadWriteLock[stationNum - 1][];
		for(int i = 0; i < stationNum - 1; ++i){
			locks[i] = new ReentrantReadWriteLock[stationNum - i - 1];
			for(int j = 0; j < stationNum - i - 1; ++j)locks[i][j] = new ReentrantReadWriteLock();
		}
	}

	void soldModify(int departure, int arrival, int newVal) {
		// begin:受影响区间中最早的出发站对应的位。
		// end:受影响区间中最晚的出发站对应的位。
		// minBound：受影响区间中最早的终点站对应的位。
		// maxBound:受影响区间中最晚的终点站对应的位。
		int begin = departure - 2;
		while(begin >= 0 && ((1 << begin) & newVal) == 0){
			begin -= 1;
		}
		begin += 1;

		int minBound = departure - 1 - begin;
		int end = arrival - 1;

		int maxBound = arrival - 1;
		while(maxBound <= maxStationNum && ((1 << maxBound) & newVal) == 0){
			maxBound += 1;
		}
		maxBound -= 1;

		for(int i = begin; i < end; ++i){
			for(int j = Math.max(minBound - i, 0); j < Math.max(maxBound - i, 1); ++j){
				locks[i][j].readLock().lock();
			}
		}
		for(int i = begin; i < end; ++i){
			for(int j = Math.max(minBound - i, 0); j < Math.max(maxBound - i, 1); ++j){
				elements[i][j].decrementAndGet();
			}
		}
		for(int i = begin; i < end; ++i){
			for(int j = Math.max(minBound - i, 0); j < Math.max(maxBound - i, 1); ++j){
				locks[i][j].readLock().unlock();
			}
		}
	}

	void refundModify(int departure, int arrival, int oldVal){
		int begin = departure - 2;
		while(begin >= 0 && ((1 << begin) & oldVal) == 0){
			begin -= 1;
		}
		begin += 1;

		int minBound = departure - 1 - begin;
		int end = arrival - 1;

		int maxBound = arrival - 1;
		while(maxBound <= maxStationNum && ((1 << maxBound) & oldVal) == 0){
			maxBound += 1;
		}
		maxBound -= 1;

		for(int i = begin; i < end; ++i){
			for(int j = Math.max(minBound - i, 0); j < Math.max(maxBound - i, 1); ++j){
				locks[i][j].readLock().lock();
			}
		}

		for(int i = begin; i < end; ++i){
			for(int j = Math.max(minBound - i, 0); j < Math.max(maxBound - i, 1); ++j){
				elements[i][j].incrementAndGet();
			}
		}

		for(int i = begin; i < end; ++i){
			for(int j = Math.max(minBound - i, 0); j < Math.max(maxBound - i, 1); ++j){
				locks[i][j].readLock().unlock();
			}
		}
	}

	int query(int departure, int arrival){
		int i = departure - 1;
		int j = arrival - departure - 1;
		int result = 0;
		locks[i][j].writeLock().lock();
		result = elements[i][j].get();
		locks[i][j].writeLock().unlock();
		return result;
	}
}

class Seat {
	static final int[] bitHelper = {
		0x0,
		0x1, 0x3, 0x7, 0xF,
		0x1F, 0x3F, 0x7F, 0xFF,
		0x1FF, 0x3FF, 0x7FF, 0xFFF,
		0x1FFF, 0x3FFF, 0x7FFF, 0xFFFF,
		0x1FFFF, 0x3FFFF, 0x7FFFF, 0xFFFFF,
		0x1FFFFF, 0x3FFFFF, 0x7FFFFF, 0xFFFFFF,
		0x1FFFFFF, 0x3FFFFFF, 0x7FFFFFF, 0xFFFFFFF,
		0x1FFFFFFF, 0x3FFFFFFF, 0x7FFFFFFF, 0xFFFFFFFF,
	};
	private int roadMap;
	private QueryTable table;

	public Seat(QueryTable table){
		roadMap = 0;
		this.table = table;
	}

	public void clear(int departure, int arrival){
		int temp = (~bitHelper[arrival - 1]) | bitHelper[departure - 1];
		int oldVal = 0; 
		synchronized(this) {
			oldVal = roadMap;
			roadMap &= temp;
		}
		table.refundModify(departure, arrival, oldVal);
	}

	public boolean set(int departure, int arrival){
		int temp = bitHelper[arrival - 1] & (~bitHelper[departure - 1]);
		if((temp & roadMap) != 0)return false;
		int newVal = 0;
		synchronized(this) {
			if((temp & roadMap) == 0){
				roadMap |= temp;
				newVal = roadMap;
			}
		}
		if(newVal != 0){
			table.soldModify(departure, arrival, newVal);
			return true;
		}else{
			return false;
		}
	}

	public int query(int departure, int arrival){
		int temp = bitHelper[arrival - 1] & (~bitHelper[departure - 1]);
		return ((roadMap & temp) ==0)?1:0;
	}
}

class Coach {
	private int seatNumber;
	private Seat[] seats;

	public Coach(int seatNum, QueryTable table){
		seatNumber = seatNum;
		seats = new Seat[seatNum];
		for(int i = 0; i < seatNumber; ++i){
			seats[i] = new Seat(table);
		}
	}

	public Ticket buyTicket(int departure, int arrival){
		for(int i = 0; i < seatNumber; ++i){
			if(seats[i].set(departure, arrival)){
				Ticket ticket = new Ticket();
				ticket.departure = departure;
				ticket.arrival = arrival;
				ticket.seat = i + 1;
				return ticket;
			}
		}
		return null;
	}

	public void refundTicket(int seatNum, int departure, int arrival){
		seats[seatNum - 1].clear(departure, arrival);
	}

	public int queryInterval(int departure, int arrival){
		int result = 0;
		for(int i = 0; i < seatNumber; ++i){
			result += seats[i].query(departure, arrival);
		}
		return result;
	}

}

class Train {
	private int stationNumber;
	private int coachNumbers;
	private ArrayList<Coach>  coaches;
	private QueryTable table;

	public Train(int coachNum, int seatNum, int stationNum){
		stationNumber = stationNum;
		coachNumbers = coachNum;
		int ticketNum = coachNum * seatNum;
		this.table = new QueryTable(stationNum, ticketNum);
		coaches = new ArrayList<>(coachNum);
		for (int i = 1; i <= coachNum; ++i)
			this.coaches.add(new Coach(seatNum, table));
	}

	public Ticket buyTicket(int departure, int arrival){
		Ticket result = null;
		if(departure < 1 || departure >= stationNumber || departure >= arrival)return result;
		if(arrival <= 1 || arrival > stationNumber)return result;

		for(int i = 0; i <  coachNumbers; ++i){
			result = coaches.get(i).buyTicket(departure, arrival);
			if(result != null){
				result.coach = i + 1;
				break;
			}
		}
		return result;
	}

	public int query(int departure, int arrival){
		if(departure < 1 || departure >= stationNumber || departure >= arrival)return 0;
		if(arrival <= 1 || arrival > stationNumber)return 0;
		// int result = 0;
		// for(int i = 0; i < coaches.size(); ++i){
		// 	result += coaches.get(i).queryInterval(departure, arrival);
		// }
		// return result;
		return table.query(departure, arrival);
	}

	public void refund(Ticket ticket){
		coaches.get(ticket.coach - 1).refundTicket(ticket.seat, ticket.departure, ticket.arrival);
	}

}

//public class TicketingDS implements TicketingSystem {
public class TicketingDS {
	private ArrayList<Train> trains;
	private ConcurrentHashMap<Long, Ticket> ticketRecord;
	private AtomicLong idGenerator;

	public TicketingDS(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
		trains = new ArrayList<Train>(routeNum);
		for (int i = 0; i < routeNum; ++i)
			this.trains.add(new Train(coachNum, seatNum, stationNum));
		int capacity = (int)(routeNum * coachNum * seatNum * 0.5);
		ticketRecord = new ConcurrentHashMap<>(capacity, 0.75f, threadNum);
		idGenerator = new AtomicLong(0);
	}

	//@Override
	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
		if(route < 0 || route > trains.size())return null;
		Ticket result = trains.get(route - 1).buyTicket(departure, arrival);
		if(result != null){
			long tid = idGenerator.getAndIncrement();
			result.tid = tid;
			result.passenger = passenger;
			result.route = route;
			ticketRecord.put(tid, result);
		}
		return result;
	}

	//@Override
	public int inquiry(int route, int departure, int arrival) {
		if(route < 0 || route > trains.size())return 0;
		return trains.get(route-1).query(departure, arrival);
	}

	//@Override
	public boolean refundTicket(Ticket ticket) {
		if(!checkEqual(ticket, ticketRecord.get(ticket.tid))){
			return false;
		}
		ticketRecord.remove(ticket.tid);
		trains.get(ticket.route - 1).refund(ticket);
		return true;
	}

	private boolean checkEqual(Ticket a, Ticket b){
		return a != null && b != null && a.tid == b.tid
			&& a.passenger.equals(b.passenger) 
			&& a.route == b.route && a.coach == b.coach && a.seat == b.seat 
			&& a.departure == b.departure && a.arrival == b.arrival;
	}

}
