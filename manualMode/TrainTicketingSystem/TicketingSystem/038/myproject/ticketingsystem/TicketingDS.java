package ticketingsystem;

import java.util.Random;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.Math.max;

public class TicketingDS implements TicketingSystem {
	public int routenum;
	public int coachnum;
	public int seatnum;
	public int stationnum;
	public int threadnum;

	public final int TIDLENGTH = 10000000;
	public final int CAPACITY = 2000003;
	public Ticket[] hashTicketSet = new Ticket[CAPACITY];	// hash集合：存储已售出的票
	public boolean[] tidFlag = new boolean[TIDLENGTH];		// tid是否被用过
	public boolean[] hashFlag = new boolean[CAPACITY];		// hash集合对应的下标是否被使用

	public SoldSeat[][] soldSeatArray;
	public int[][] freeSeatArray;

	public ReentrantReadWriteLock[] routeLock;

	public int[][][] stationArray;
	public int[][] initState;

	public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
		int totalSeat;
		totalSeat = coachnum * seatnum;

		this.routenum = routenum;
		this.coachnum = coachnum;
		this.seatnum = seatnum;
		this.stationnum = stationnum;
		this.threadnum = threadnum;

		// 初始化哈希表
		for(int i = 0; i < CAPACITY; ++i) {
			hashTicketSet[i] = new Ticket();
		}

		// 初始化每个车次的已售座位
		soldSeatArray = new SoldSeat[routenum + 1][totalSeat + 1];
		for(int i = 1; i <= routenum; ++i) {
			soldSeatArray[i] = new SoldSeat[totalSeat + 1];
			for(int j = 0; j <= totalSeat; ++j) {
				soldSeatArray[i][j] = new SoldSeat(j);		// ps
			}
		}

		// 初始化每个车次的空闲座位
		freeSeatArray = new int[routenum + 1][totalSeat + 1];
		for(int i = 1; i <= routenum; ++i) {
			for(int j = 0; j < totalSeat; ++j) {
				freeSeatArray[i][j] = j + 1;
			}

			freeSeatArray[i][totalSeat] = -1;				// 表尾
		}

		// 初始化每个车次的满足起终站的合法票数
		stationArray = new int[routenum + 1][stationnum + 1][stationnum + 1];
		for(int i = 1; i <= routenum; ++i) {
			for(int j = 1; j <= stationnum; ++j) {
				for(int k = j; k <= stationnum; ++k) {
					stationArray[i][j][k] = totalSeat;
				}
			}
		}

		// 初始化每个车次的锁
		routeLock = new ReentrantReadWriteLock[routenum + 1];
		for(int i = 1; i <= routenum; ++i) {
			routeLock[i] = new ReentrantReadWriteLock();
		}

