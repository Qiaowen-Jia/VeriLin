package ticketingsystem;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.*;

class Ticket{
        long tid;
        String passenger;
        int route;
        int coach;
        int seat;
        int departure;
        int arrival;
}


class Block{
	int blockSeatNum;//块的座位数
	int startSeat;//块的第一个座位下标
	int avaliableSeatNum; //剩余票数
	ReentrantReadWriteLock locks=new ReentrantReadWriteLock();
	public Block(int blockSeatNum,int startSeat) {
		this.blockSeatNum=blockSeatNum;
		this.startSeat=startSeat;
		this.avaliableSeatNum=blockSeatNum;
	}
}

class Section{
	Block[] blockArray;
	boolean[] seatArray;
	public Section() {}
	public Section(int stationnum,int coachnum,int seatnum,int blockSeatNum,int mid,int midStartSeat,int rm) {
		//blockArray
		Block[] blkArr=new Block[stationnum-1];
		int start=0;
		for(int k=0;k<stationnum-1;k++) {
			Block blk;
			if(k==mid) {
				blk=new Block(blockSeatNum+rm,start);
			}
			else {
				blk=new Block(blockSeatNum,start);
			}
			blkArr[k]=blk;
			start +=blk.blockSeatNum;
		}
		this.blockArray=blkArr;
		//seatArray
		this.seatArray=new boolean[coachnum*seatnum];
	}
}

class Route{
	Section[] sectionArray; //段数组
	ConcurrentLinkedDeque<Ticket> ticketSold; //已购票数组
	Zone[][] zones; //乘车区域数组

	public Route() {}
	public Route(int stationnum,int coachnum,int seatnum,int blockSeatNum,int mid,int midStartSeat,int rm) {
		//sectionArray
		Section[] secArr=new Section[stationnum-1];
		for(int s=0;s<stationnum-1;s++) {
			Section section=new Section(stationnum,coachnum,seatnum,blockSeatNum,mid,midStartSeat,rm);
			secArr[s]=section;
		}
		this.sectionArray=secArr;
		//ticketSold
		ticketSold=new ConcurrentLinkedDeque<Ticket>();
		//Zone
		zones=new Zone[stationnum-1][stationnum-1];
		for(int i=0;i<stationnum-1;i++) {
			for(int j=0;j<stationnum-1;j++) {
				zones[i][j]=new Zone(coachnum*seatnum);
			}
		}
		
	}
}

class FullBlock{
	int section; //段下标
	int block; //块下标
	public FullBlock(int sec,int blk) {
		section=sec;
		block=blk;
	}
}

class Column{
	ReentrantReadWriteLock locks;
	public Column(){
		locks=new ReentrantReadWriteLock();
	}
}

class CoachAndSeat{
	int coach; //车厢号(1开始)
	int seat; //车厢座位号(1开始)
}

class Zone{
	int num; //剩余票数
	ReentrantReadWriteLock locks;
	public  Zone() {
		locks=new ReentrantReadWriteLock();
	}
	public Zone(int num) {
		this.num=num;
		locks=new ReentrantReadWriteLock();
	}
}

class ZoneUpDown{
	int up;
	int down;
	public ZoneUpDown(int up,int down) {
		this.up=up;
		this.down=down;
	}
}

//public  class TicketingDS implements TicketingSystem {
public  class TicketingDS {
	int routeNum;
	int coachNum;
	int seatNum;
	int stationNum;
	int threadNum;
	int totalSeatNum;
	int blockNum;
	Route[] routeArray;
	int mid; //中间块的下标
	int midStartSeatIdx;//中间块的最后一个座位下标
	int midEndSeatIdx;//中间块的最后一个座位下标
	int blockSeatNum;//每个块的座位数
	//int SeldTicketNum;
	AtomicInteger seldTicketNum=new AtomicInteger(1);
		
	public TicketingDS(int routenum,int coachnum,int seatnum,int stationnum,int threadnum) {
		routeNum=routenum;
		coachNum=coachnum;
		seatNum=seatnum;
		stationNum=stationnum;
		blockNum=stationnum;
		threadNum=threadnum;
		//分块
		routeArray=new Route[routeNum]; //车次数组
		totalSeatNum=coachNum*seatNum;
		blockSeatNum=totalSeatNum/(stationNum-1);
		int rm=totalSeatNum%(stationNum-1);
		mid=(stationNum-1)/2;
		midStartSeatIdx=mid*blockSeatNum;
		midEndSeatIdx=midStartSeatIdx+blockSeatNum+rm-1;
		routeArray=new Route[routeNum];
		for(int r=0;r<routeNum;r++) {
			Route rt=new Route(stationnum,coachnum,seatnum,blockSeatNum,mid,midStartSeatIdx,rm);
			routeArray[r]=rt;
		}
	}
	
