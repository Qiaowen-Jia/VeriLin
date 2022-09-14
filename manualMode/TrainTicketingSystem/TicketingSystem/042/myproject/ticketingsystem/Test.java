package ticketingsystem;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class Test {
	final static int[] threadnums = {1, 2, 4, 8, 16, 32, 64, 128};
	//final static int[] threadnums = {4};
	
	final static int routenum = 5; // route is designed from 1 to 3
	final static int coachnum = 8; // coach is arranged from 1 to 5
	final static int seatnum = 100; // seat is allocated from 1 to 20
	final static int stationnum = 10; // station is designed from 1 to 5

	final static int testnum = 10000;
	
	final static int retpc = 10; // return ticket operation is 10% percent
	final static int buypc = 30; // buy ticket operation is 30% percent
	final static int inqpc = 60; //inquiry ticket operation is 60% percent
	
	private static TicketBase soldticketbase;
	
	static String passengerName() {
		Random rand = new Random();
		long uid = rand.nextInt(testnum);
		return "passenger" + uid; 
	}

	public static void main(String[] args) throws InterruptedException {
		soldticketbase = new TicketBase(); 
		
		System.out.println("Threads Times(ms) Throughout(kop/s) AVGbuy(ns) AVGInquire(ns) AVGRefund(ns)");
		
		for(int i=1;i<=1;i++)
		{
			for(int threadnum : threadnums){
				work(threadnum);
			}
		}
	}
		 
	public static void work(int threadnum)throws InterruptedException
	{
		Thread[] threads = new Thread[threadnum];

		final AtomicLong totalRetNum = new AtomicLong(0);
		final AtomicLong totalBuyNum = new AtomicLong(0);
		final AtomicLong totalInqueryNum = new AtomicLong(0);

		final AtomicLong totalRetTime = new AtomicLong(0);
		final AtomicLong totalBuyTime = new AtomicLong(0);
		final AtomicLong totalInqueryTime = new AtomicLong(0);
	
		final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);

		long aTime = System.currentTimeMillis();
		
	    for (int i = 0; i< threadnum; i++) {
	    	threads[i] = new Thread(new Runnable() {
                public void run() {
            		Random rand = new Random();
                	Ticket ticket = new Ticket();
            		//ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();

					long retNum = 0, buyNum = 0, queryNum = 0;
					long retTime = 0, buyTime = 0, queryTime = 0;
					long begin;
            		//System.out.println(ThreadId.get());
            		for (int i = 0; i < testnum; i++) {
            			int sel = rand.nextInt(inqpc);
            			if (0 <= sel && sel < retpc && soldticketbase.size() > 0) 
            			{ 	
            				// return ticket
            				long randseed= rand.nextInt(soldticketbase.size());
       						ticket = soldticketbase.FindTicket(randseed);
       						//System.out.println("TicketRefund " + randseed + " " + soldticketbase.size() + " " + ticket);
        					if (ticket!=null) {
        						//------------------------
        						begin = System.nanoTime();
        						//------------------------
        						tds.refundTicket(ticket);
        						//------------------------
								retTime += System.nanoTime() - begin;
								++retNum;
        						//------------------------
        						soldticketbase.RefundTicket(ticket);
        						//System.out.println("TicketRefund" + " " + ticket.tid + " " + ticket.passenger + " " + ticket.route + " " + ticket.coach  + " " + ticket.departure + " " + ticket.arrival + " " + ticket.seat);
        						//System.out.flush();
        					} 
            			} else if (retpc <= sel && sel < buypc) { // buy ticket
            				String passenger = passengerName();
            				int route = rand.nextInt(routenum) + 1;
            				int departure = rand.nextInt(stationnum - 1) + 1;
            				int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
    						//------------------------
            				begin = System.nanoTime();
    						//------------------------
            				ticket = tds.buyTicket(passenger, route, departure, arrival);
    						//------------------------
							buyTime += System.nanoTime() - begin;
							++buyNum;
    						//------------------------
							
            				if (ticket!= null) {
            					soldticketbase.AddTicket(ticket);
            					//System.out.println("TicketBought" + " " + ticket.tid + " "+ ticket.route + " " + ticket.coach + " " + ticket.departure + " " + ticket.arrival + " " + ticket.seat + " " + ticket.passenger + " " );
            					//System.out.flush();
            				}
            			} else if (buypc <= sel && sel < inqpc) { // inquiry ticket
            				
            				int route = rand.nextInt(routenum) + 1;
            				int departure = rand.nextInt(stationnum - 1) + 1;
            				int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
            				

    						//------------------------
            				begin = System.nanoTime();
    						//------------------------
            				tds.inquiry(route, departure, arrival);
    						//------------------------
							queryTime += System.nanoTime() - begin;
							++queryNum;
    						//------------------------
    						         			
            			}
            		}


					totalRetNum.addAndGet(retNum);
					totalBuyNum.addAndGet(buyNum);
					totalInqueryNum.addAndGet(queryNum);

					totalRetTime.addAndGet(retTime);
					totalBuyTime.addAndGet(buyTime);
					totalInqueryTime.addAndGet(queryTime);
                }
            });
              threads[i].start();
 	    }
	
	    for (int i = 0; i< threadnum; i++) {
	    	threads[i].join();
	    }		
	
		aTime = System.currentTimeMillis()-aTime;

		//System.out.println("Thread Time Throughout AVGbuy AVGInquire AVGRefund");
		System.out.println(threadnum  
				+ " " + aTime 
				+ " " + testnum*threadnum/aTime
				+ " " + totalBuyTime.get()/ totalBuyNum.get() 
				+ " " + totalInqueryTime.get()/ totalInqueryNum.get() 
				+ " " + totalRetTime.get()/ totalRetNum.get());
	}	
}
