package ticketingsystem;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Test {
	final static int threadnum = 1;
	final static int routenum = 20; // route is designed from 1 to 3
	final static int coachnum = 10; // coach is arranged from 1 to 5
	final static int seatnum = 100; // seat is allocated from 1 to 20
	final static int stationnum = 16; // station is designed from 1 to 5

	final static int testnum = 5; 
	final static int retpc = 10; // return ticket operation is 10% percent
	final static int buypc = 30; // buy ticket operation is 30% percent
	final static int inqpc = 100; //inquiry ticket operation is 60% percent
	
	

	static String passengerName() {
		Random rand = new Random();
		long uid = rand.nextInt(testnum);
		return "passenger" + uid; 
	}

	
	public static void main(String[] args) throws InterruptedException {
		int routenum = 5;
    	int coachnum = 8;
    	int seatnum = 100;
    	int stationnum = 10;
    	int threadnum = 16;
    	/*
    	 * 
    	 * 每个线程是一个票务代 理，按照60%查询余票，30%购票和10% 逥票的比率反复调用TicketingDS类 的三种方法若干次（缺省为总共10000次）。
    	 * 按照线程数为4，8，16，32，64个的情况分别给出每种方法调用的平均执行时间，同时计算系统的总吞 吐率（单位时间内完成的方法调用总数）。
    	 */
		final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
				 
	    Thread[] threads = new Thread[threadnum];
				
		Ticket ticket = new Ticket();
		final long startTime = System.nanoTime();
		long preTime = System.nanoTime() - startTime;
		long postTime = System.nanoTime() - startTime;
		String passenger = passengerName();
		Random rand = new Random();
		int route = rand.nextInt(routenum) + 1;
		int departure = rand.nextInt(stationnum - 1) + 1;
		int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
        //long preTime = System.nanoTime() - startTime;
		if ((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null) {
					//	long postTime = System.nanoTime() - startTime;
			System.out.println(preTime + " " + postTime + " " + ThreadId.get() + " " + "TicketBought" + " " + ticket.tid + " " + ticket.passenger + " " + ticket.route + " " + ticket.coach + " " + ticket.departure + " " + ticket.arrival + " " + ticket.seat);
			//soldTicket.add(ticket);
			System.out.flush();
		} else {
			System.out.println(preTime + " " + String.valueOf(System.nanoTime()-startTime) + " " + ThreadId.get() + " " + "TicketSoldOut" + " " + route + " " + departure+ " " + arrival);
			System.out.flush();
		}
		Ticket ticket2 = new Ticket();
	    ticket2.arrival = ticket.arrival;
	    ticket2.tid = ticket.tid;//车票编号
	    ticket2.passenger = ticket.passenger;//乘客名字
	    ticket2.route = ticket.route;//列车车次
	    ticket2.coach = ticket.coach;//车厢号
	    ticket2.seat = ticket.seat;//座位号
	    ticket2.departure = ticket.departure;//出发站编号
	    ticket2.arrival = ticket.arrival+1 ;//到达站编号
		//ticket2.arrival = 30000;
       if (tds.refundTicket(ticket2)) {											//long postTime = System.nanoTime() - startTime;
		    	System.out.println(preTime + " " + postTime + " " + ThreadId.get() + " " + "TicketRefund" + " " + ticket.tid + " " + ticket.passenger + " " + ticket.route + " " + ticket.coach  + " " + ticket.departure + " " + ticket.arrival + " " + ticket.seat);
		     	System.out.flush();
				} else {
	           			System.out.println(preTime + " " + String.valueOf(System.nanoTime()-startTime) + " " + ThreadId.get() + " " + "ErrOfRefund");
	         			System.out.flush();
				}
		         	
		     
			    final long endTime = System.nanoTime();
			    System.out.println((threadnum * testnum * 1e9)/(endTime - startTime));
				ticket.arrival = 20000;
			    if (tds.refundTicket(ticket)) {											//long postTime = System.nanoTime() - startTime;
			    	System.out.println(preTime + " " + postTime + " " + ThreadId.get() + " " + "TicketRefund" + " " + ticket.tid + " " + ticket.passenger + " " + ticket.route + " " + ticket.coach  + " " + ticket.departure + " " + ticket.arrival + " " + ticket.seat);
			     	System.out.flush();
					} else {
		           			System.out.println(preTime + " " + String.valueOf(System.nanoTime()-startTime) + " " + ThreadId.get() + " " + "ErrOfRefund");
		         			System.out.flush();
					}
			         	
			     
				  //  final long endTime = System.nanoTime();
				    System.out.println((threadnum * testnum * 1e9)/(endTime - startTime));
					
		

		//ToDo
	    
	}
}
