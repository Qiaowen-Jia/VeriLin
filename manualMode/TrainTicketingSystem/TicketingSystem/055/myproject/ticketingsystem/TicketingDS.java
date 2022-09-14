package ticketingsystem;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

public class TicketingDS implements TicketingSystem {

    private static final class Route{
		private static interface Task{}
		private static final class Buy implements Task{
			public final int from, to;
			public volatile int ret=-2;
			public Buy(int from, int to){
				this.from=from;
				this.to=to;
			}
		}
		private static final class Ref implements Task{
			public final int index, from, to;
			public volatile boolean ret=false;
			public Ref(int index, int from, int to){
				this.index=index;
				this.from=from;
				this.to=to;
			}
		}
		private static final class Emp implements Task{
			public volatile boolean ret=false;
			public Emp(){}
		}
		private final AtomicReference<long[]> info;
		private long[] newInfo=null;
		private final ConcurrentLinkedQueue<Task> task_que=new ConcurrentLinkedQueue<Task>();
		public Route(int seat_count, int station_count){
			if(station_count>16||station_count<0)
				throw new InvalidParameterException("stationnum should not be larger than 16");
			if(seat_count<0)
			    throw new InvalidParameterException("Invalid train shape");
			int info_size=seat_count/4;
			long stations=(1L<<station_count)-1;
			long[] nfo=new long[info_size+1];
			Arrays.fill(nfo,~((stations<<48)|(stations<<32)|(stations<<16)|stations));
			nfo[info_size]|=~((1L<<((seat_count%4)<<4))-1);
			info=new AtomicReference<long[]>(nfo);
		}
		public int queryAvailableCnt(int from, int to){
			long[] snapshot=info.get();
			long mask1=((1L<<(to-from+1))-1)<<from;
			long mask2=mask1<<16;
			long mask3=mask1<<32;
			long mask4=mask1<<48;
			int ret=Arrays.stream(snapshot).unordered().mapToInt(x -> {
				return
				    (0==(mask4&x)?1:0)+
				    (0==(mask3&x)?1:0)+
				    (0==(mask2&x)?1:0)+
				    (0==(mask1&x)?1:0);
			}).sum();
			return ret;
		}
		public int buyAvailableTicket(int from, int to){
			Buy op=new Buy(from,to);
			task_que.add(op);
			synchronized(this){
				int ret=op.ret;
				if(ret==-2)
					applyTasks();
				return op.ret;
			}
		}
		public void refundTicket(int index, int from, int to){
			Ref op=new Ref(index,from,to);
			task_que.add(op);
			synchronized(this){
				if(!op.ret)
					applyTasks();
			}
		}
		public void emptyOperation(){
			Emp op=new Emp();
			task_que.add(op);
			synchronized(this){
				if(!op.ret)
					applyTasks();
			}
		}
		private void applyTasks(){
			begin();
			Task tk;
			while(null!=(tk=task_que.poll())){
				if(tk instanceof Buy){
					Buy b=(Buy) tk;
					b.ret=_buyAvailableTicket(b.from, b.to);
				}else if(tk instanceof Ref){
					Ref r=(Ref) tk;
					_refundTicket(r.index, r.from, r.to);
					r.ret=true;
				}else{
					Emp e=(Emp) tk;
					e.ret=true;
				}
			}
			commit();
		}
		private void begin(){
			long[] t=info.get();
			newInfo=new long[t.length];
			System.arraycopy(t, 0, newInfo, 0, t.length);
		}
		private int _buyAvailableTicket(int from, int to){
			long mask1=((1L<<(to-from+1))-1)<<from;
			long mask2=mask1<<16;
			long mask3=mask1<<32;
			long mask4=mask1<<48;
			OptionalInt oi=IntStream.range(0, newInfo.length).unordered().filter(i -> {
				long lc=newInfo[i];
				return 0==(mask4&lc)||0==(mask3&lc)||0==(mask2&lc)||0==(mask1&lc);
			}).findAny();
			if(!oi.isPresent())
				return -1;
			int index=oi.getAsInt();
			long lc=newInfo[index];
			if(0==(mask1&lc)){
				newInfo[index]|=mask1;
				return index<<2;
			}
			if(0==(mask2&lc)){
				newInfo[index]|=mask2;
				return 1+(index<<2);
			}
			if(0==(mask3&lc)){
				newInfo[index]|=mask3;
				return 2+(index<<2);
			}
			//if(0==(mask4&lc)){
				newInfo[index]|=mask4;
				return 3+(index<<2);
			/*}
			assert(false);
			return -1;*/
		}
		private void _refundTicket(int index, int from, int to){
			long mask=~(((1L<<(to-from+1))-1)<<from<<((index&3)<<4));
			newInfo[index>>2]&=mask;
		}
		private void commit(){
			info.set(newInfo);
			newInfo=null;
		}
	}