    public int inquiry(int route, int departure, int arrival) {
    	if(!checkRequest(route,departure,arrival)) {
			return 0;
		}
    	Route rt=routeArray[route-1]; //车次
		int depIdx=departure-1; //第一个区间下标
		int avlIdx=arrival-2;//最后一个区间下标
		Zone zone=rt.zones[depIdx][avlIdx];
		zone.locks.readLock().lock();
		int num=zone.num;
		zone.locks.readLock().unlock();
		return num;
	}	
	
	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {		
		if(!checkRequest(route,departure,arrival)) {
			return null;
		}
		Route rt=routeArray[route-1]; //车次
		int depIdx=departure-1; //第一个区间下标
		int avlIdx=arrival-1;//最后一个区间下标
		//记录扫描过程中无空座位的块
		List<FullBlock> list=new ArrayList<FullBlock>();
		Section[] secArr=rt.sectionArray;//区间数组
		Section sec=secArr[depIdx];; //区间
		Block blk=sec.blockArray[avlIdx-1];
		int secIdx;
		int blkIdx;
		int b;
		//看有没有余票数不为0的块
		for(b=0;b<stationNum-1;b++) {
			//扫描这个块的全部区间
			for(secIdx=depIdx;secIdx<avlIdx;secIdx++) {
				sec=secArr[secIdx];
				blk=sec.blockArray[b];
				blk.locks.readLock().lock(); //获取块的读锁
				//这个块没有空座位了，其余区间没有必要扫描了
				if(blk.avaliableSeatNum==0) { 
					list.add(new FullBlock(secIdx,b));
					//释放之前区间块的读锁，本区间块的读锁不释放(没有空座位了，对其他买票线程无影响，对退票线程有影响)
					for(int s=depIdx;s<secIdx;s++) {
						blk=secArr[s].blockArray[b];
						blk.locks.readLock().unlock();
					}
					break;
				}
			}
			//相等说明这个块所有区间都有空座(但不代表有票)
			if(secIdx==avlIdx) {
				blkIdx=b;
				int emptySeatIdx=FindEmptySeat(depIdx,avlIdx,blkIdx,rt,list);
				//有票(已上写锁，肯定能买到票)
				if(emptySeatIdx!=-1) {
					//修改座位
					for(int i=depIdx;i<avlIdx;i++) {
						sec=rt.sectionArray[i];
						sec.seatArray[emptySeatIdx]=true;
						blk=sec.blockArray[blkIdx];
						blk.avaliableSeatNum--;
					}
					//对Zones加锁
					ZoneUpDown ud=lockZones(emptySeatIdx,departure,arrival,rt);
					//修改Zones
					decZonesNum(departure,arrival,ud.up,ud.down,rt);
					//释放Zones锁
					unlockZones(departure,arrival,ud.up,ud.down,rt);
					//释放全部块锁
					for(int s=depIdx;s<avlIdx;s++) {
						blk=secArr[s].blockArray[blkIdx];
						blk.locks.writeLock().unlock();
					}
					//释放之前区间块的读锁
					unlockFullBlock(secArr,list);
					//买票
					Ticket ticket=new Ticket();
					//SeldTicketNum++;
					ticket.tid=seldTicketNum.getAndIncrement();
					ticket.passenger=passenger;
					ticket.route=route;
					ticket.departure=departure;
					ticket.arrival=arrival;
					CoachAndSeat cs=calculateCoachAndSeat(emptySeatIdx);
					ticket.coach=cs.coach;
					ticket.seat=cs.seat;
					rt.ticketSold.add(ticket);
					return ticket;
				}
				//没票,释放这个块的所有写锁，扫描下一个块
			}
			//不相等说明这个块没票了，继续扫描下一个块
		}
		unlockFullBlock(secArr,list);
		return null;		
	}
	
