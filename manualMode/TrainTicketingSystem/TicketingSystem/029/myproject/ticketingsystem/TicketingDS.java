package ticketingsystem;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class TicketingDS implements TicketingSystem {

	private int routeNum = 5;
	private int coachNum = 8;
	private int seatNum = 100;
	private int stationNum = 10;
	private int threadNum = 16;

	private AtomicLong counter;									//tid生成器
	private StationManage stationManage;						//管理站点
	private SeatManage seatManage;								//管理座位
	private ConcurrentHashMap<Long, Ticket> ticketsRecord;		//存放已卖出票的信息

	private void init() {
		stationManage = new StationManage(routeNum, coachNum, seatNum, stationNum, threadNum);
		seatManage = new SeatManage(routeNum, coachNum, seatNum, stationNum, threadNum);
		counter = new AtomicLong();
		int initialCapacity = (int) (routeNum * coachNum * seatNum * stationNum * 0.5);	//参数是百度找的
		ticketsRecord = new ConcurrentHashMap<>(initialCapacity, 0.75f, threadNum);	//参数是百度找的
	}

	public TicketingDS() {
		init();
	}

	public TicketingDS(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
		this.routeNum = routeNum;
		this.coachNum = coachNum;
		this.seatNum = seatNum;
		this.stationNum = stationNum;
		this.threadNum = threadNum;
		init();
	}

	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
		// 判断传入参数的正确性，包块验证是否有名字，数字是否正确
		if (isWrong(passenger, route, departure, arrival))
			return null;
		ThreadLocalRandom rand = ThreadLocalRandom.current();				// 随机数生成器，单个线程独享
		// InsideTicket是程序内部使用的代替Ticket的类，创建一张无效的车票
		MyTicket it = new MyTicket(route, 0, 0, departure, arrival);
		// 阀值，降低在高并发的情况下，购票的冲突，经过测试，有阀值最大可以提高一倍的吞吐量，但会增加购票的延迟
		for(int i = 0; i < 128; i++){
			it.coach = rand.nextInt(coachNum) + 1;
			it.seat = rand.nextInt(seatNum) + 1;
			if (tryBuyTicket(it)){
				return printTicket(passenger, it);		//购买车票成功，将车票加入ticketsRecord
			}

		}
		//查询余票
		List<MyTicket> availableTickets = stationManage.locateAvailables(route, departure, arrival);
		for (MyTicket t : availableTickets) {
			if (tryBuyTicket(t)) {
				return printTicket(passenger, t);
			}
		}
		return null;
	}

	public int inquiry(int route, int departure, int arrival) {		//查询余票数
		if (isWrong("just inquiry", route, departure, arrival))
			return 0;
		return stationManage.countAvailables(route, departure, arrival);
	}

	public boolean refundTicket(Ticket ticket) {					//退票
		if (ticket == null)
			return false;
		long tid = ticket.tid;
		MyTicket it = new MyTicket(ticket);
		if (isWrong(ticket) || seatManage.isAvailable(it) || !ticketsRecord.containsKey(tid))
			return false;
		if (!isEqual(ticket, ticketsRecord.get(tid)))
			return false;
		if (ticketsRecord.remove(tid) == null)
			return false;
		seatManage.free(it);
		stationManage.free(it);				//将每个车站对应座位的比特置为0
		return true;
	}

	private boolean tryBuyTicket(MyTicket it) {
		if (seatManage.tryOccupy(it)) {			//尝试占座，如果成功，将车票信息加入查询数据结构中
			stationManage.occupy(it);		//将每个车站对应座位的比特置为1
			return true;
		}
		return false;
	}

	private Ticket printTicket(String passenger, MyTicket it) {
		Ticket t = it.toTicket(counter.getAndIncrement(), passenger);
		ticketsRecord.put(t.tid, t);
		return t;
	}

	private boolean isEqual(Ticket a, Ticket b) {
		return a.tid == b.tid && a.passenger != null && b.passenger != null && a.passenger.equals(b.passenger) && a.route == b.route && a.coach == b.coach && a.seat == b.seat && a.departure == b.departure && a.arrival == b.arrival;
	}

	private boolean isWrong(Ticket t) {
		String passenger = t.passenger;
		long tid = t.tid;
		int route = t.route;
		int coach = t.coach;
		int seat = t.seat;
		int departure = t.departure;
		int arrival = t.arrival;
		return tid < 0L || coach <= 0 || coach > this.coachNum || seat <= 0 || seat > this.seatNum || this.isWrong(passenger, route, departure, arrival);
	}

	private boolean isWrong(String passenger, int route, int departure, int arrival) {
		return passenger == null || passenger.equals("") || route <= 0 || route > this.routeNum || departure <= 0 || departure > this.stationNum || arrival <= 0 || arrival > this.stationNum || departure >= arrival;
	}
}