	private static final class TicketSupport{
		public static final Ticket newTicket(long tid, String passenger, int route, int coach, int seat, int departure, int arrival){
			Ticket tk=new Ticket();
			tk.tid=tid;
			tk.passenger=passenger;
			tk.route=route;
			tk.coach=coach;
			tk.seat=seat;
			tk.departure=departure;
			tk.arrival=arrival;
			return tk;
		}
		public static final Ticket newTicket(Ticket o){
			Ticket tk=new Ticket();
			tk.tid=o.tid;
			tk.passenger=o.passenger;
			tk.route=o.route;
			tk.coach=o.coach;
			tk.seat=o.seat;
			tk.departure=o.departure;
			tk.arrival=o.arrival;
			return tk;
		}
		public static final boolean isSame(Ticket t1, Ticket t2){
			return t1==t2||(t1.tid==t2.tid&&t1.route==t2.route&&t1.coach==t2.coach&&t1.seat==t2.seat&&t1.departure==t2.departure&&t1.arrival==t2.arrival&&(t1.passenger==t2.passenger||(t1.passenger!=null&&t1.passenger.equals(t2.passenger))));
		}
	}

	TicketingDS(){
		this(5);
	}
	TicketingDS(int routenum){
		this(routenum, 8);
	}
	TicketingDS(int routenum, int coachnum){
		this(routenum, coachnum, 100);
	}
	TicketingDS(int routenum, int coachnum, int seatnum){
		this(routenum, coachnum, seatnum, 10);
	}
	TicketingDS(int routenum, int coachnum, int seatnum, int stationnum){
		this(routenum, coachnum, seatnum, stationnum, 16);
	}
	TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum){
		routes=new Route[routenum];
		for(int i=routenum-1;i>=0;--i)
			routes[i]=new Route(seatnum*coachnum, stationnum);
		this.seatnum=seatnum;
		this.stationnum=stationnum;
	}

	private final int seatnum, stationnum;
	private final Route[] routes;
	private final ConcurrentHashMap<Long,Ticket> ticketList=new ConcurrentHashMap<Long,Ticket>();
	private final long tidGap=1L<<32;
	private final AtomicLong tidBase=new AtomicLong(1);
	private final ThreadLocal<Long> tidCounter=ThreadLocal.withInitial(() -> tidBase.getAndAdd(tidGap));

	private boolean invalidParam(int route, int departure, int arrival){
		return route<1||route>routes.length||departure<1||departure>stationnum||arrival<1||arrival>stationnum||departure>=arrival;
	}

	@Override
	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
		if(invalidParam(route, departure, arrival))
			return null;
		long tid=tidCounter.get();
		if(tid%tidGap==0)
			tidCounter.set(tidBase.getAndAdd(tidGap));
		else
		    tidCounter.set(tid+1);
		int ret=routes[route-1].buyAvailableTicket(departure-1, arrival-2);
		if(ret==-1)
			return null;
		Ticket tk=TicketSupport.newTicket(tid, passenger, route, (ret/seatnum)+1, (ret%seatnum)+1, departure, arrival);
		ticketList.put(tid, tk);
		return TicketSupport.newTicket(tk);
	}

	@Override
	public int inquiry(int route, int departure, int arrival) {
		if(invalidParam(route, departure, arrival))
			return 0;
		return routes[route-1].queryAvailableCnt(departure-1, arrival-2);
	}

	@Override
	public boolean refundTicket(Ticket ticket) {
		if(ticket==null)
		    return false;
		Ticket tk=ticketList.get(ticket.tid);
		if(tk==null)
			return false;
		if(!TicketSupport.isSame(tk, ticket))
			return false;
		if(!ticketList.remove(ticket.tid, tk)){
			routes[ticket.route-1].emptyOperation();
			return false;
		}
		routes[ticket.route-1].refundTicket((ticket.coach-1)*seatnum+ticket.seat-1, ticket.departure-1, ticket.arrival-2);
		return true;
	}
}
