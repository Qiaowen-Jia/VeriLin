package ticketingsystem;

import java.util.*;


//线程执行类
class ThreadRun implements Runnable
{
	int threadnum; // concurrent thread number
	int routenum; // route is designed from 1 to 5
	int coachnum; // coach is arranged from 1 to 8
	int seatnum; // seat is allocated from 1 to 100
	int stationnum; // station is designed from 1 to 10

	int testnum ;
	int retpc; // return ticket operation is 10% percent
	int buypc; // buy ticket operation is 30% percent
	int inqpc; //inquiry ticket operation is 60% percent
	
	TicketingSystem tds;
	
	boolean bOut;//结果输出
	boolean bSingleRight;//单线程正确性测试
	
	boolean[][][][] arrSeat;//辅助测试，座位bitmap
	
	ThreadRun(int threadnum, int routenum, int coachnum, int seatnum, int stationnum, TicketingSystem tds,
			int testnum, int retpc, int buypc, int inqpc, boolean bOut, boolean bSingleRight)
	{
		this.threadnum = threadnum;
		this.routenum = routenum;
		this.coachnum = coachnum;
		this.seatnum = seatnum;
		this.stationnum = stationnum;
		this.tds = tds;
		this.testnum = testnum;
		this.retpc = retpc;
		this.buypc = buypc;
		this.inqpc = inqpc;
		this.bOut = bOut;
		this.bSingleRight = bSingleRight;
		arrSeat = new boolean[routenum][coachnum][seatnum][stationnum-1];
	}

	//买票成功函数
	void BuySuccessDeal(Ticket ticket)
	{
		for(int k=ticket.departure-1; k<ticket.arrival-1; ++k) {
			if(arrSeat[ticket.route-1][ticket.coach-1][ticket.seat-1][k])
				throw new RuntimeException();
			arrSeat[ticket.route-1][ticket.coach-1][ticket.seat-1][k] = true;
		}
	}
	//买票失败测试处理
	void BuyFailDeal(int route, int departure, int arrival)
	{
		//循环查找
		for(int i=0; i<coachnum; ++i) {
			for(int j=0; j<seatnum; ++j) {
				boolean bTmp = true;
				for(int k=departure-1; k<arrival-1; ++k) {
					if(arrSeat[route-1][i][j][k])
						bTmp = false;
				}
				if(bTmp)
					throw new RuntimeException();
			}
		}
	}
	//退票成功测试处理
	void RefundSuccessDeal(Ticket ticket)
	{
		for(int k=ticket.departure-1; k<ticket.arrival-1; ++k) {
			if(!arrSeat[ticket.route-1][ticket.coach-1][ticket.seat-1][k])
				throw new RuntimeException();
			arrSeat[ticket.route-1][ticket.coach-1][ticket.seat-1][k] = false;
		}
	}
	//查询测试处理
	void InquiryDeal(int route, int departure, int arrival, int res)
	{
		int resCnt = 0;
		//循环查找
		for(int i=0; i<coachnum; ++i) {
			for(int j=0; j<seatnum; ++j) {
				boolean bTmp = true;
				for(int k=departure-1; k<arrival-1; ++k) {
					if(arrSeat[route-1][i][j][k])
						bTmp = false;
				}
				if(bTmp) {
					++ resCnt;
				}
			}
		}
		if(resCnt!=res)
			throw new RuntimeException();
	}
	
