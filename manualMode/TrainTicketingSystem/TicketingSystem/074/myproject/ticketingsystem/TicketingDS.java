package ticketingsystem;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/*
TO-know
1.目前来看让TicketingDS知道threadnum并没有什么实际意义
2.
*/

/*
TO-DO
1.获得ticket的id的函数，其中id的获取与增长需要锁住
2.如何存储列车座位呢？如何判断哪些座位哪个段能卖?使用一个整型代表哪段能卖
*/

public class TicketingDS implements TicketingSystem {
	private int routenum;// 车次总数
	private int coachnum;// 列车的车厢数目
	private int seatnum;// 每节车厢的座位数
	private int stationnum;// 每个车次经停站的数量
	private int threadnum;// 并发购票的线程数
	private Route routes[];
	private AtomicLong count = new AtomicLong(0);
	ConcurrentHashMap<Long, Ticket> soldTickets = new ConcurrentHashMap<>();

	public TicketingDS() {
		this.routenum = 5;
		this.coachnum = 8;
		this.seatnum = 100;
		this.stationnum = 10;
		this.threadnum = 16;
		routes = new Route[routenum + 1];
		for (int i = 1; i < routenum + 1; i++) {
			routes[i] = new Route(coachnum, seatnum);
		}
	}

	public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
		this.routenum = routenum;
		this.coachnum = coachnum;
		this.seatnum = seatnum;
		this.stationnum = stationnum;
		this.threadnum = threadnum;
		routes = new Route[routenum + 1];
		for (int i = 1; i < routenum + 1; i++) {
			routes[i] = new Route(coachnum, seatnum);
		}
	}

	@Override
	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
		Ticket ticket = null;
		// 参数验证,若输入参数不符合系统要求则直接返回0
		if (!ifLegal(route, departure, arrival))
			return ticket;
		Route tempRoute = routes[route];
		Coach tempCoaches[] = tempRoute.getCoachs();
		int mask = getMask(departure, arrival);
		// 遍历每个车厢的每个座位
		for (int i = 1; i <= coachnum; i++) {
			Coach tempCoach = tempCoaches[i];
			// tempCoach.readAndwriteLockBuy.writeLock().lock();
			Seat tempSeats[] = tempCoach.getSeats();
			for (int j = 1; j <= seatnum; j++) {
				Seat tempSeat = tempSeats[j];
				if (tempSeat.ifAvailable(mask)) {// 成功，填装ticket
					//尝试上锁买票
					tempCoach.readAndwriteLockBuy.writeLock().lock();
					if(tempSeat.ifAvailableAndSet(mask)){//买票成功
						tempCoach.readAndwriteLockBuy.writeLock().unlock();
						ticket = new Ticket();
						setTicket(ticket, passenger, route, i, j, departure, arrival);
						ticket.tid = count.getAndIncrement();
						Ticket hashTicket = new Ticket();
						copy(hashTicket, ticket);
						soldTickets.put(ticket.tid, ticket);
						return ticket;
					}
					else{
						tempCoach.readAndwriteLockBuy.writeLock().unlock();
						continue;
					}
				}
			}
		}
		return ticket;
	}

	public void setTicket(Ticket ticket, String passenger, int route, int coach, int seat, int departure, int arrival) {
		ticket.passenger = passenger;
		ticket.route = route;
		ticket.coach = coach;
		ticket.seat = seat;
		ticket.departure = departure;
		ticket.arrival = arrival;
	}

	@Override
	public int inquiry(int route, int departure, int arrival) {
		int seatLeftNums=0;
		//参数验证,若输入参数不符合系统要求则直接返回0
		if(!ifLegal(route,departure,arrival)) return seatLeftNums;
		Route tempRoute=routes[route];
		Coach tempCoaches[]=tempRoute.getCoachs();
		int mask=getMask(departure,arrival);
		//遍历每个车厢的每个座位
		for(int i=1;i<=coachnum;i++){
			Coach tempCoach=tempCoaches[i];
			//追求查询的可线性化
			tempCoach.readAndwriteLockRefund.readLock().lock();
			tempCoach.readAndwriteLockBuy.readLock().lock();
			Seat tempSeats[]=tempCoach.getSeats();
			for(int j=1;j<=seatnum;j++){
				Seat tempSeat=tempSeats[j];
				// tempSeat.readAndwriteLockRefund.readLock().lock();
				if(tempSeat.ifAvailable(mask)){
					seatLeftNums++;
				}
			}
		}
		for(int i=1;i<=coachnum;i++){
			Coach tempCoach=tempCoaches[i];
			//追求查询的可线性化
			tempCoach.readAndwriteLockRefund.readLock().unlock();
			tempCoach.readAndwriteLockBuy.readLock().unlock();
		}
		return seatLeftNums;
	}

	// @Override
	// public int inquiry(int route, int departure, int arrival) {
	// 	int seatLeftNums = 0;
	// 	// 参数验证,若输入参数不符合系统要求则直接返回0
	// 	if (!ifLegal(route, departure, arrival))
	// 		return seatLeftNums;
	// 	Route tempRoute = routes[route];
	// 	Coach tempCoaches[] = tempRoute.getCoachs();
	// 	int mask = getMask(departure, arrival);
	// 	// 遍历每个车厢的每个座位
	// 	for (int i = 1; i <= coachnum; i++) {
	// 		Coach tempCoach = tempCoaches[i];
	// 		// 追求查询的可线性化
	// 		tempCoach.readAndwriteLockRefund.readLock().lock();
	// 		tempCoach.readAndwriteLockBuy.readLock().lock();
	// 		// synchronized (tempCoach.lockRefund) {
	// 		// 	while (tempCoach.lockRefund != 0) {// 正在有refund操作
	// 		// 		try {
	// 		// 			tempCoach.lockRefund.wait();
	// 		// 		} catch (InterruptedException e) {
	// 		// 			// TODO Auto-generated catch block
	// 		// 			e.printStackTrace();
	// 		// 		}
	// 		// 	} 
	// 		// 	synchronized (tempCoach.lockInquiry) {
	// 		// 		tempCoach.lockInquiry++;
	// 		// 	}
				
	// 		// }
	// 		Seat tempSeats[] = tempCoach.getSeats();
	// 		for (int j = 1; j <= seatnum; j++) {
	// 			Seat tempSeat = tempSeats[j];
	// 			// tempSeat.readAndwriteLockRefund.readLock().lock();
	// 			if (tempSeat.ifAvailable(mask)) {
	// 				seatLeftNums++;
	// 			}
	// 		}
	// 	}
	// 	// 释放每个座位
	// 	// for(int i=1;i<=coachnum;i++){
	// 	// Coach tempCoach=tempCoaches[i];
	// 	// Seat tempSeats[]=tempCoach.getSeats();
	// 	// for(int j=1;j<=seatnum;j++){
	// 	// Seat tempSeat=tempSeats[j];
	// 	// tempSeat.readAndwriteLockRefund.readLock().unlock();
	// 	// }
	// 	// }
	// 	// 释放每个车厢
	// 	for (int i = 1; i <= coachnum; i++) {
	// 		Coach tempCoach = tempCoaches[i];
	// 		// synchronized (tempCoach.lockInquiry) {
	// 		// 	tempCoach.lockInquiry--;
	// 		// 	if (tempCoach.lockInquiry == 0) {// 唤醒所有refund线程
	// 		// 		tempCoach.lockInquiry.notifyAll();
	// 		// 	}
	// 		// }
	// 		// 追求查询的可线性化
	// 		tempCoach.readAndwriteLockRefund.readLock().unlock();
	// 		tempCoach.readAndwriteLockBuy.readLock().unlock();
	// 	}
	// 	return seatLeftNums;
	// }

	public int getMask(int departure, int arrival) {
		int mask = 0;
		int temp = (int) Math.pow(2, departure - 1);
		for (int i = departure; i < arrival; i++) {
			mask = mask + temp;
			temp = temp * 2;
		}
		return mask;
	}

	public boolean ifLegal(int route, int departure, int arrival) {
		if (route <= routenum && route >= 1 && departure <= stationnum && departure >= 1 && arrival <= stationnum
				&& arrival >= 1 && departure < arrival) {
			return true;
		}
		return false;
	}

	@Override
	public boolean refundTicket(Ticket ticket) {
		if (soldTickets.containsKey(ticket.tid)&&ticketEquals(soldTickets.get(ticket.tid), ticket)) {// 如果该票存在则删除
			if (soldTickets.remove(ticket.tid) == null) {
				return true;
			}
			int mask = getMaskForRefund(ticket.departure, ticket.arrival);
			routes[ticket.route].getCoachs()[ticket.coach].readAndwriteLockRefund.writeLock().lock();
			routes[ticket.route].getCoachs()[ticket.coach].getSeats()[ticket.seat].refund(mask);
			routes[ticket.route].getCoachs()[ticket.coach].readAndwriteLockRefund.writeLock().unlock();
			return true;
		}
		return false;
	}

	public int getMaskForRefund(int departure, int arrival) {
		int mask=Integer.MAX_VALUE;
		int temp=(int) Math.pow(2, departure-1);
		for(int i=departure;i<arrival;i++){
			mask=mask-temp;
			temp=temp*2;
		}
		return mask;
	}

	public boolean ticketEquals(Ticket a, Ticket obj) {
		if (a == obj)
			return true;
		if (obj == null)
			return false;
		if (a.getClass() != obj.getClass())
			return false;
		Ticket other = (Ticket) obj;
		if (a.arrival != other.arrival)
			return false;
		if (a.coach != other.coach)
			return false;
		if (a.departure != other.departure)
			return false;
		if (a.passenger == null) {
			if (other.passenger != null)
				return false;
		} else if (!a.passenger.equals(other.passenger))
			return false;
		if (a.route != other.route)
			return false;
		if (a.seat != other.seat)
			return false;
		if (a.tid != other.tid)
			return false;
		return true;
	}
	
	public void copy(Ticket a,Ticket obj){
		a.tid = obj.tid;
		a.passenger = obj.passenger;
		a.route = obj.route;
		a.coach = obj.coach;
		a.seat = obj.seat;
		a.departure = obj.departure;
		a.arrival = obj.arrival;
	}
}