		// 预处理每个站可能用到的二进制状态
		initState = new int[stationnum + 1][stationnum + 1];
		for(int i = 1; i <= stationnum; ++i) {
			int state = 0;
			for(int j = i; j < stationnum; ++j) {
				state += (1 << (j - 1));
				initState[i][j + 1] = state;
			}
		}
	}

	@Override
	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
		int cnt;
		int state;
		int startState;
		int endState;
		int randTid;
		int randTidHash;
		int chooseSeatNum;
		int buyTicketState;
		Random rand;
		Ticket ticket;

		rand = new Random();
		do {
			randTid = rand.nextInt(TIDLENGTH);
			randTidHash = randTid % CAPACITY;
		} while(tidFlag[randTid] || hashFlag[randTidHash]);

		startState = 1;
		endState = stationnum;
		rand = new Random();
		ticket = new Ticket();
		buyTicketState = initState[departure][arrival];
		routeLock[route].writeLock().lock();

		try {
			if(freeSeatArray[route][0] == -1) {
				chooseSeatNum = queryNumber(route, buyTicketState);
				if(chooseSeatNum == -1) {		// 无票
					return null;
				}

				state = soldSeatArray[route][chooseSeatNum].state;
				soldSeatArray[route][chooseSeatNum].state |= buyTicketState;
				for(int i = departure - 1; i >= 0; --i) {
					if((state & (1 << i)) == 0)
						startState = i + 1;
					else
						break;
				}
				for(int i = arrival - 2; i <= stationnum - 2; ++i) {
					if((state & (1 << i)) == 0)
						endState = i + 2;
					else
						break;
				}
			}
			else {
				chooseSeatNum = freeSeatArray[route][0];
				freeSeatArray[route][0] = freeSeatArray[route][chooseSeatNum];

				soldSeatArray[route][chooseSeatNum].seatNum = chooseSeatNum;
				soldSeatArray[route][chooseSeatNum].state = buyTicketState;
				if(soldSeatArray[route][chooseSeatNum].flag) {			// ps
					soldSeatArray[route][chooseSeatNum].next = soldSeatArray[route][0].next;
					soldSeatArray[route][0].next = chooseSeatNum;
				}
				else
					soldSeatArray[route][chooseSeatNum].flag = true;
			}

			// 更新状态 stationArray
			for(int i = startState; i <= arrival - 1; ++i) {
				cnt = max(departure + 1, i + 1);
				for(int j = cnt; j <= endState; ++j) {
					stationArray[route][i][j] -= 1;
				}
			}

			while(tidFlag[randTid] || hashFlag[randTidHash]) {
				randTid = rand.nextInt(TIDLENGTH);
				randTidHash = randTid % CAPACITY;
			}
			tidFlag[randTid] = true;
		} finally {
			routeLock[route].writeLock().unlock();
		}

		// 生成一张票
		ticket.tid = randTid;
		ticket.passenger = passenger;
		ticket.route = route;
		ticket.coach = (chooseSeatNum - 1) / seatnum + 1;
		ticket.seat = (chooseSeatNum - 1) % seatnum + 1;
		ticket.departure = departure;
		ticket.arrival = arrival;
		hashFlag[randTidHash] = true;
		hashTicketSet[randTidHash] = ticket;

		return ticket;
	}

	@Override
	public int inquiry(int route, int departure, int arrival) {
		routeLock[route].readLock().lock();

		try {
			return stationArray[route][departure][arrival];
		} finally {
			routeLock[route].readLock().unlock();
		}
	}

	@Override
	public boolean refundTicket(Ticket ticket) {
		int cnt;
		int index;
		int route;
		int state;
		int startState = 0;
		int endState = 0;
		int refundSeatNum;
		int refundSeatState;

		index = (int) (ticket.tid % CAPACITY);
		route = ticket.route;
		refundSeatNum = (ticket.coach - 1) * seatnum + ticket.seat;
		routeLock[route].writeLock().lock();

		try {
			if(!hashFlag[index] || route != hashTicketSet[index].route || !ticket.passenger.equals(hashTicketSet[index].passenger)
					|| ticket.coach != hashTicketSet[index].coach || ticket.seat != hashTicketSet[index].seat
					|| ticket.departure != hashTicketSet[index].departure || ticket.arrival != hashTicketSet[index].arrival) {
				return false;
			}

			hashFlag[index] = false;
			refundSeatState = initState[ticket.departure][ticket.arrival];

			update(route, refundSeatNum, refundSeatState);

			// ps：更新状态，以及 stationArray
			state = soldSeatArray[route][refundSeatNum].state;
			for(int i = ticket.departure - 1; i >= 0; --i) {
				if((state & (1 << i)) == 0)
					startState = i + 1;
				else
					break;
			}
			for(int i = ticket.arrival - 2; i <= stationnum - 2; ++i) {
				if((state & (1 << i)) == 0)
					endState = i + 2;
				else
					break;
			}

			for(int i = startState; i <= ticket.arrival - 1; ++i) {
				cnt = max(ticket.departure + 1, i + 1);
				for(int j = cnt; j <= endState; ++j) {
					stationArray[route][i][j] += 1;
				}
			}

			return true;
		} finally {
			routeLock[route].writeLock().unlock();
		}
	}

	public int queryNumber(int route, int state) {
		int pre;
		int cur;
		SoldSeat soldSeat;

		soldSeat = soldSeatArray[route][0];
		cur = 0;

		while(soldSeat.next != -1) {
			pre = cur;
			cur = soldSeat.next;
			soldSeat = soldSeatArray[route][cur];

			if(!soldSeat.flag) {
				soldSeatArray[route][pre].next = soldSeatArray[route][cur].next;
				soldSeat.flag = true;
				cur = pre;
			}
			else if((soldSeat.state & state) == 0) {
//				soldSeat.state |= state;
				return soldSeat.seatNum;
			}
		}

		return -1;
	}

	public boolean update(int route, int refundSeatNum, int state) {
		SoldSeat soldSeat;

		soldSeat = soldSeatArray[route][refundSeatNum];
		if((soldSeat.state -= state) == 0) {
			soldSeat.flag = false;
			freeSeatArray[route][refundSeatNum] = freeSeatArray[route][0];
			freeSeatArray[route][0] = refundSeatNum;
		}

		return true;
	}
}

class SoldSeat {
	int seatNum;
	int state;
	int next;
	boolean flag;

	public SoldSeat(int seatNum) {
		this.seatNum = seatNum;
		this.state = 0;
		this.next = -1;
		this.flag = true;
	}
}