	String passengerName() {
		Random rand = new Random();
		long uid = rand.nextInt(testnum);
		return "passenger" + uid; 
	}
	//线程执行函数
	public void run()
	{
		Random rand = new Random();
    	Ticket ticket = new Ticket();
		ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();
		
		//System.out.println(ThreadId.get());
		for (int i = 0; i < testnum/threadnum; i++) {
			int sel = rand.nextInt(inqpc);
			//退票
			if (0 <= sel && sel < retpc && soldTicket.size() > 0) { // return ticket
				int select = rand.nextInt(soldTicket.size());
				if ((ticket = soldTicket.remove(select)) != null) {
    				//退票成功
					if (tds.refundTicket(ticket)) {
						if(bOut) {
    						System.out.println("TicketRefund" + " " + ticket.tid + " " + ticket.passenger + " " + ticket.route + " " + ticket.coach  + " " + ticket.departure + " " + ticket.arrival + " " + ticket.seat);
    						System.out.flush();
						}
						if(bSingleRight && threadnum==1)
							RefundSuccessDeal(ticket);
					}
					//退票失败
					else {
						if(bOut) {
    						System.out.println("ErrOfRefund");
    						System.out.flush();
						}
						throw new RuntimeException();
					}
				} else {
					if(bOut) {
    					System.out.println("ErrOfRefund");
						System.out.flush();
					}
					if(bSingleRight && threadnum==1)
						throw new RuntimeException();
				}
			}
			//买票
			else if (retpc <= sel && sel < buypc) { // buy ticket
				String passenger = passengerName();
				int route = rand.nextInt(routenum) + 1;
				int departure = rand.nextInt(stationnum - 1) + 1;
				int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
				//买票成功
				if ((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null) {
					soldTicket.add(ticket);
					if(bOut) {
    					System.out.println("TicketBought" + " " + ticket.tid + " " + ticket.passenger + " " + ticket.route + " " + ticket.coach + " " + ticket.departure + " " + ticket.arrival + " " + ticket.seat);
						System.out.flush();
					}
					if(bSingleRight && threadnum==1)
						BuySuccessDeal(ticket);
				}
				//买票失败
				else {
					if(bOut) {
    					System.out.println("TicketSoldOut" + " " + route+ " " + departure+ " " + arrival);
						System.out.flush();
					}
					if(bSingleRight && threadnum==1)
						BuyFailDeal(route, departure, arrival);
				}
			}
			//查询
			else if (buypc <= sel && sel < inqpc) { // inquiry ticket
				
				int route = rand.nextInt(routenum) + 1;
				int departure = rand.nextInt(stationnum - 1) + 1;
				int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
				int leftTicket = tds.inquiry(route, departure, arrival);
				if(bOut) {
    				System.out.println("RemainTicket" + " " + leftTicket + " " + route+ " " + departure+ " " + arrival);
					System.out.flush();
				}
				if(bSingleRight && threadnum==1)
					InquiryDeal(route, departure, arrival, leftTicket);
			}
		}
    }
}

//实现测试类
class TestAssist
{
	int threadGroup[]; // concurrent thread number
	int routenum; // route is designed from 1 to 5
	int coachnum; // coach is arranged from 1 to 8
	int seatnum; // seat is allocated from 1 to 100
	int stationnum; // station is designed from 1 to 10

	int[] testGroup;
	int retpc; // return ticket operation is 10% percent
	int buypc; // buy ticket operation is 30% percent
	int inqpc; //inquiry ticket operation is 60% percent
	
	TicketingSystem tdsGroup[];
	String name;
	
	boolean bOut;//结果输出
	boolean bSingleRight;//单线程正确性测试
	
	TestAssist(int[] threadGroup, int routenum, int coachnum, int seatnum, int stationnum, TicketingSystem[] tdsGroup,
			int[] testGroup, int retpc, int buypc, int inqpc, boolean bOut, boolean bSingleRight, String name)
	{
		this.threadGroup = threadGroup;
		this.routenum = routenum;
		this.coachnum = coachnum;
		this.seatnum = seatnum;
		this.stationnum = stationnum;
		this.tdsGroup = tdsGroup;
		this.testGroup = testGroup;
		this.retpc = retpc;
		this.buypc = buypc;
		this.inqpc = inqpc;
		this.bOut = bOut;
		this.bSingleRight = bSingleRight;
		this.name = name;
	}
	
	void TestGroup(int testIdx, int threadIdx) throws InterruptedException
	{
		int threadnum = threadGroup[threadIdx];
		ThreadRun[] func = new ThreadRun[threadnum];
		Thread[] thd = new Thread[threadnum];

		long begintime = System.nanoTime();
		
		//线程执行
		for(int i=0; i<threadnum; ++i) {
			func[i] = new ThreadRun(threadnum, routenum, coachnum, seatnum, stationnum, tdsGroup[threadIdx],
					testGroup[testIdx], retpc, buypc, inqpc, bOut, bSingleRight);
			thd[i] = new Thread(func[i], ""+i);
			thd[i].start();
		}
		for(int i=0; i<threadnum; ++i) {
			thd[i].join();
		}

		long endtime = System.nanoTime();
		System.out.println("threadnum: "+threadnum+", testnum: "+testGroup[testIdx]+", time: "+(double)(endtime-begintime)/1000000+
				"ms, volum: "+(double)testGroup[testIdx]*1000000/(endtime-begintime));
	}
	public void Test() throws InterruptedException
	{
		System.out.println(name+": ");
		System.out.println(
				"(route, coach, seat, station): ("+routenum+", "+coachnum+", "+seatnum+", "+stationnum+
				"), (retpc, buypc, inqpc): ("+retpc+", "+(buypc-retpc)+", "+(inqpc-buypc)+")");
		for(int j=0; j<testGroup.length; ++j) {
			for(int i=0; i<threadGroup.length; ++i) {
				TestGroup(j, i);
			}
		}
		System.out.println();
	}
}

public class Test {
//	final static int threadnum = 1; // concurrent thread number
//	final static int routenum = 5; // route is designed from 1 to 5
//	final static int coachnum = 8; // coach is arranged from 1 to 8
//	final static int seatnum = 100; // seat is allocated from 1 to 100
//	final static int stationnum = 10; // station is designed from 1 to 10

//	final static int testnum = 1000000;
//	final static int retpc = 10; // return ticket operation is 10% percent
//	final static int buypc = 40; // buy ticket operation is 30% percent
//	final static int inqpc = 100; //inquiry ticket operation is 60% percent
	
