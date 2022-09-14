package ticketingsystem;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;


/*安全分支版本
 * 1使用bitmap存储座位
 * 1全局锁互斥
 * 1原始哈希表
 * 2增加了总查询索引结构
 * 3改掉找座位bug
 * 3座位按线程分组
 * 3增加查询二级索引
 * 3增加的座位结构互斥，使用总读写锁
 * 4去掉读写锁设置和组互斥，使用总互斥
 * 4使用真正的位图存储座位
 * 分支1函数手动内联展开
 */


public class TicketingDSSafe implements TicketingSystem
{
	//车站信息
	int routenum = 5;//车次数
	int coachnum = 8;//车厢数
	int seatnum = 100;//座位数
	int stationnum = 10;//站台数
	int threadnum = 16;//线程数
	//辅助结构
	AtomicLong ticketCnt = new AtomicLong(1);//车票号计数
	int groupNum;//分组数量
	long[][][] arrSeat;//按组座位bitmap
	int[] groupIdx, groupSize;//组索引值
	int seatBound, groupBound, groupSize1, groupSize2;//组分段总座位数边界，组分段组号边界，一段组大小，二段组大小
	AtomicInteger[][][] idxInqAll;//总查询索引，每个元素(i, j)意味着(i, j)比(i, j-1)多的座位数
	int[][][][] idxInqGroup;//组查询索引，每个元素(i, j)意味着(i, j)比(i, j-1)多的座位数
	Map<Long, IdMsg> mapId;//id数据记录
	//互斥结构
	//synchronized(arrSeat[route])，车次，负责锁住组座位和组查询索引
	//synchronized(mapId)，哈希表锁
	//初始化
	void OtherInit()
	{
		//组座位记录初始化
		int allSeat = coachnum*seatnum;
		groupNum = (int)(Math.sqrt(allSeat)/2);
		arrSeat = new long[routenum][groupNum][];
		groupIdx = new int[groupNum];
		groupSize = new int[groupNum];
		groupBound = allSeat%groupNum;
		groupSize1 = allSeat/groupNum+1;
		groupSize2 = allSeat/groupNum;
		seatBound = groupBound*groupSize1;
		for(int route=0; route<routenum; ++route) {
			int tmpSum = 0;
			for(int i=0; i<coachnum*seatnum%groupNum; ++i) {
				int tmp = coachnum*seatnum/groupNum+1;
				arrSeat[route][i] = new long[tmp];
				groupIdx[i] = tmpSum;
				groupSize[i] = tmp;
				tmpSum += tmp;
			}
			for(int i=coachnum*seatnum%groupNum; i<groupNum; ++i) {
				int tmp = coachnum*seatnum/groupNum;
				arrSeat[route][i] = new long[tmp];
				groupIdx[i] = tmpSum;
				groupSize[i] = tmp;
				tmpSum += tmp;
			}
		}
		//id哈希表初始化
		mapId = new HashMap<Long, IdMsg>();
		//查询结构初始化
		idxInqAll = new AtomicInteger[routenum][stationnum-1][];
		idxInqGroup = new int[routenum][groupNum][stationnum-1][];
		for(AtomicInteger[][] arr: idxInqAll) {
			for(int i=0; i<stationnum-1; ++i) {
				arr[i] = new AtomicInteger[stationnum-1-i];
				arr[i][0] = new AtomicInteger(allSeat);
				for(int k=1; k<stationnum-1-i; ++k)
					arr[i][k] = new AtomicInteger(0);
			}
		}
		for(int route=0; route<routenum; ++route) {
			for(int group=0; group<groupNum; ++group) {
				for(int i=0; i<stationnum-1; ++i) {
					idxInqGroup[route][group][i] = new int[stationnum-1-i];
					idxInqGroup[route][group][i][0] = groupSize[group];
				}
			}
		}
		//debug
		//System.out.println("groupNum: "+groupNum);
	}
	//构造函数
	public TicketingDSSafe()
	{
		OtherInit();
	}
	public TicketingDSSafe(int routenum, int coachnum, int seatnum, int stationnum, int threadnum)
	{
		this.routenum = routenum;
		this.coachnum = coachnum;
		this.seatnum = seatnum;
		this.stationnum = stationnum;
		this.threadnum = threadnum;
		OtherInit();
	}
	//组号与车号转换
	public int[] GroupToCoach(int group, int idx)
	{
		int[] res = new int[2];
		int cnt = groupIdx[group]+idx;
		res[0] = cnt/seatnum;
		res[1] = cnt%seatnum;
		return res;
	}
	public int[] CoachToGroup(int coachZero, int seatZero)
	{
		int[] res = new int[2];
		int cnt = coachZero*seatnum+seatZero;
		if(cnt<seatBound) {
			res[0] = cnt/groupSize1;
			res[1] = cnt%groupSize1;
		}
		else {
			res[0] = groupBound+(cnt-seatBound)/groupSize2;
			res[1] = (cnt-seatBound)%groupSize2;
		}
		return res;
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
	//调试组索引
	void DebugCoachGroup()
	{
		int cnt = 0;
		for(int group=0; group<groupNum; ++group) {
			for(int idx=0; idx<groupSize[group]; ++idx) {
				int res[] = GroupToCoach(group, idx);
				if(cnt!=res[0]*seatnum+res[1])
					throw new RuntimeException();
				int res2[] = CoachToGroup(res[0], res[1]);
				if(res2[0]!=group || res2[1]!=idx)
					throw new RuntimeException();
				++ cnt;
			}
		}
	}
	//购票函数辅助
	boolean BuyTicketAssist(Ticket ticket)
	{
		boolean bBuy = false;
		//循环查找
		Random rand = new Random();
		int initGroup = rand.nextInt(groupNum);
		int initIdx = rand.nextInt(Math.max(1, groupSize2));
		int findGroup = -1;
		int findIdx = -1;
		int[] sted = null;
		long target = CreateSeatBitmap(ticket.departure, ticket.arrival);
		//组循环
		loop:
		for(int i=initGroup; i<initGroup+groupNum; ++i) {
			int group = i%groupNum;
			if(CalInq(idxInqGroup[ticket.route-1][group], ticket.departure, ticket.arrival)==0)
				continue;
			for(int j=initIdx; j<initIdx+groupSize[group]; ++j) {
				int idx = j%groupSize[group];
				//找到处理
				if((arrSeat[ticket.route-1][group][idx]&target)==0) {
					//记录
					bBuy = true;
					findGroup = group;
					findIdx = idx;
					//改变座位和组查询内容
					arrSeat[ticket.route-1][group][idx] ^= target;
					sted = CalStedBitmap(arrSeat[ticket.route-1][findGroup][findIdx],
							ticket.departure, ticket.arrival);
					ChangeInq(idxInqGroup[ticket.route-1][group], sted, ticket.departure, ticket.arrival, true);
					break loop;
				}
			}
		}
		//判断查找结果
		if(bBuy) {
			//票存储
			ticket.tid = ticketCnt.getAndIncrement();
			int[] res = GroupToCoach(findGroup, findIdx);
			ticket.coach = res[0]+1;
			ticket.seat = res[1]+1;
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
		//锁定车次
		synchronized(arrSeat[route-1]) {
			if(CalInqAtomic(idxInqAll[route-1], departure, arrival)>0)
				res = BuyTicketAssist(ticket);
		}
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
			int[] res = CoachToGroup(msg.coach-1, msg.seat-1);
			int[] sted = null;
			long target = CreateSeatBitmap(ticket.departure, ticket.arrival);
			//锁定车次
			synchronized(arrSeat[msg.route-1]) {
				//改变座位和查询内容
				arrSeat[msg.route-1][res[0]][res[1]] ^= target;
				sted = CalStedBitmap(arrSeat[msg.route-1][res[0]][res[1]],
						msg.departure, msg.arrival);
				ChangeInq(idxInqGroup[msg.route-1][res[0]], sted, msg.departure, msg.arrival, false);
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
		}
		//否则等待其他线程退票
		else {
			while(!msg.bFinish.get())
				;
			return false;
		}
	}
}