	public boolean refundTicket(Ticket ticket){
		if(!checkRefund(ticket)) {
			return false;
		}
		int routeIdx=ticket.route-1;
		Route rt=routeArray[routeIdx];
		int depIdx=ticket.departure-1;
		int avlIdx=ticket.arrival-1;
		int coachIdx=ticket.coach-1;
		int seatIdx=coachIdx*seatNum+ticket.seat-1;
		int blkIdx=getBlockIndex(seatIdx);
		Section[] secArr=rt.sectionArray;
		Block blk;
		//获得块锁
		for(int secIdx=depIdx;secIdx<avlIdx;secIdx++) {
			blk=secArr[secIdx].blockArray[blkIdx];
			blk.locks.writeLock().lock(); 
		}
		//修改：块票数。区间票数。座位
		for(int s=depIdx;s<avlIdx;s++) {
			Section section=secArr[s];
			//Sect sect=rt.sectList.get(s);
			blk=secArr[s].blockArray[blkIdx];
			blk.avaliableSeatNum++;
			section.seatArray[seatIdx]=false;
		}
		//Zone上锁
		ZoneUpDown ud=lockZones(seatIdx,ticket.departure,ticket.arrival,rt);
		//修改Zone
		incZonesNum(ticket.departure,ticket.arrival,ud.up,ud.down,rt);
		//释放Zone锁
		unlockZones(ticket.departure,ticket.arrival,ud.up,ud.down,rt);
		//释放全部块锁
		for(int secIdx=depIdx;secIdx<avlIdx;secIdx++) {
			blk=secArr[secIdx].blockArray[blkIdx];
			blk.locks.writeLock().unlock();
		}
		rt.ticketSold.remove(ticket);
		return true;
	}
	
	//获取空座位
	int FindEmptySeat(int depIdx,int avlIdx,int blkIdx,Route route,List<FullBlock> list) {
		Route rt=route;
		Section sec=rt.sectionArray[depIdx];
		Block blk=sec.blockArray[blkIdx];
		
		int start=blk.startSeat;
		int end=start+blk.blockSeatNum;
		//释放读锁
		for(int i=depIdx;i<avlIdx;i++) {
			sec=rt.sectionArray[i];
			blk=sec.blockArray[blkIdx];
			blk.locks.readLock().unlock();
		}
		//获取写锁
		for(int i=depIdx;i<avlIdx;i++) {
			sec=rt.sectionArray[i];
			blk=sec.blockArray[blkIdx];
			blk.locks.writeLock().lock();
		}
		//扫描全部座位找到一个空座
		for(int s=start;s<end;s++) {
			int i=depIdx;
			for(;i<avlIdx;i++) {
				sec=rt.sectionArray[i];
				if(sec.seatArray[s]) { //这个座位在该区间已经被占了
					break;
				}
			}
			if(i==avlIdx) {
				return s;
			}
		}
		//块写锁降级
		for(int i=depIdx;i<avlIdx;i++) {
			sec=rt.sectionArray[i];
			blk=sec.blockArray[blkIdx];
			blk.locks.readLock().lock();
			blk.locks.writeLock().unlock();
			list.add(new FullBlock(i,blkIdx));
		}
		return -1;
	}	
	
	//求所在块下标，seatIdx为整个车厢座位的下标，从0开始
	int getBlockIndex(int seatIdx) {
		int blkIdx;
		if(seatIdx>midEndSeatIdx) {
			blkIdx=mid+(seatIdx-midEndSeatIdx-1)/blockSeatNum+1;
		}
		else if(seatIdx>=midStartSeatIdx){
			blkIdx=mid;
		}
		else {
			blkIdx=seatIdx/blockSeatNum;
		}
		return blkIdx;
	}	
		
	
	void unlockFullBlock(Section[] secArr,List<FullBlock> list) {
		list.forEach(item->{
			Block blk=secArr[item.section].blockArray[item.block];
			blk.locks.readLock().unlock();
		});
	}
	
	//根据seatIdx计算票的车厢号和座位号，从1开始
	//seatIdx为整个车厢座位的下标，从0开始
	CoachAndSeat calculateCoachAndSeat(int seatIdx) {
		CoachAndSeat cs=new CoachAndSeat();
		int a=(seatIdx+1)%seatNum;
		//能整除
		if(a==0) {
			cs.seat=seatNum;
			cs.coach=(seatIdx+1)/seatNum;
		}
		//不能整除
		else{
			cs.seat=a;
			cs.coach=(seatIdx+1)/seatNum+1;
		}
		return cs;
	}
	
