package ticketingsystem;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;


/*激进分支版本
 * 1使用bitmap存储座位
 * 1全局锁互斥
 * 1原始哈希表
 * 2增加了总查询索引结构
 * 3改掉找座位bug
 * 3座位按线程分组
 * 3增加查询二级索引
 * 3增加的座位结构互斥，使用总读写锁
 * 分支1取消分组设置，取消二级索引，恢复车厢结构
 * 分支1取消总读写锁，使用无锁结构
 * 分支1使用真正位图存储座位，使用原子long型
 * 分支1函数手动内联展开
 */


public class TicketingDSRadical implements TicketingSystem
{
	//车站信息
	int routenum = 5;//车次数
	int coachnum = 8;//车厢数
	int seatnum = 100;//座位数
	int stationnum = 10;//站台数
	int threadnum = 16;//线程数
	//辅助结构
	AtomicLong ticketCnt = new AtomicLong(1);//车票号计数
	AtomicLong[][][] arrSeat;//按组座位bitmap
	AtomicInteger[][][] idxInqAll;//总查询索引，每个元素(i, j)意味着(i, j)比(i, j-1)多的座位数
	Map<Long, IdMsg> mapId;//id数据记录
	//初始化
	void OtherInit()
	{
		int allSeat = coachnum*seatnum;
		//组座位记录初始化
		arrSeat = new AtomicLong[routenum][coachnum][seatnum];
		for(int i=0; i<routenum; ++i) {
			for(int j=0; j<coachnum; ++j) {
				for(int k=0; k<seatnum; ++k) {
					arrSeat[i][j][k] = new AtomicLong(0);
				}
			}
		}
		//id哈希表初始化
		mapId = new HashMap<Long, IdMsg>();
		//查询结构初始化
		idxInqAll = new AtomicInteger[routenum][stationnum-1][];
		for(AtomicInteger[][] arr: idxInqAll) {
			for(int i=0; i<stationnum-1; ++i) {
				arr[i] = new AtomicInteger[stationnum-1-i];
				arr[i][0] = new AtomicInteger(allSeat);
				for(int k=1; k<stationnum-1-i; ++k)
					arr[i][k] = new AtomicInteger(0);
			}
		}
	}
	//构造函数
	public TicketingDSRadical()
	{
		OtherInit();
	}
	public TicketingDSRadical(int routenum, int coachnum, int seatnum, int stationnum, int threadnum)
	{
		this.routenum = routenum;
		this.coachnum = coachnum;
		this.seatnum = seatnum;
		this.stationnum = stationnum;
		this.threadnum = threadnum;
		OtherInit();
	}
	//边界检查
	boolean CheckRegular(int route, int departure, int arrival)
	{
		return route>=1 && route<=routenum
				&& departure<arrival
				&& departure>=1 && arrival<=stationnum;
	}
	//生成座位
	long CreateSeatBitmap(int departure, int arrival)
	{
		long res = 0;
		for(int i=0; i<(arrival-departure)/8; ++i) {
			res <<= 8;
			res |= 0xff;
		}
		for(int i=0; i<(arrival-departure)%8; ++i) {
			res <<= 1;
			res |= 0x01;
		}
		res <<= (departure-1);
		return res;
	}
	//计算座位范围
	int[] CalStedBitmap(long seat, int departure, int arrival)
	{
		int[] sted = new int[2];
		//计算附近座位
		for(sted[0]=departure-1; sted[0]>0; --sted[0]) {
			if((seat&(1<<(sted[0]-1)))!=0)
				break;
		}
		for(sted[1]=arrival-1; sted[1]<stationnum-1; ++sted[1]) {
			if((seat&(1<<sted[1]))!=0)
				break;
		}
		return sted;
	}
	//增减索引计数
	void ChangeInq(int[][] inq, int[] sted, int departure, int arrival, boolean bBuy)
	{
		//根据附近座位调整查询索引
		int delta = bBuy? -1:1;
		for(int i=sted[0]; i<arrival-1; ++i) {
			inq[i][Math.max(0, departure-1-i)] += delta;
			if(sted[1]<stationnum-1)
				inq[i][sted[1]-i] -= delta;
		}
	}
	void ChangeInqAtomic(AtomicInteger[][] inq, int[] sted, int departure, int arrival, boolean bBuy)
	{
		//根据附近座位调整查询索引
		int delta = bBuy? -1:1;
		for(int i=sted[0]; i<arrival-1; ++i) {
			inq[i][Math.max(0, departure-1-i)].getAndAdd(delta);
			if(sted[1]<stationnum-1)
				inq[i][sted[1]-i].getAndAdd(-delta);
		}
	}
	//计算查询数
	int CalInq(int[][] inq, int departure, int arrival)
	{
		int sum = 0;
		for(int i=0; i<arrival-departure; ++i)
			sum += inq[departure-1][i];
		return sum;
	}
	int CalInqAtomic(AtomicInteger[][] inq, int departure, int arrival)
	{
		int sum = 0;
		for(int i=0; i<arrival-departure; ++i)
			sum += inq[departure-1][i].get();
		return sum;
	}
	//调试查询索引结构，未更新按组结构
	void DebugIdxInq()
	{
	}
	//购票函数辅助
	boolean BuyTicketAssist(Ticket ticket)
	{
		boolean bBuy = false;
		//循环查找
		Random rand = new Random();
		int initCoach = rand.nextInt(coachnum);
		int initSeat = rand.nextInt(seatnum);
		int findCoach = -1;
		int findSeat = -1;
		int[] sted = null;
		long target = CreateSeatBitmap(ticket.departure, ticket.arrival);
		//车厢循环
		loop:
		for(int i=initCoach; i<initCoach+coachnum; ++i) {
			int coachIdx = i%coachnum;
			//车座循环
			for(int j=initSeat; j<initSeat+seatnum; ++j) {
				int seatIdx = j%seatnum;
				//找到处理且改变成功
				while(true) {
					long seat = arrSeat[ticket.route-1][coachIdx][seatIdx].get();
					if((seat&target)==0) {
						if(arrSeat[ticket.route-1][coachIdx][seatIdx].compareAndSet(seat, seat^target)) {
							//记录
							bBuy = true;
							findCoach = coachIdx+1;
							findSeat = seatIdx+1;
							//改变座位和组查询内容
							sted = CalStedBitmap(seat, ticket.departure, ticket.arrival);
							break loop;
						}
					}
					else
						break;
				}
			}
		}
		//判断查找结果
		if(bBuy) {
			//票存储
			ticket.tid = ticketCnt.getAndIncrement();
			ticket.coach = findCoach;
			ticket.seat = findSeat;
			//改变总查询内容
			ChangeInqAtomic(idxInqAll[ticket.route-1], sted, ticket.departure, ticket.arrival, true);
			//改变哈希表
			synchronized(mapId) {
				mapId.put(ticket.tid, new IdMsg(ticket));
			}
		}
		return bBuy;
	}
	//买票函数
	public Ticket buyTicket(String passenger, int route, int departure, int arrival)
	{
		//合法性检查
		if(!CheckRegular(route, departure, arrival))
			return null;
		//票初始化
		Ticket ticket = new Ticket();
		ticket.passenger = passenger;
		ticket.route = route;
		ticket.departure = departure;
		ticket.arrival = arrival;
		boolean res = false;
		//进行买票
		res = BuyTicketAssist(ticket);
		if(res)
			return ticket;
		return null;
	}
	//查询函数
	public int inquiry(int route, int departure, int arrival)
	{
		//合法性检查
		if(!CheckRegular(route, departure, arrival))
			return 0;
		//累加查询数组
		int res = CalInqAtomic(idxInqAll[route-1], departure, arrival);
		res = Math.max(0, res);
		res = Math.min(coachnum*seatnum, res);
		return res;
	}
	//退票函数
	public boolean refundTicket(Ticket ticket)
	{
		//找到退票，不合理则返回
		IdMsg msg;
		synchronized(mapId) {
			msg = mapId.get(ticket.tid);
		}
		if(msg==null)
			return false;
		//若我是第一个退票的
		if(msg.bWait.compareAndSet(false, true)) {
			long target = CreateSeatBitmap(ticket.departure, ticket.arrival);
			//组内互斥，改变座位和查询内容
			int[] sted;
			while(true) {
				long seat = arrSeat[msg.route-1][msg.coach-1][msg.seat-1].get();
				if(arrSeat[msg.route-1][msg.coach-1][msg.seat-1].compareAndSet(seat, seat^target)) {
					sted = CalStedBitmap(seat, msg.departure, msg.arrival);
					break;
				}
			}
			//改变总查询结构
			ChangeInqAtomic(idxInqAll[msg.route-1], sted, msg.departure, msg.arrival, false);
			//改变哈希表
			synchronized(mapId) {
				mapId.remove(ticket.tid);
			}
			//通知其他线程
			msg.bFinish.set(true);
			return true;
		}
		//否则等待其他线程退票
		else {
			while(!msg.bFinish.get())
				;
			return false;
		}
	}
}
