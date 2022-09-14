package ticketingsystem;

import java.util.*;

public class Test {
	final static int threadnum = 1; // concurrent thread number
	final static int routenum = 5; // route is designed from 1 to 3
	final static int coachnum = 8; // coach is arranged from 1 to 5
	final static int seatnum = 100; // seat is allocated from 1 to 20
	final static int stationnum = 10; // station is designed from 1 to 5

	final static int testnum = 20000;
	final static int retpc = 10; // return ticket operation is 10% percent
	final static int buypc = 40; // buy ticket operation is 30% percent
	final static int inqpc = 100; //inquiry ticket operation is 60% percent
	
	static String passengerName() {
		Random rand = new Random();
		long uid = rand.nextInt(testnum);
		return "passenger" + uid; 
	}

	public static void main(String[] args) throws InterruptedException {	
		Thread[] threads = new Thread[threadnum];	
		final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
	    for (int i = 0; i< threadnum; i++) {
	    	threads[i] = new Thread(new Runnable() {
                public void run() {
                	long t1=System.currentTimeMillis();
            		Random rand = new Random();
                	Ticket ticket;
            		ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();
          
            		for (int i = 0; i < testnum; i++) {
            			int sel = rand.nextInt(100);
            			if (0 <= sel && sel < retpc && soldTicket.size() > 0) { // return ticket
            				int select = rand.nextInt(soldTicket.size());
           				if ((ticket = soldTicket.remove(select)) != null) {
            					if (tds.refundTicket(ticket)) {
            					//	System.out.println("TicketRefund" + " " + ticket.tid + " " + ticket.passenger 
            					//			+ " " + ticket.route + " " + ticket.coach  + " " + ticket.departure 
            					//			+ " " + ticket.arrival + " " + ticket.seat);
            					//	System.out.flush();
            					} else {
            					//	System.out.println("ErrOfRefund");
            					//	System.out.flush();
            					}
            				} else {
            					//System.out.println("ErrOfRefund");
        						//System.out.flush();
            				}
            			} else if (retpc <= sel && sel < buypc) { // buy ticket
            				String passenger = passengerName();
            				int route = rand.nextInt(routenum) + 1;
            				int departure = rand.nextInt(stationnum - 1) + 1;
            				int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
            				if ((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null) {
            					soldTicket.add(ticket);
            					//System.out.println("TicketBought" + " " + ticket.tid + " " + ticket.passenger 
            					//		+ " " + ticket.route + " " + ticket.coach + " " + ticket.departure 
            					//		+ " " + ticket.arrival + " " + ticket.seat);
        						//System.out.flush();
            				} else {
            					//System.out.println("TicketSoldOut" + " " + route+ " " + departure+ " " + arrival);
        						//System.out.flush();
            				}
            			} else if (buypc <= sel && sel < inqpc) { // inquiry ticket
            				
            				int route = rand.nextInt(routenum) + 1;
            				int departure = rand.nextInt(stationnum - 1) + 1;
            				int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
            				int leftTicket = tds.inquiry(route, departure, arrival);
            				//System.out.println("RemainTicket" + " " + leftTicket + " " + route+ " " + departure+ " " + arrival);
    						//System.out.flush();      						         			
            			}
            		}
            		long t2=System.currentTimeMillis();
            	    System.out.println("time :" + (t2 - t1) / 1000.0);
                }
            });
            threads[i].start();
 	    }
	}
}



//public Ticket() {}
//
//public Ticket(Object obj) {
//	if(obj instanceof Ticket) {
//		Ticket ticket = (Ticket)obj;
//		this.tid = ticket.tid;
//		this.passenger = ticket.passenger;
//		this.route = ticket.route;
//		this.coach = ticket.coach;
//		this.seat = ticket.seat;
//		this.departure = ticket.departure;
//		this.arrival = ticket.arrival;
//	}
//}
//
//public Ticket(long tid, String passenger, int route, int coach,
//		int seat, int departure, int arrival) {
//	this.tid = tid;
//	this.passenger = passenger;
//	this.route = route;
//	this.coach = coach;
//	this.seat = seat;
//	this.departure = departure;
//	this.arrival = arrival;
//}
//
//@Override
//public String toString() {
//	return "Ticket [tid=" + tid + ", passenger=" + passenger + ", route=" + route + ", coach=" + coach + ", seat="
//			+ seat + ", departure=" + departure + ", arrival=" + arrival + "]";
//}
//
//@Override
//public int hashCode() {
//	final int prime = 31;
//	int result = 1;
//	result = prime * result + ((passenger == null) ? 0 : passenger.hashCode());
//	result = prime * result + (int) (tid ^ (tid >>> 32));
//	return result;
//}
//
//@Override
//public boolean equals(Object obj) {
//	if (this == obj)
//		return true;
//	if (obj == null)
//		return false;
//	if (getClass() != obj.getClass())
//		return false;
//	Ticket other = (Ticket) obj;
//	if (passenger == null) {
//		if (other.passenger != null)
//			return false;
//	} else if (!passenger.equals(other.passenger))
//		return false;
//	if (tid != other.tid)
//		return false;
//	return true;
//}
