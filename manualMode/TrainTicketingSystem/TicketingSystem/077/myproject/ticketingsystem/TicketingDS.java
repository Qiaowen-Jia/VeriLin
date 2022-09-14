package ticketingsystem;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.lang.InterruptedException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.TimeUnit;

class Ticket{
        long tid;
        String passenger;
        int route;
        int coach;
        int seat;
        int departure;
        int arrival;
}


class SimpleReadWriteLock implements ReadWriteLock{
	int readers;
	boolean writer;
	Lock lock;
	public Lock read_lock;
	public Lock write_lock;
	Condition condition;
	public SimpleReadWriteLock(){
		writer=false;
		readers=0;
		lock=new ReentrantLock();
		read_lock=new ReadLock();
		write_lock=new WriteLock();
		condition=lock.newCondition();
	}
	class ReadLock implements Lock{
		public void lock(){
			lock.lock();
			try{
				while(writer)
				{
					synchronized(condition){
					condition.await();
					}
				}
				readers++;
			}catch(InterruptedException e){
				System.out.println("wrong");
			}finally{
				lock.unlock();
			}
		}
		public void unlock(){
			lock.lock();
			try{
				readers--;
				if(readers==0)
				{
					synchronized(condition) {
						condition.signalAll();
					}
				}
			}finally{
				lock.unlock();
			}
		}
		public Condition newCondition()
		{
			return null;
		}
		public boolean tryLock(long timeout,TimeUnit unit)
		{
			return true;
		}
		public boolean tryLock()
		{
			return true;
		}
		public void lockInterruptibly()
		{

		}
	}
	class WriteLock implements Lock{
		public void lock(){
			lock.lock();
			try{
				while(writer || readers!=0)
				{
					synchronized(condition){
					condition.await();
					}
				}
				writer=true;
			}catch(InterruptedException e){
				System.out.println("wrong");
			}finally{
				lock.unlock();
			}
		}
		public void unlock(){
			writer=false;
			synchronized(condition) {
				condition.signalAll();
			}
		}
		public Condition newCondition()
		{
			return null;
		}
		public boolean tryLock(long timeout,TimeUnit unit)
		{
			return false;
		}
		public boolean tryLock()
		{
			return false;
		}
		public void lockInterruptibly()
		{

		}
	}
	public Lock writeLock()
	{
		return write_lock;
	}
	public Lock readLock()
	{
		return read_lock;
	}
}
class state_of_seat
{
	int[] oneseat;
	state_of_seat(int stationnum)
	{
		oneseat=new int[stationnum+1];
	}
}
class state_of_route
{
	Random rand=new Random();
	private ArrayList<Ticket> sold_tickets=new ArrayList<Ticket>();
	//SimpleReadWriteLock lock = new SimpleReadWriteLock();
	ReentrantReadWriteLock  lock = new ReentrantReadWriteLock ();
	state_of_seat[][] seats;
	int stationnum;
	int coachnum;
	int seatnum;
	state_of_route(int a,int b,int c)
	{
		stationnum=a;
		coachnum=b;
		seatnum=c;

		seats=new state_of_seat[coachnum+1][seatnum+1];
		for(int i=1;i<=coachnum;i++)
		{
			for(int j=1;j<=seatnum;j++)
			{
				seats[i][j]=new state_of_seat(stationnum);
			}
		}
	}
	public Ticket buyTicket(String passenger,int route,int departure,int arrival)
	{
		lock.writeLock().lock();
		int flag;
		for(int i=1;i<=coachnum;i++)
		{
			for(int j=1;j<=seatnum;j++)
			{
				flag=0;
				for(int k=departure;k<arrival;k++)
				{
					if(seats[i][j].oneseat[k]==1)
					{
						flag=1;
						break;
					}
				}
				if(flag==0)
				{
					for(int k=departure;k<arrival;k++)
					{
						seats[i][j].oneseat[k]=1;
					}
					Ticket buy_ticket=new Ticket();
					buy_ticket.tid=rand.nextLong();
					buy_ticket.passenger=passenger;
					buy_ticket.route=route;
					buy_ticket.coach=i;
					buy_ticket.seat=j;
					buy_ticket.departure=departure;
					buy_ticket.arrival=arrival;
					sold_tickets.add(buy_ticket);
					lock.writeLock().unlock();
					return buy_ticket;
				}
			}
		}
		lock.writeLock().unlock();
		return null;
	}
	public boolean refundTicket(Ticket ticket)
	{

		lock.writeLock().lock();
		long tid=ticket.tid;
		int coach=ticket.coach;
		int seat=ticket.seat;
		int departure=ticket.departure;
		int arrival=ticket.arrival;

		int size=sold_tickets.size();
		for(int i=0;i<size;i++)
		{
			if(tid==(sold_tickets.get(i)).tid && coach==(sold_tickets.get(i)).coach && seat==(sold_tickets.get(i)).seat && departure==(sold_tickets.get(i)).departure && arrival==(sold_tickets.get(i)).arrival)
			{
				for(int j=departure;j<arrival;j++)
				{
					seats[coach][seat].oneseat[j]=0;
				}
				sold_tickets.remove(i);
				lock.writeLock().unlock();
				return true;
			}
		}
		lock.writeLock().unlock();
		return false;
	}
	public int inquiry(int departure,int arrival)
	{
		lock.readLock().lock();
		int flag;
		int count=0;
		for(int i=1;i<=coachnum;i++)
		{
			for(int j=1;j<=seatnum;j++)
			{
				flag=0;
				for(int k=departure;k<arrival;k++)
				{
					if(seats[i][j].oneseat[k]==1)
					{
						flag=1;
						break;
					}
				}
				if(flag==0)
				{
					count++;
				}
			}
		}
		lock.readLock().unlock();
		return count;
	}
}

//public class TicketingDS implements TicketingSystem 
public class TicketingDS 
{
	private int routenum,coachnum,seatnum,stationnum,threadnum;
	private state_of_route[] routes;
	TicketingDS(int a,int b,int c,int d,int e)
	{
		routenum=a;
		coachnum=b;
		seatnum=c;
		stationnum=d;
		threadnum=e;

		routes=new state_of_route[routenum+1];
		for(int i=0;i<=routenum;i++)
		{
			routes[i]=new state_of_route(stationnum,coachnum,seatnum);
		}
	}

	public Ticket buyTicket(String passenger, int route, int departure, int arrival)
	{
		return routes[route].buyTicket(passenger,route,departure,arrival);
	}

	public int inquiry(int route, int departure, int arrival)
	{
		return routes[route].inquiry(departure,arrival);
	}

	public boolean refundTicket(Ticket ticket)
	{
		return routes[ticket.route].refundTicket(ticket);
	}
}

