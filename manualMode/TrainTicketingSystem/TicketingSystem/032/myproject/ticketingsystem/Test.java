package ticketingsystem;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/*
    车次总数（20个），
    列车的车厢数目（10个），
    每节车厢的座位数（100个），
    每个车次经停站的数量（16个），
    并发购票的线程数（64个），
    每个线程调用方法数（100000），
    查询，购票，退票比例为7：2：1。
 */

public class Test {
	final static int routenum = 20;
	final static int coachnum = 10;
	final static int seatnum = 100;
	final static int stationnum = 16;
	final static int threadnum = 128;
	
	final static int testnum = 100000;
	final static int retpc = 10; // return ticket operation is 10% percent
	final static int buypc = 30; // buy ticket operation is 20% percent
	final static int inqpc = 100; //inquiry ticket operation is 70% percent
	
	private final static long[] buyTime = new long[threadnum];
	private final static long[] refundTime = new long[threadnum];
	private final static long[] inquiryTime = new long[threadnum];

	private final static long[] buyNum = new long[threadnum];
	private final static long[] refundNum = new long[threadnum];
	private final static long[] inquiryNum = new long[threadnum];
	private final static AtomicInteger threadId = new AtomicInteger(0);
	private final static int[] thread_set = {4, 8, 16, 32, 64};
	static String passengerName() {
		Random rand = new Random();
		long uid = rand.nextInt(testnum);
		return "passenger" + uid; 
	}
	public static long sumToatl(long[] listNum, int threadnum) {
		long ret = 0;
		for(int i=0;i<threadnum;i++) {
			ret += listNum[i];
			listNum[i] = 0;
		}
		return ret;
	}
	public static void main(String[] args) throws InterruptedException {
		for(int j=0;j<5;j++) {
			int thread_num = thread_set[j];	
			Thread[] threads = new Thread[thread_num];
			final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, thread_num);
			//ToDo
			for (int i = 0; i< thread_num; i++) {
		    	threads[i] = new Thread(new Runnable() {
	                public void run() {
	            		Random rand = new Random();
	                	Ticket ticket = new Ticket();
	                	int id  = threadId.getAndIncrement();
	            		ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();	
	             		for (int i = 0; i < testnum; i++) {
	            			int sel = rand.nextInt(inqpc);
	            			if (0 <= sel && sel < retpc && soldTicket.size() > 0) { // return ticket
	            				int select = rand.nextInt(soldTicket.size());
	            				if ((ticket = soldTicket.remove(select)) != null) {	
									long s = System.nanoTime();
									tds.refundTicket(ticket);
									long e = System.nanoTime();		
									refundTime[id] += e-s;
									refundNum[id] +=1;
	            				} else {
	            					System.out.println("ErrOfRefund");
	        						System.out.flush();
	            				}
	            			} else if (retpc <= sel && sel < buypc) { // buy ticket
	            				String passenger = passengerName();
	            				int route = rand.nextInt(routenum) + 1;
	            				int departure = rand.nextInt(stationnum - 1) + 1;
	            				int arrival = departure + rand.nextInt(stationnum - departure) + 1;
	            				long s = System.nanoTime();
	            				if ((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null) {
	            					long e = System.nanoTime();
	            					soldTicket.add(ticket);
	            					buyTime[id] += e-s;
									buyNum[id] +=1;	
	            				} 
//	            				else {	
//	            					System.out.println(tds.inquiry(route, departure, arrival) + "TicketSoldOut" + " " + route + " " + departure+ " " + arrival);
//	        						System.out.flush();
//	            				}
	            			} else if (buypc <= sel && sel < inqpc) { // inquiry ticket
	            				int route = rand.nextInt(routenum) + 1;
	            				int departure = rand.nextInt(stationnum - 1) + 1;
	            				int arrival = departure + rand.nextInt(stationnum - departure) + 1;
	            				long s = System.nanoTime();
	            				int leftTicket = tds.inquiry(route, departure, arrival);
	            				long e = System.nanoTime();
	            				inquiryTime[id] += e-s;
								inquiryNum[id] +=1;      			
	            			}
	            		}
	
	                }
	            });
			}
			
			long start = System.currentTimeMillis();
			for (int i = 0; i < thread_num; ++i)
				threads[i].start();
			for (int i = 0; i < thread_num; i++) {
				threads[i].join();
			}
			long end = System.currentTimeMillis();
			long total_time = end-start;
			long toatl_buy_time = sumToatl(buyTime, thread_num);
			long toatl_inq_time = sumToatl(inquiryTime, thread_num);
			long toatl_refun_time = sumToatl(refundTime, thread_num);
			long toatl_buy_num = sumToatl(buyNum, thread_num);
			long toatl_inq_num = sumToatl(inquiryNum, thread_num);
			long toatl_refun_num = sumToatl(refundNum, thread_num);
			long burperns = (long)(toatl_buy_time/toatl_buy_num);
			long inqperns = (long)(toatl_inq_time/toatl_inq_num);
			long refunperns = (long)(toatl_refun_time/toatl_refun_num);
			long sum_method = (long)(thread_num * testnum);
			long tun = (long)(sum_method / (double) total_time) * 1000; 
			threadId.set(0);
			System.out.print("threadnum:"+thread_num+"\tbuyaverage:"+burperns+"(ns)\t inqueryaverage:"+inqperns+"(ns)\trefunaverage:"+refunperns+"(ns)\tthroughout:"+tun);
			System.out.print("\n");
		}
		//ToDo——end()
	}
}

