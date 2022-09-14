package ticketingsystem;

import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TicketingDS implements TicketingSystem 
{

	int routenum, coachnum, seatnum, stationnum;
	AtomicLong tid;	//表示车票序号
	//定义四维大小的数组，依次表示routenum, coachnum, seatnum,stationnum,取值为0、1表示是否有人，例如
	//seat_state[1][5][12][6] = 1表示1号车次的5车12车厢在从第6站到第7站是已被占有的，购票会失败，如果为0
	//就购票成功，如果为1即购票失败
	int[][][][] seat_state;
	//乐观锁实现，对每一个座位都定义一个显式锁
	Lock lock_seat [][][];
	//int remains[][];	//第一维表示车次，第二维表示从index站到index+1站的剩余票数
	//使用原子更新整数数组，避免因为多个线程同时对票数进行操作而引发错误,整体可以看作是一个二维数组
	AtomicIntegerArray remains[];



	// 构造函数
	public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum)
	{
		this.routenum = routenum;
		this.coachnum = coachnum;
		this.seatnum = seatnum;
		this.stationnum = stationnum;
		tid = new AtomicLong(0);	//初试为0，用于生成唯一的票的编号
		
		//初始化并更新剩余票数数组
		this.remains = new AtomicIntegerArray[routenum+1];
		for(int i = 1; i <= routenum; i++)
		{
			int [] value = new int[stationnum];
			for(int j = 1; j < stationnum; j++)
			{
				value[j] = coachnum * seatnum;
			}
			remains[i] = new AtomicIntegerArray(value);
		}
		
		//分配座位状态数组，状态为0表示车票avaliable
		seat_state = new int[routenum + 1][coachnum + 1][seatnum + 1][stationnum];
		for(int i = 1; i <= routenum; i++)
		{
			for(int j = 1; j <= coachnum; j++)
			{
				for(int k = 1; k <= seatnum; k++)
				{
					for(int r = 1; r < stationnum; r++)
					{
						seat_state[i][j][k][r] = 0;
					}
				}
			}
		}

		// 初始化lock_seat数组，声明显式乐观锁
		lock_seat = new Lock[routenum + 1][coachnum + 1][seatnum + 1];
		for(int i = 1; i <= routenum; i++)
		{
			for(int j = 1; j <= coachnum; j++)
			{
				for(int k = 1; k <= seatnum; k++)
				{
					lock_seat[i][j][k] = new ReentrantLock();
				}
			}
		}

	}

	@Override
	public Ticket buyTicket(final String passenger, final int route, final int departure, final int arrival) 
	{
		boolean isAvaliable = true;
		//根据购票要求查询是否乘客乘车区间是否有票
		Ticket tic;
		for(int i = 1; i <= this.coachnum; i++)
		{
			for(int j = 1; j <= this.seatnum; j++)
			{
				isAvaliable = true;
				//检查当前座位在乘客指定的乘车区间内是否有票
				for(int k = departure; k < arrival; k++)
				{
					if(this.seat_state[route][i][j][k] == 0)
					{
						isAvaliable = true;
					}
					else
					{
						isAvaliable = false;
						break;
					}
				}
				if(!isAvaliable)
				{
					continue;
				}
				//如果查询成功，则对该座位上锁
				this.lock_seat[route][i][j].lock();

				//重新检测该座位是否被其他线程修改
				boolean isChanged = false;
				for(int k = departure; k < arrival; k++)
				{
					if(this.seat_state[route][i][j][k] != 0)
					{
						isChanged = true;
					}
				}
				if(isChanged)	//如果当前位置状态被其他线程修改了，则释放锁并继续查找下一个座位
				{
					this.lock_seat[route][i][j].unlock();
					continue;
				}
			
				//生成ticket
				tic = new Ticket();
				tic.tid = this.tid.getAndIncrement();
				tic.passenger = passenger;
				tic.route = route;
				tic.departure = departure;
				tic.arrival = arrival;
				tic.coach = i;
				tic.seat = j;

				//修改seat_state数组，表示当前座位已经售出去了
				for(int k = departure; k < arrival; k++)
				{
					this.seat_state[route][i][j][k] = 1;
				}

				//更新票数数组
				for(int k = departure; k < arrival; k++)
				{
					remains[route].getAndDecrement(k);
				}

				this.lock_seat[route][i][j].unlock();

				if(tic != null) 
				{
					return tic;
				}
			}
		}
		return null;
	}

	@Override
	public int inquiry(final int route, final int departure, final int arrival) 
	{
		int avaliableTicNum = Integer.MAX_VALUE;
		for(int i = departure; i < arrival; i++)
		{
			avaliableTicNum = Math.min(this.remains[route].get(i), avaliableTicNum);
		}
		return avaliableTicNum;
	}

	@Override
	public boolean refundTicket(final Ticket ticket) 
	{
		
		this.lock_seat[ticket.route][ticket.coach][ticket.seat].lock();
		//判断当前票据是否有效
		boolean isValid = true;
		for(int k = ticket.departure; k < ticket.arrival; k++)
		{
			if(this.seat_state[ticket.route][ticket.coach][ticket.seat][k] == 0)
			{
				isValid = false;
				break;
			}
		}
		if(!isValid)
		{
			this.lock_seat[ticket.route][ticket.coach][ticket.seat].unlock();
			return false;	//退票失败
		}
		// 如果可以退票，更新票数信息
		for(int k = ticket.departure; k < ticket.arrival; k++)
		{
			this.seat_state[ticket.route][ticket.coach][ticket.seat][k] = 0;
		}

		// 修改rest数组，更新剩余的票数信息
		for(int i = ticket.departure; i < ticket.arrival; i++)
			this.remains[ticket.route].getAndIncrement(i);

		this.lock_seat[ticket.route][ticket.coach][ticket.seat].unlock();

		return true;
	}
}
