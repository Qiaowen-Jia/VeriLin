package ticketingsystem;

import java.util.concurrent.locks.ReentrantLock;

public class Train2 {
	Seat[][] seats;
	int coachnum;
	int seatnum;
	int stationnum;
	int threadnum;
	int route;
	int[] leftticketsnum;
	
	public int computeIndex(int departure,int arrival)
	{
		return arrival-departure-1 + ((((stationnum<<1) - departure) * (departure-1))>>1);
	}
	Train2(int coachnum,int seatnum,int stationnum,int threadnum,int route)
	{
		this.coachnum = coachnum;
		this.seatnum = seatnum;
		this.stationnum = stationnum;
		this.threadnum = threadnum;
		this.route = route;
		leftticketsnum = new int[ (stationnum*(stationnum-1)) / 2];
		for(int j = 0 ; j< (stationnum*(stationnum-1)) / 2 ;j++)
		{
			leftticketsnum[j] = coachnum*seatnum;
		}
		seats = new Seat[coachnum][seatnum];
		for(int i=0;i<coachnum;i++)
		{
			for(int j=0;j<seatnum;j++)
			{
				seats[i][j] = new Seat(stationnum);
			}
		}
	}
	void reducetickets(int coach, int seat, int departure, int arrival) {
		// TODO Auto-generated method stub
		int d = departure;
		int a = arrival;
		while(d>=2)
		{
			int testnum = constructBinary(d-2, d-2);
			if((testnum & seats[coach-1][seat-1].status) == 0)
				break;
			d--;
		}
		while(a<stationnum)
		{
			int testnum = constructBinary(a-1,a-1);
			if((testnum & seats[coach-1][seat-1].status) == 0)
				break;
			a++;
		}
		for(int i=d;i<arrival;i++)
		{
			int j = i+1>departure+1?i+1:departure+1;
			while(j<=a)
			{
				if(leftticketsnum[computeIndex(i, j)]>0)
				{
					leftticketsnum[computeIndex(i, j)]--;
				}
				j++;
			}
		}
	}
	void addtickets(int coach, int seat, int departure, int arrival) {
		// TODO Auto-generated method stub
		int d = departure;
		int a = arrival;
		while(d>=2)
		{
			int testnum = constructBinary(d-2, d-2);
			if((testnum & seats[coach-1][seat-1].status) == 0)
				break;
			d--;
		}
		while(a<stationnum)
		{
			int testnum = constructBinary(a-1,a-1);
			if((testnum & seats[coach-1][seat-1].status) == 0)
				break;
			a++;
		}
		for(int i=d;i<arrival;i++)
		{
			int j = i+1>departure+1?i+1:departure+1;
			while(j<=a)
			{
				leftticketsnum[computeIndex(i, j)]++;
				j++;
			}
		}
	}
	int getValidticketnum(int departure,int arrival)
	{
		synchronized(this)
		{
			return leftticketsnum[computeIndex(departure, arrival)];
		}
	}
	void refundticket(int coach,int seat,int departure,int arrival)
	{
		synchronized (this) {
			recyle(coach, seat, departure, arrival);
			addtickets(coach,seat,departure,arrival);
		}
	}
	Ticket buyticket(String passenger,int departure,int arrival)
	{
		Ticket ticket = null;
		if(leftticketsnum[computeIndex(departure,arrival)]==0)
		{
			//synchronized(this)
			//{
			//if(leftticketsnum[computeIndex(departure,arrival)]==0)
			return null;
			//}
		}

		int i=0,j=0;
		synchronized (this) {
			GET: for (i = 0; i < coachnum; i++) {
				for (j = 0; j < seatnum; j++) {
					boolean available = isvalid(i + 1, j + 1, departure, arrival);
					if (available) {
						// synchronized(this)
						{
							//available = isvalid(i + 1, j + 1, departure, arrival);
							//if (available) {
								sell(i + 1, j + 1, departure, arrival);
								reducetickets(i + 1, j + 1, departure, arrival);
								break GET;
							//}
						}
					}

				}
			}
		}
		if(i!=coachnum)
		{
			Long ticId = TicketingDS.countId.get() * threadnum + ThreadId.get();
			TicketingDS.countId.set(TicketingDS.countId.get()+1);
			ticket = new Ticket(ticId,passenger,route,i+1,j+1,departure,arrival);
		}
		return ticket;
	}
	class Seat
	{
		int status;
		ReentrantLock lock;
		Seat(int stationnum)
		{
			status = constructBinary(0,stationnum-2);
			lock = new ReentrantLock();
		}
	}
	int constructBinary(int low,int high)
	{
		int b = 0xffffffff;
		int mask = 0xffffffff;
		mask >>>= (32-high-1);
		b &= mask;
		mask = 0xffffffff;
		mask <<= (low);
		b &= mask;
		return b;
	}
	boolean isvalid(int coach,int seat,int departure,int arrival)
	{
		int testnum = constructBinary(departure-1,arrival-2);
		testnum = testnum | seats[coach-1][seat-1].status;
		testnum = testnum ^ seats[coach-1][seat-1].status;
		if(testnum == 0)
			return true;
		else 
			return false;
	}
	void sell(int coach,int seat,int departure,int arrival)
	{
		int mask = constructBinary(departure-1,arrival-2);
		mask = ~mask;
		seats[coach-1][seat-1].status &= mask;
	}
	void recyle(int coach,int seat,int departure,int arrival)
	{
		int mask = constructBinary(departure-1,arrival-2);
		seats[coach-1][seat-1].status |= mask;
	}
}