	final static boolean bOut = false;//结果输出
	final static boolean bSingleRight = false;//单线程正确性测试
	

	//主函数
	public static void main(String[] args) throws InterruptedException
	{
		//测试各选择
		int testGroup[] = {1000000, 100000, 40000};
		int threadGroup[] = {1, 3, 8, 64};
		int seatGroup[][] = {{5, 8, 100, 10}, {1, 8, 100, 10}};
		int operGroup[][] = {{10, 40, 100}};
		for(int control=0; control<2; ++control) {
			//循环选择
	        for(int seatSeg=0; seatSeg<seatGroup.length; ++seatSeg) {
	        	for(int operSeg=0; operSeg<operGroup.length; ++operSeg) {
	        		int allGroupNum = threadGroup.length*threadGroup.length;
	        		//测试
	        		{
		        		TicketingSystem[] tdsGroup = new TicketingSystem[allGroupNum];
		        		for(int i=0; i<allGroupNum; ++i) {
		        			tdsGroup[i] = new TicketingDSSafe(seatGroup[seatSeg][0], seatGroup[seatSeg][1],
		        					seatGroup[seatSeg][2], seatGroup[seatSeg][3], threadGroup[i%threadGroup.length]);
		        		}
		        		TestAssist test = new TestAssist(threadGroup,
		        				seatGroup[seatSeg][0], seatGroup[seatSeg][1], seatGroup[seatSeg][2], seatGroup[seatSeg][3],
		        				tdsGroup, testGroup,
		        				operGroup[operSeg][0], operGroup[operSeg][1], operGroup[operSeg][2],
		        				bOut, bSingleRight, "TicketingDSSafe");
		        		test.Test();
	        		}
	        		//测试
	        		{
		        		TicketingSystem[] tdsGroup = new TicketingSystem[allGroupNum];
		        		for(int i=0; i<allGroupNum; ++i) {
		        			tdsGroup[i] = new TicketingDSAd(seatGroup[seatSeg][0], seatGroup[seatSeg][1],
		        					seatGroup[seatSeg][2], seatGroup[seatSeg][3], threadGroup[i%threadGroup.length]);
		        		}
		        		TestAssist test = new TestAssist(threadGroup,
		        				seatGroup[seatSeg][0], seatGroup[seatSeg][1], seatGroup[seatSeg][2], seatGroup[seatSeg][3],
		        				tdsGroup, testGroup,
		        				operGroup[operSeg][0], operGroup[operSeg][1], operGroup[operSeg][2],
		        				bOut, bSingleRight, "TicketingDSAd");
		        		test.Test();
	        		}
	        		//测试
	        		{
		        		TicketingSystem[] tdsGroup = new TicketingSystem[allGroupNum];
		        		for(int i=0; i<allGroupNum; ++i) {
		        			tdsGroup[i] = new TicketingDSRadical(seatGroup[seatSeg][0], seatGroup[seatSeg][1],
		        					seatGroup[seatSeg][2], seatGroup[seatSeg][3], threadGroup[i%threadGroup.length]);
		        		}
		        		TestAssist test = new TestAssist(threadGroup,
		        				seatGroup[seatSeg][0], seatGroup[seatSeg][1], seatGroup[seatSeg][2], seatGroup[seatSeg][3],
		        				tdsGroup, testGroup,
		        				operGroup[operSeg][0], operGroup[operSeg][1], operGroup[operSeg][2],
		        				bOut, bSingleRight, "TicketingDSRadical");
		        		test.Test();
	        		}
	        		//测试
	        		{
		        		TicketingSystem[] tdsGroup = new TicketingSystem[allGroupNum];
		        		for(int i=0; i<allGroupNum; ++i) {
		        			tdsGroup[i] = new TicketingDSBack3(seatGroup[seatSeg][0], seatGroup[seatSeg][1],
		        					seatGroup[seatSeg][2], seatGroup[seatSeg][3], threadGroup[i%threadGroup.length]);
		        		}
		        		TestAssist test = new TestAssist(threadGroup,
		        				seatGroup[seatSeg][0], seatGroup[seatSeg][1], seatGroup[seatSeg][2], seatGroup[seatSeg][3],
		        				tdsGroup, testGroup,
		        				operGroup[operSeg][0], operGroup[operSeg][1], operGroup[operSeg][2],
		        				bOut, bSingleRight, "TicketingDSBack3");
		        		test.Test();
	        		}
	        	}
	        }
	        if(control!=1)
	        	System.out.println("\n\n************************************\n\n");
		}
	}
}
