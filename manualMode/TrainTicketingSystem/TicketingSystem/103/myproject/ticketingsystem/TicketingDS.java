package ticketingsystem;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

final class CONSTANT
{
	static int THREADNUM = 32;
	static int THREADOP = 12500;
	static int ROUTENUM = 5;
	static int COACHNUM = 8;
	static int SEATNUM = 100;
	static int STATIONNUM = 10;
}
public class TicketingDS implements TicketingSystem {
	private int routenum;
	private int coachnum;
	private int seatnum;
	private int stationnum;
	private int threadnum;
	private AtomicLong nextTid = new AtomicLong();
	private Station[][] seatManagement;//route station
	private SeatStation[][][][] seatResources;//route coach seat station 
	//尝试为每一个route分别设置查询cache,保存之前的查询结果,一致性由购票,退票操作维护,即购票、退票前先将cache中内容无效
	private Map<InquiryUnit, Integer> []inquiryBuffer;
	private class InquiryUnit
	{
		public int departure;
		public int arrival;
		public InquiryUnit(int departure, int arrival) 
		{
			this.arrival = arrival;
			this.departure = departure;
		}
	}
	//Map<Long,Ticket> tickets = new ConcurrentHashMap<Long,Ticket>();
	private class Station//表示一条路线上两个站间的情况
	{
		Set<Integer> emptySet = new HashSet<Integer>();//Collections.synchronizedSet(new HashSet<Integer>());//该站间的余票
		//Set<Integer> occupiedSet = new HashSet<Integer>();//Collections.synchronizedSet(new HashSet<Integer>());//该站间已售出的票
		ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		Lock rLock = lock.readLock();
		Lock wLock = lock.writeLock();
		public Station(int coachnum, int seatnum)
		{
			//occupiedSet.clear();
			for(int i = 0; i < coachnum ; i++)
			{
				for(int j = 0; j < seatnum ; j++)
				{
					emptySet.add(j + seatnum * i);
				}
			}
		}
		
	}
	private class SeatStation
	{
		boolean occupy;//是否被占
		long tid = -1;//被哪张车票占用
		String passenger;//乘车人
		public SeatStation() 
		{
			this.occupy = false;
		}
	}
	public TicketingDS() 
	{
		super();
		this.seatnum = CONSTANT.SEATNUM;
		this.coachnum = CONSTANT.COACHNUM;
		this.routenum = CONSTANT.ROUTENUM;
		this.stationnum = CONSTANT.STATIONNUM - 1;
		this.threadnum = CONSTANT.THREADNUM;
		nextTid.set(0);
		inquiryBuffer = new ConcurrentHashMap[routenum];
		for(int i = 0; i < routenum; i++)
		{
			inquiryBuffer[i] = new ConcurrentHashMap<InquiryUnit,Integer>();
		}
		initTicket();
		
	}
	public TicketingDS (int routenum,int coachnum,int seatnum,int stationnum,int threadnum) 
	{
		super();
		this.routenum = routenum;
		this.coachnum = coachnum;
		this.seatnum = seatnum;
		this.stationnum = stationnum - 1;
		this.threadnum = threadnum;
		nextTid.set(0);
		inquiryBuffer = new ConcurrentHashMap[routenum];
		for(int i = 0; i < routenum; i++)
		{
			inquiryBuffer[i] = new ConcurrentHashMap<InquiryUnit,Integer>();
		}
		initTicket();
	}
	public Ticket buyTicket(String passenger, int route, int departure, int arrival)
	{
		//调整departure和arrival的大小关系
		if((departure - arrival) > 0)
		{
			int t = departure;
			departure = arrival;
			arrival = t;
		}
		//检查购票信息是否符合语义
		if(!checkSemantic(passenger,route - 1,departure - 1,arrival - 1))
		{
			System.out.println("invaild sematic");
			return null;
		}
		//保存交集中间结果的集合
		Set<Integer>retain = new HashSet<Integer>();//Collections.synchronizedSet(new HashSet<Integer>());
		retain.clear();
		for(int i = 0; i < coachnum ; i++)
		{
			for(int j = 0; j < seatnum ; j++)
			{
				retain.add(j + i*seatnum);
			}
		}
		boolean isOccupied = false;
		//初始化ticket,确定能够占座后才为ticket获取id
		Ticket ticket = new Ticket();
		ticket.passenger = passenger;
		ticket.route = route;
		ticket.arrival =arrival;
		ticket.departure = departure;
		//对每个站进行检查
		for(int i = departure; i < arrival; i++)
		{
			//获取所需站的写锁
			seatManagement[route - 1][i - 1].wLock.lock();
			//从departure到当前站的可用座位
			retain.retainAll(seatManagement[route - 1][i - 1].emptySet);
			//retain为空表示没有合适的座位
			if(retain.isEmpty())
			{
				isOccupied = true;
				for(int j = departure; j < i + 1; j++)
				{
					seatManagement[route - 1][j - 1].wLock.unlock();
				}
				break;
			}
		}
		//无余座，购票失败
		if(isOccupied)
		{
			return null;
		}
		//有空座
		//获取tid
		//设置management中的set
		//设置resources中的属性 occupy passenger 和 tid
		Iterator<Integer> iterator = retain.iterator();
		int idx = iterator.next();
		int nowCoach = idx / seatnum;
		int nowSeat = idx % seatnum;
		ticket.tid = nextTid.getAndIncrement();
		ticket.seat = nowSeat + 1;
		ticket.coach = nowCoach + 1;
		//修改前维护cache
		if(!inquiryBuffer[route - 1].isEmpty())
		{
			inquiryBuffer[route - 1].clear();
		}
		for(int i = departure; i < arrival; i++)
		{
			seatResources[route - 1][nowCoach][nowSeat][i - 1].occupy = true;
			seatResources[route - 1][nowCoach][nowSeat][i - 1].passenger = ticket.passenger;
			seatResources[route - 1][nowCoach][nowSeat][i - 1].tid = ticket.tid;
			seatManagement[route - 1][i - 1].emptySet.remove(idx);
			//seatManagement[route - 1][i - 1].occupiedSet.add(idx);
			seatManagement[route - 1][i - 1].wLock.unlock();
		}
		return ticket;
	}
	//是可线性化的吗？线性化点在哪里？
	public int inquiry(int route, int departure, int arrival)
	{
		//检查购票信息是否符合语义
		if(!checkSemantic("inquiry",route - 1,departure - 1,arrival - 1))
		{
			System.out.println("invaild sematic");
			return -1;
		}
		int emptySeat = 0;
		//调整departure,arrival的大小关系,保证arrival大
		if((departure - arrival) > 0)
		{
			int t = departure;
			departure = arrival;
			arrival = t;
		}
		//先查cache
		if(inquiryBuffer[route - 1].containsKey(new InquiryUnit(departure, arrival)))
		{
			return inquiryBuffer[route - 1].get(new InquiryUnit(departure, arrival));
		}
		//获取各个statio上的余票数,最小者是区间上可以换票时的余票
		Set<Integer>retain = new HashSet<Integer>();//Collections.synchronizedSet(new HashSet<Integer>());
		retain.clear();
		for(int i = 0; i < coachnum ; i++)
		{
			for(int j = 0; j < seatnum ; j++)
			{
				retain.add(j + i*seatnum);
			}
		}
		for(int i = departure; i < arrival ; i ++)
		{
			seatManagement[route - 1][i - 1].rLock.lock();
			retain.retainAll(seatManagement[route - 1][i - 1].emptySet);
			if(retain.isEmpty())
			{
				for(int j = departure; j < i + 1; j++)
				{
					seatManagement[route - 1][j - 1].rLock.unlock();
				}
				return 0;
			}
		}
		emptySeat = retain.size();
		//更新cache
		inquiryBuffer[route - 1].put(new InquiryUnit(departure, arrival), emptySeat);
		for(int i  = departure ; i < arrival ; i++)
		{
			seatManagement[route - 1][i - 1].rLock.unlock();
		}
		return emptySeat;
	}
	public boolean refundTicket(Ticket ticket)
	{
		if(ticket == null)
		{
			return false;
		}
		int departure = ticket.departure;
		int arrival = ticket.arrival;
		//调整departure，arrival的大小
		if((departure - arrival) > 0)
		{
			int t = departure;
			departure = arrival;
			arrival = t;
		}
		//检查退票的合法性
		for(int i = departure; i < arrival ; i++)
		{
			seatManagement[ticket.route - 1][i - 1].wLock.lock();
			if(seatResources[ticket.route - 1][ticket.coach - 1][ticket.seat - 1][i - 1].occupy
			&& seatResources[ticket.route - 1][ticket.coach - 1][ticket.seat - 1][i - 1].tid  == ticket.tid
			&& seatResources[ticket.route - 1][ticket.coach - 1][ticket.seat - 1][i - 1].passenger == ticket.passenger)
			{
				continue;
			}
			//非法的退票
			for(int j = departure; j < i + 1 ; j++)
			{
				seatManagement[ticket.route - 1][j - 1].wLock.unlock();
			}
			return false;
		}
		//信息无误,开始退票
		//先维护cache
		if(!inquiryBuffer[ticket.route - 1].isEmpty())
		{
			inquiryBuffer[ticket.route - 1].clear();
		}
		for(int i = departure; i < arrival ; i++)//首次进入时为可线性化点?
		{
			seatResources[ticket.route - 1][ticket.coach - 1][ticket.seat - 1][i - 1].occupy = false;
			seatManagement[ticket.route - 1][i - 1].emptySet.add((ticket.coach - 1) * seatnum + ticket.seat - 1);
			//seatManagement[ticket.route - 1][i - 1].occupiedSet.remove((ticket.coach - 1) * seatnum + ticket.seat - 1);
			seatManagement[ticket.route - 1][i - 1].wLock.unlock();
		}
		return true;
	}
	private boolean checkSemantic(String passenger, int route, int departure, int arrival) 
	{
		if( Integer.toUnsignedLong(route) > this.routenum//route 在0和routenum之间
			|| Integer.toUnsignedLong(departure) > this.stationnum//departure 在0和stationnum之间
			|| Integer.toUnsignedLong(arrival) > this.stationnum//arrival 在0和stationnum之间
			|| passenger.isEmpty()//passenger非空
			|| departure == arrival//departure不等于arrival
			)
		{
			return false;
		}
		return true;
	}
	private void initTicket() 
	{
		this.seatManagement = new Station[routenum][stationnum];
		this.seatResources = new SeatStation[routenum][coachnum][seatnum][stationnum];
		for(int i = 0; i < routenum; i++)
		{
			for(int j = 0; j < stationnum; j++) 
			{
				seatManagement[i][j] = new Station(coachnum,seatnum);
			}
		}
		for(int i = 0;i < routenum;i++)
		{
			for(int j = 0;j < coachnum; j++)
			{
				for(int k = 0; k < seatnum;k++)
				{
					for(int l = 0; l < stationnum; l++)
					{
						seatResources[i][j][k][l] = new SeatStation();
					}
				}
			}
		}
	}
}
