package ticketingsystem;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class TicketingDS implements TicketingSystem
{
	AtomicLong globalTid=new AtomicLong(1);
	int routenum = 5;
	int coachnum = 8;
	int seatnum  = 100;
	int stationnum = 10;
	int[][][][] train;
	ReentrantLock[] lockArray;
	public void init()
	{
		for (int i=0; i<=routenum; i++)
		{
			for(int j=0; j<=coachnum; j++)
			{
				for(int k=0; k<=seatnum; k++)
				{
					for(int l=0; l<=stationnum; l++)
						train[i][j][k][l] = 0;
				}
			}
		}
		
	}
	public TicketingDS()
	{
		train = new int[routenum+1][coachnum+1][coachnum+1][stationnum+1];
		init();
		lockArray = new ReentrantLock[routenum+1];
		for (int i=0; i<=routenum; i++)
		{
			lockArray[i] = new ReentrantLock();
		}
	}
	public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum,int _threadnum)
	{
		this.routenum = routenum;
		this.coachnum = coachnum;
		this.seatnum = seatnum;
		this.stationnum = stationnum;
		train = new int[routenum+1][coachnum+1][seatnum+1][stationnum+1];
		init();
		lockArray = new ReentrantLock[routenum+1];
		for (int i=0; i<=routenum; i++)
		{
			lockArray[i] = new ReentrantLock();
		}
	}

	//查询余票
	public int inquiry(int route, int start, int end) {
		if(route<1 || route>routenum || start<1 || start>stationnum || end<start || end>stationnum)
			return 0;
			int count=0;
			for(int coach=1; coach<=coachnum;coach++)
			{
				for(int seat=1; seat<=seatnum; seat++)
				{
					boolean flag = true;
					for(int i=start; i<end; i++)
					{
						if(train[route][coach][seat][i] != 0)
						{
							flag =false;
							break;
						}
					}
					if (flag)
						count++;
				}
			}
			return count;
	
	}
	//买票
	public Ticket buyTicket(String passenger, int route, int departure,	int arrival) {
		if(route<1 || route>routenum ||	departure<1 || departure>stationnum || arrival<departure ||arrival>stationnum)
			return null;
		Ticket ticket;
		lockArray[route].lock();
		try{
			int myTid = (int)globalTid.getAndIncrement();
			//搜索哪一个座位可以用
			for(int i=1; i<=coachnum; i++)
			{
				for(int j=1; j<=seatnum; j++)
				{
					boolean flag = true;
					for(int k=departure; k<arrival; k++)
					{
						if(train[route][i][j][k] != 0)
						{
							flag =false;
							break;
						}
					}
					//满足购票要求，更新座位
					if(flag)
					{
						for(int m=departure; m<arrival; m++)
						{
							train[route][i][j][m] = myTid;
						}
						Ticket myTicket = new Ticket();
						myTicket.tid=myTid;
						myTicket.passenger=passenger;
						myTicket.route=route;
						myTicket.coach=i;
						myTicket.seat=j;
						myTicket.departure=departure;
						myTicket.arrival=arrival;
						return myTicket;
					}						
				}
			}
			return null;
		}
		finally{
			lockArray[route].unlock();
		}
	}

	//退票
	public boolean refundTicket(Ticket ticket) {

		if(ticket.tid<1 || ticket.tid>globalTid.get() || ticket.route<1 || ticket.route>routenum ||	ticket.coach<1 || ticket.coach>coachnum ||
		ticket.seat<1 || ticket.seat>seatnum ||	ticket.departure<1 || ticket.departure>stationnum || ticket.arrival<ticket.departure || ticket.arrival>stationnum)
			return false;
		// 加锁
		lockArray[ticket.route].lock();
		try{
			long myTid = ticket.tid;
			String myName = ticket.passenger;
			int myRoute = ticket.route;
			int myCoach = ticket.coach;
			int mySeat = ticket.seat;
			int myDeparture = ticket.departure;
			int myArrival = ticket.arrival;
			boolean flag = true;
			for(int i=myDeparture; i<myArrival; i++)
			{
				if(train[myRoute][myCoach][mySeat][i] != myTid)
				{
					flag = false;
					break;
				}			
			}
			if(myDeparture>1)
			{
				if(train[myRoute][myCoach][mySeat][myDeparture-1] == myTid)
				{
					flag = false;
			    }			  
			}
			   
			if(train[myRoute][myCoach][mySeat][myArrival] == myTid)
			{
				flag = false; 
			}
			//满足退票要求，更新座位
			if(flag)
			{				
				for(int i=myDeparture; i<myArrival; i++)
				{
					train[myRoute][myCoach][mySeat][i] = 0;
				}
			}
			return flag;
		}
		finally{
			lockArray[ticket.route].unlock();
		}
	}
}