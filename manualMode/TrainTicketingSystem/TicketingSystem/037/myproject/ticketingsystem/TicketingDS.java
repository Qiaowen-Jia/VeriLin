package ticketingsystem;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class TicketingDS implements TicketingSystem {
	int coachnum;
	int threadnum;
	int routenum;
	int seatnum;
	int stationnum ;
	int name;
	Map<Long, Ticket>sold;
	route[] train;
	AtomicIntegerArray tidd;

	TicketingDS (int routenum,int coachnum ,int seatnum ,int stationnum , int threadnumn){
		this.threadnum=threadnumn;
		this.routenum=routenum;
		this.seatnum=seatnum;
		this.stationnum=stationnum ;
		this.coachnum=coachnum;
		this.train=new route[this.routenum];
		this.sold = new ConcurrentHashMap<Long, Ticket>();
		this.tidd = new AtomicIntegerArray(routenum*(coachnum)*(seatnum));
		for(int m=0;m<routenum;m++) {
			train[m]=new route(routenum, coachnum , seatnum , stationnum ,  threadnumn);
		}
		
		
		for(int i=0;i<routenum*coachnum*seatnum;i++) {
			this.tidd.set(i, 0);
		}

	}


	class route{
		AtomicIntegerArray sell;
		Avail[] avail;
	
		
		//初始化类链表，供购买车票的sell、提供查询的avail、储存已经贩卖的车票的sold
		route(int routenum,int coachnum ,int seatnum ,int stationnum ,int threadnum){
			this.sell = new AtomicIntegerArray((coachnum)*(seatnum));
			
			//初始化sell并发哈希表，这里存放着所有的座位(key--id)以及对应的状态(value--state)
			//(1)座位(id)由一个五位的整数表示，其高两位表示座位所属的车厢号，低三位表示座位在该车厢的标号；
			//      id="车厢号"+"车厢内座位编号"
			//(2)状态(state)指的是一个二进制的数字，其从左往右数的第i位表示对于该座位从第i站到第i+1站是否空闲，
			//若总站数为stationnum，那么该状态的有效位为从右往左数到第stationnum-1位。
			for(int i=0;i<coachnum*seatnum;i++) {
				this.sell.set(i, (1<<(stationnum-1))-1);
			}

			//this.maxorder=(stationnum-1+1)*(stationnum-1)/2;
			this.avail = new Avail[stationnum];
			//初始化avail对象数组，每一个对象avail[i]代表着以i+1为起始站点的所有乘车区间段；
			for(int i1=0;i1<stationnum;i1++) {
				this.avail[i1]=new Avail(coachnum,seatnum,i1,stationnum);
			}

		}
	}

	//train[route-1].avail[i]
	class Avail{
		End[] end;
		Avail( int coachnum,int seatnum,int start,int stationnum ) {
			//start ----- 对应于上级对象数组 avail的下标，即每个avail所对应的起始站点-1；
			//对于对象avail[i],其包含的对象数组end[]中的每一个元素都对应着一个以i+1起始的乘车区段；
			//end[]中的元素个数因avail的不同而变化。
			//例如，对于avail[2],那么其包含的end对象分别对应的乘车区段为（假设总共有5站,其中i表示第几站)
			//end[0] ----- 2->3
			//end[1] ----- 2->4
			//end[2] ----- 2->5
			//注意:i从0开始，车站编号从1开始。
			int total_end=(stationnum-1)-(start);
			//System.out.printf("start is %d,total end is %d\n", start,total_end);
			this.end=new End[total_end+1];
			for(int i=0;i<=total_end;i++) {
				this.end[i]=new End(coachnum,seatnum);
			}
		}
	}

	//train[route-1].avail[i].end[j]
	class End{
		Map<Integer,Integer>endmap;
		End(int coachnum,int seatnum){
			this.endmap=new ConcurrentHashMap<Integer, Integer>(coachnum*seatnum);
			for(int i=1;i<=coachnum;i++) {
				for(int j=1;j<=seatnum;j++) {
					//仅键值有用，键值定义为
					//key -> id -----i.e.coach_id*1000+seat_id
					//id -> "车厢"+"座位"
					//用来检索是否在这个end对象表示的乘车区间内，存在id对应的座位；
					//若存在，那么说明对于这个乘车区间，id对应的座位可用。
					this.endmap.put(Integer.valueOf(i*1000+j), Integer.valueOf(0));
				}
			}
		}
	}

	int tractGenerate(int stationnum,int departure,int arrival) {
		if(arrival==stationnum) {
			return((1<<((stationnum-1)-(departure-1)))-1);
		}else {
			return (((1<<((stationnum-1)-(departure-1)))-1)&(~((1<<((stationnum-1)-(arrival-1)))-1)));
		}
	}


	@Override
	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
		//Random random = new Random();
		if(departure>=arrival || departure>=stationnum || arrival>stationnum )return null;
		int i2=0;
		//System.out.printf("route is %d,departure is %d,arrival is %d\n", route,departure,arrival);
		for(int key : train[route-1].avail[departure-1].end[arrival-1-departure].endmap.keySet()) {
			i2=(key/1000-1)*this.seatnum+key%1000-1;
			break;
		}
		
		//int i2 = random.nextInt((this.coachnum-1)*(this.seatnum-1));
		//int j2 = random.nextInt((this.coachnum-1)*(this.seatnum-1));//this.coachnum*this.seatnum);
		int index;
		int tract=tractGenerate(this.stationnum,departure,arrival);
		for(int iout=0;iout<2;iout++) {
			if(iout==0) {
				index=i2;
			}else {
				index=0;
			}
			while(index<(this.coachnum)*(this.seatnum)) {
				if(iout==1&&index==i2)break;
				while(true) {
					int value=this.train[route-1].sell.get(index);
					if(Integer.compare(tract,(value&tract))==0) {//找到座位是否对应区段空闲，若不是，退出true循环，寻找下一个座位
						boolean k=this.train[route-1].sell.compareAndSet( index, value,value&(~(tract)) );
						if(k) {
							int i=index/seatnum+1;
							int j=index%seatnum+1;
							int id=i*1000+j;
							//更改座位状态是否成功，失败则重新检测座位是否空闲
							changeAvail(id,route,departure,arrival,0);//座位状态修改成功,修改avail数组
							while(true) {
								//System.out.printf("buy key is %d\n",key);
								Ticket ticket=tickedGenerate(id,passenger,route,departure,arrival);
								if(addSold(ticket)) {//如果添加成功，那么就返回ticket对象，失败说明tid重复，重新生成tid
									return ticket;
								}
							}
						}	
					}else {
						break;
					}
				}
				index++;
			}
		}
		return null;
	}




	private boolean addSold(Ticket ticket) {
		if(this.sold.putIfAbsent(ticket.tid, ticket)==null) {
			return true;
		}else {
			return false;
		}

	}



	private Ticket tickedGenerate(Integer key, String passenger,int route, int departure, int arrival) {
		//锁座位成功，并且已经修改了avail，现在生成ticket对象；
		//tid的组成 id(5位)+stamp(10位)+随机数(4位)
		//注:long型数据类型最长为19位，最高两位最大为92，认为车厢不会长于92节，故满足。
		//stamp使用10位，可以保证在15天以内（stamp）没有重复，符合火车票预售15天，精确到毫秒。
		//Long timeStamp = (long)(System.currentTimeMillis()%(Math.pow(10, 10)));
		int tid_index=(route-1)*this.coachnum*this.seatnum+(key/1000-1)*this.seatnum+key%1000-1;
        int low=this.tidd.getAndAdd(tid_index, 1);
		long tid=(long)(Math.pow(10,12 )*key)+low+(long)(Math.pow(10,14 )*route);
		Ticket ticket=new Ticket();
		ticket.tid=tid;
		ticket.passenger=passenger;
		ticket.route=route;
		ticket.coach=(int)(key/1000);
		ticket.seat=(int)(key%1000);
		ticket.departure=departure;
		ticket.arrival=arrival;
		//System.out.printf("generate coach is %d,seat is %d\n",ticket.coach,ticket.seat);
		return ticket;
	}


	public void changeAvail(Integer key,int route,int departure,int arrival,int bool ) {
		if(bool==0) {//买票时对avail的修改
			for(int i=0;i<=arrival-2;i++) {
				if(i+1<departure) {
					for(int j=departure-(i+1);j<((stationnum-1)-i);j++) {
						// System.out.printf("in changeAvail,i is %d,j is %d\n", i,j);
						Integer k = train[route-1].avail[i].end[j].endmap.remove(key);
					}
				}else {
					for(int j=0;j<this.stationnum-1-i;j++) {
						train[route-1].avail[i].end[j].endmap.remove(key);
					}
				}
			}	   
		}else {//退票时对avail的修改
			int tract1=tractGenerate(this.stationnum,departure,arrival);
			//System.out.printf("tract1 is %d\n", tract1);
			int tract;
			int index=(key/1000-1)*this.seatnum+key%1000-1;
			for(int i=0;i<=arrival-2;i++) {
				if((i+1)<departure) {
					for(int j=departure-1;j<this.stationnum-1-i;j++) {
						tract=tractGenerate(this.stationnum,i+1,j+2);
						int mmm=((this.train[route-1].sell.get(index)|tract1)&tract);
						if(mmm==tract) {
							this.train[route-1].avail[i].end[j].endmap.putIfAbsent(key, Integer.valueOf(0));					   }
					}
				}else {
					for(int j=0;j<this.stationnum-1-i;j++) {
						//System.out.printf("i is %d,j is %d\n", i,j);
						tract=tractGenerate(this.stationnum,i+1,j+2);
						//System.out.printf("key is %d,tract is %d,value is %d\n", key,tract,this.train[route-1].sell.get(key));
						//因为退票对应座位的状态还没有修改，所以需要|操作把所退座位状态改正一下
						if(((this.train[route-1].sell.get(index)|tract1)&tract)==tract) {
							this.train[route-1].avail[i].end[j].endmap.putIfAbsent(key, Integer.valueOf(0));
						}
					}
				}
			}

		}

	}


	@Override
	public int inquiry(int route, int departure, int arrival) {
		return this.train[route-1].avail[departure-1].end[(arrival-1)-departure].endmap.size();
	}




	@Override
	public boolean refundTicket(Ticket ticket) {
		if(this.sold.remove(ticket.tid,ticket)) {
			int id=ticket.coach*1000+ticket.seat;
			//System.out.printf("coach is %d,seat is %d\n",ticket.coach,ticket.seat);
			int tract=tractGenerate(this.stationnum,ticket.departure,ticket.arrival);
			changeAvail(Integer.valueOf(id),ticket.route,ticket.departure,ticket.arrival,1);
			changeSeatSate(id,tract,ticket.route);
			return true;
		}
		return false;
	}


	private void changeSeatSate(int id, int tract,int route) {
		int index=(id/1000-1)*this.seatnum+id%1000-1;
		int m = this.train[route-1].sell.get(index);
		int ksjkwk=0;
		ksjkwk=ksjkwk+1;
		while(!this.train[route-1].sell.compareAndSet(index, m, m | tract)) {
			m = this.train[route-1].sell.get(index);
		}
	}
}


