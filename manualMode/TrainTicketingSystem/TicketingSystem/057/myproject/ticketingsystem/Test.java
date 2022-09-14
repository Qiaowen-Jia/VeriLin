package ticketingsystem;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Test {

	private static ReentrantLock total_ticket_sold_lock = new ReentrantLock();     //可重入锁；
	public static int calcul_ticket_occupancy(ArrayList<Ticket> soldTicket) {
//		int rec = 0;
		int counter = 0;
		Ticket ticket_temp;
		ArrayList<Ticket> soldTicket_temp = soldTicket;
		while(soldTicket_temp.size()>0) {
			ticket_temp = soldTicket_temp.remove(0);
			counter = counter + (ticket_temp.arrival - ticket_temp.departure);
		}
		return counter;
	}

	public static void main(String[] args) throws InterruptedException {
		long startTime = System.currentTimeMillis(); //获取开始时间
		int threadnum = 96; 
		int routenum = 5; 
	    int coachnum = 8; 
		int seatnum = 100; 
		int stationnum = 10; 

		int testnum = 50000;
		int refund_per = 10; 
		int buy_per = 30; 
		int inquiry_per = 100 - buy_per - refund_per; 
		
		final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
		Thread[] threads = new Thread[threadnum];
		final int[] total_ticket_sold = new int[1];
		
	    for (int i = 0; i< threadnum; i++) {
	    	threads[i] = new Thread(new Runnable() {
                public void run() {
            		Random rand = new Random();
                	Ticket ticket = new Ticket();
            		ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();
            		for (int i = 0; i < testnum; i++) {
            			int sel = rand.nextInt(refund_per + buy_per + inquiry_per);
            			if(sel < refund_per) {
            				if(soldTicket.size() > 0) {
                				int re_sel = rand.nextInt(soldTicket.size());
                				ticket = soldTicket.remove(re_sel);
                				if(tds.refundTicket(ticket)) {
//                					System.out.println("refund ticket " + ticket.tid + "  " + ticket.passenger + "  " + ticket.route + "  " + ticket.departure + "  " + ticket.arrival + " successful");
                				}else {
  //              					System.out.println("refund ticket " + ticket.tid + "  " + ticket.passenger + "  " + ticket.route + "  " + ticket.departure + "  " + ticket.arrival + " failed");
                				}
                					
            				}else {
            					//System.out.println("there is no ticket to refund");
            				}
            				

            			}else if(sel < (refund_per + buy_per) ) {
            				String passengername = "passenger" + rand.nextInt(10000);
            				int train = rand.nextInt(routenum) + 1;
            				int departure = rand.nextInt(stationnum - 1) + 1;
            				int arrival = rand.nextInt(stationnum - departure) + departure + 1;
            				ticket = tds.buyTicket(passengername, train, departure, arrival);
//            				ticket = tds.buyTicket(passengername, train, 1, 10);
            				if(null != ticket) {
            				//	System.out.println("buy ticket " + ticket.tid + "  " + ticket.passenger + "  " + ticket.route + "  " +ticket.coach + "  " + ticket.seat + "  " + ticket.departure + "  " + ticket.arrival + " successful");
            					soldTicket.add(ticket);
            				}else {
            				//	System.out.println( passengername + " buy ticket " +  " failed ");
            				}
            			}else {
            				int train = rand.nextInt(routenum) + 1;
            				int departure = rand.nextInt(stationnum - 1) + 1;
            				int arrival = rand.nextInt(stationnum - departure) + departure + 1;
            				int rec = tds.inquiry(train, departure, arrival);
            				if(rec > 0) {
            				//	System.out.println(" train " + train + " from " + departure + " to " + arrival + " have ticket");
            				}else if(0 == rec) {
            				//	System.out.println(" train " + train + " from " + departure + " to " + arrival + " not have ticket");
            				}else {
            					System.out.println("inquiry ticket error");
            				}
            			}
            			
            		}
            		
            		int total_sold = calcul_ticket_occupancy(soldTicket);
            		total_ticket_sold_lock.lock();
            		total_ticket_sold[0] = total_sold + total_ticket_sold[0];
            		total_ticket_sold_lock.unlock();
                }
	    	});
	    	threads[i].start();
	    }

	    for (int i = 0; i< threadnum; i++) {
	    	threads[i].join();
	    }		
	    long endTime = System.currentTimeMillis(); //获取结束时间

	    //正确性验证；
        int unsold_seat = 0;
        Train[] train = tds.getTrain();
        Coach[] coach;
        Seat[] seat;
        long[] seat_status;
        for(int i = 0;i < tds.getRoutenum(); i++) {
        	coach = train[i].getCoach();
        	for(int j = 0; j < train[i].getCoachnum();j++) {
        		seat = coach[j].getLocal_seat();
        		for(int k = 0;k < coach[j].getSeat_num();k++) {
        			seat_status = seat[k].getTicket_id();
        			for(int n = 0; n < seat[k].getTotal_sta()-1;n++) {
        				if(seat_status[n] == -1) {
        					unsold_seat++;
        				}
        				
        			}
        		}
        	}
        }
	    System.out.println("ticket occupancy: " + total_ticket_sold[0]);
	    System.out.println("train remain : " + unsold_seat);
	    System.out.println("train remain + ticket =  " + (unsold_seat + total_ticket_sold[0]));
	    System.out.println("ticket total " + (routenum * coachnum * seatnum * (stationnum-1)));
	    if(total_ticket_sold[0] > 0) {
		    System.out.println("occupancy rate " + ((double)total_ticket_sold[0]) / (routenum * coachnum * seatnum * (stationnum-1)));
	    }
	    System.out.println("程序运行时间：" + (endTime - startTime) + "ms"); //输出程序运行时间
	    System.out.println("方法平均调用时间：" + (endTime - startTime) / ((double)(testnum * threadnum)) + "毫秒/次"); //方法平均调用时间
	    System.out.println("程序吞吐量：" + ((double)(testnum * threadnum)) / (endTime - startTime) + "次/毫秒"); //程序吞吐量
	}
	

}