	//必须先修改座位再修改Zones
	//zones[i][j]=(i+1)->(j+2)
	ZoneUpDown lockZones(int seatIdx,int departure,int arrival,Route rt) {
		int fstSectIdx=departure-1;
		int sndSectIdx=arrival-2;
		Zone[][] zone=rt.zones;
		Section sect;
		//向上遍历
		int up=fstSectIdx;
		if(up!=0) {
			for(up=fstSectIdx-1;up>=0;up--) {
				sect=rt.sectionArray[up];
				if(sect.seatArray[seatIdx]) {
					break;
				}
			}
			up++;
		}
		//向下遍历
		int down=sndSectIdx;
		if(down!=stationNum-2) {
			for(down=sndSectIdx+1;down<stationNum-1;down++) {
				sect=rt.sectionArray[down];
				if(sect.seatArray[seatIdx]) {
					break;
				}
			}
			down--;
		}
		//上锁
		for(int i=up;i<fstSectIdx;i++) {
			for(int j=fstSectIdx;j<=down;j++) {
				zone[i][j].locks.writeLock().lock();
			}
		}
		for(int i=fstSectIdx;i<=sndSectIdx;i++) {
			for(int j=i+1;j<=down;j++) {
				zone[i][j].locks.writeLock().lock();
			}
		}
		for(int i=fstSectIdx;i<=sndSectIdx;i++) {
			zone[i][i].locks.writeLock().lock();
		}
		ZoneUpDown upDown=new ZoneUpDown(up,down);
		return upDown;
	}
	
	void unlockZones(int departure,int arrival,int up,int down,Route rt) {
		int fstSectIdx=departure-1;
		int sndSectIdx=arrival-2;
		Zone[][] zone=rt.zones;
		//释放锁
		for(int i=up;i<fstSectIdx;i++) {
			for(int j=fstSectIdx;j<=down;j++) {
				zone[i][j].locks.writeLock().unlock();
			}
		}
		for(int i=fstSectIdx;i<=sndSectIdx;i++) {
			for(int j=i+1;j<=down;j++) {
				zone[i][j].locks.writeLock().unlock();
			}
		}
		for(int i=fstSectIdx;i<=sndSectIdx;i++) {
			zone[i][i].locks.writeLock().unlock();
		}
	}
	
	void incZonesNum(int departure,int arrival,int up,int down,Route rt) {
		int fstSectIdx=departure-1;
		int sndSectIdx=arrival-2;
		Zone[][] zone=rt.zones;
		for(int i=up;i<fstSectIdx;i++) {
			for(int j=fstSectIdx;j<=down;j++) {
				zone[i][j].num++;
			}
		}
		for(int i=fstSectIdx;i<=sndSectIdx;i++) {
			for(int j=i+1;j<=down;j++) {
				zone[i][j].num++;
			}
		}
		for(int i=fstSectIdx;i<=sndSectIdx;i++) {
			zone[i][i].num++;
		}
	}
	
	void decZonesNum(int departure,int arrival,int up,int down,Route rt) {
		int fstSectIdx=departure-1;
		int sndSectIdx=arrival-2;
		Zone[][] zone=rt.zones;
		for(int i=up;i<fstSectIdx;i++) {
			for(int j=fstSectIdx;j<=down;j++) {
				zone[i][j].num--;
			}
		}
		for(int i=fstSectIdx;i<=sndSectIdx;i++) {
			for(int j=i+1;j<=down;j++) {
				zone[i][j].num--;
			}
		}
		for(int i=fstSectIdx;i<=sndSectIdx;i++) {
			zone[i][i].num--;
		}
	}
	
	boolean checkTicket(int route,int depart,int arrival,int coach,int seat) {
		if(route>routeNum||route<1) {
			return false;
		}
		if(depart<1) {
			return false;
		}
		if(arrival>stationNum) {
			return false;
		}
		if(depart>=arrival) {
			return false;
		}
		if(coach>coachNum||coach<1) {
			return false;
		}
		if(seat>seatNum||seat<1) {
			return false;
		}
		return true;
	}
	
	boolean checkRequest(int route,int depart,int arrival) {
		if(route>routeNum||route<1) {
			return false;
		}
		if(depart<1) {
			return false;
		}
		if(arrival>stationNum) {
			return false;
		}
		if(depart>=arrival) {
			return false;
		}
		return true;
	}
	
	boolean checkRefund(Ticket ticket) {
		ConcurrentLinkedDeque<Ticket> tks= routeArray[ticket.route-1].ticketSold;
		for(Ticket item:tks) {
			if(ticket.passenger.equals(item.passenger) &&
					   ticket.tid==item.tid &&
					   ticket.departure==item.departure &&
					   ticket.arrival==item.arrival &&
					   ticket.coach==item.coach &&
					   ticket.seat==item.seat){
						return true;
					}
		}
		return false;
	}
}
