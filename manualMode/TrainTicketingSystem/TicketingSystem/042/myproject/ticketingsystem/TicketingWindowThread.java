package ticketingsystem;

import java.util.ArrayList;
import java.util.Random;

import java.util.concurrent.atomic.AtomicInteger;

public class TicketingWindowThread implements Runnable {

	private long ntotalBuyTime;
	private long ntotalRefundTime;
	private long ntotalInquiryTime;
	
	private TicketingDS tds;

	private boolean outputFlag = false;

	private AtomicInteger totalNum; 
	
	private ThreadLocal<Integer> threadLocalInt; // Test Number

	public long getNtotalBuyTime() {
		return ntotalBuyTime;
	}

	public void setNtotalBuyTime(long ntotalBuyTime) {
		this.ntotalBuyTime = ntotalBuyTime;
	}

	public long getNtotalRefundTime() {
		return ntotalRefundTime;
	}

	public void setNtotalRefundTime(long ntotalRefundTime) {
		this.ntotalRefundTime = ntotalRefundTime;
	}

	public long getNtotalInquiryTime() {
		return ntotalInquiryTime;
	}

	public void setNtotalInquiryTime(long ntotalInquiryTime) {
		this.ntotalInquiryTime = ntotalInquiryTime;
	}

	public void setThreadLocal(ThreadLocal<Integer> threadLocalInt) {
		this.threadLocalInt = threadLocalInt;
	}

	public void setTotalNum(AtomicInteger totalNum) {
		this.totalNum = totalNum;
	}

	public AtomicInteger getTotalNum() {
		return totalNum;
	}

	public TicketingWindowThread(TicketingDS tds) {
		this.tds = tds;
	}

	public void setOutputFlag(boolean flag) {
		outputFlag = flag;

	}
	
	public String getRandomString(int length){
	     String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	     Random random=new Random();
	     StringBuffer sb=new StringBuffer();
	     int i;
	     for(i=0;i<length;i++){
	       sb.append(str.charAt(random.nextInt(62)));
	     }
	     return sb.toString();
	 }
	
	private Ticket GetTicketInfoFromString(String str) {
		try
		{
			Ticket t = new Ticket();
			String[] ticketinfo = str.split(",");
			t.tid=Long.parseLong(ticketinfo[0]);
			t.route=Integer.parseInt(ticketinfo[1]);
			t.coach=Integer.parseInt(ticketinfo[2]);
			t.seat=Integer.parseInt(ticketinfo[3]);
			t.departure=Integer.parseInt(ticketinfo[4]);
			t.arrival=Integer.parseInt(ticketinfo[5]);
			t.passenger=ticketinfo[6];
			return t;
		}
		catch (Exception e) {
			System.out.println("Thread - GetTicketInfoFromString Error:" + e.toString() + threadLocalInt.get()+ "(TicketInfo:" + str + ")");
			return null;
		}
		
	}

	public void run() {
		Random randomPassage = new Random();
		Random randomTid = new Random();
		Random randomRoute = new Random();
		Random randomDeparture = new Random();
		Random randomArrival = new Random();
		Random randomCoach = new Random();

		int ticketNum=tds.getLastIDNum();
		int routeNum = tds.getRouteNum();
		int seatNum = tds.getSeatNum();
		int stationNum = tds.getStaionNum();
		int coachNum = tds.getCoachNum();

		long currentThreadid = Thread.currentThread().getId(); 
		long threadLocalNum; 
		//System.out.println(currentThreadid);
		
		int Inquire_Test  = 6; //60%
		int Buyticket_Test= 3; //30%
		int Refund_Test   = 1; //10%
		
		ArrayList<Ticket> boughtTicket = new ArrayList<Ticket>();
		
		do {
			if (totalNum.intValue() > 1) {
				threadLocalInt.set(totalNum.getAndDecrement());
			} else {
				return;
			}
			threadLocalNum = threadLocalInt.get();
			// System.out.println(threadLocalNum % 10);
			//60% Inquire
			if (threadLocalNum % 10 < Inquire_Test) { //60% Inquire ticket
				//System.out.println("(" + threadLocalInt.get() +")Thread:" + Thread.currentThread().getId() + " Inquire");
				long ninquiryStartTime = System.nanoTime();
				int ticketNumbyInquiry = 0 ;
				int Route = randomRoute.nextInt(routeNum)+1;
				int Departure = randomRoute.nextInt(stationNum-1)+1; 
				int Arrival = Departure + 1 + randomRoute.nextInt(stationNum-Departure);
				
				ticketNumbyInquiry = tds.inquiry(Route,Departure, Arrival);				

				//ntotalInquiryTime += System.nanoTime() - ninquiryStartTime;
				if (outputFlag)
				{
					System.out.println(threadLocalNum + " - " + currentThreadid + " Inquiry   - "
							+"Route:" + Route
							+",Departure:"+ Departure
							+",Arrival:"+ Arrival
							+ " - Ticket Number:"+ ticketNumbyInquiry);
				}

			} else if (threadLocalNum % 10 < Inquire_Test + Buyticket_Test) {  //30% buy ticket
				//System.out.println("(" + threadLocalInt.get() +")Thread:" + Thread.currentThread().getId() + " Start BuyTicket");
				String name ;
				long nbuyStartTime = System.nanoTime();
				

				int Route = randomRoute.nextInt(routeNum)+1;
				int Departure = randomRoute.nextInt(stationNum-1)+1; 
				int Arrival = Departure + 1 + randomRoute.nextInt(stationNum-Departure);
				
				name = "Passenger" + threadLocalNum;
				// if departure > arrival return null
				Ticket t = tds.buyTicket(name,Route,Departure,Arrival);
				boughtTicket.add(t);
				//ntotalBuyTime += System.nanoTime() - nbuyStartTime;
				if (outputFlag) // whether to print buy ticket information
				{
					if (t!= null) {
						System.out.println(threadLocalNum + " - " + currentThreadid + " Buy Ticket - "
								+"Route:" + t.route
								+",Departure:"+t.departure
								+",Arrival:"+t.arrival
								+" - TicketID:" + t.tid
								+",Coach:"+t.coach
								+",Seat:"+t.seat
								+",Passenger:"+t.passenger);

					}else
					{
						System.out.println(threadLocalNum + " - " + currentThreadid + " BuyTicket - "
								
								+"Route:" + Route
								+",Departure:"+Departure
								+",Arrival:"+Arrival
								+ " - No Ticket" );
					}
				}
			}
			else // 10%Refund
			{
				//System.out.println("(" + threadLocalInt.get() +")Thread:" + Thread.currentThread().getId() + " Refund Start");
				//long nrefundStartTime = System.nanoTime();
				if(boughtTicket.size() > 0)
					{
					int ticknum = randomTid.nextInt(boughtTicket.size());
					Ticket ticket = boughtTicket.remove(ticknum); //random Refund
					//System.out.println(s);
					if(ticket!=null)
					{
						if ((ticket.passenger.indexOf("REFUND")>0)&&outputFlag) {
							System.out.println(threadLocalNum + " - " + currentThreadid + " Refund[XX] - "
								+"Route:" + ticket.route
								+",Departure:" + ticket.departure
								+",Arrival:" + ticket.arrival
								
								+" - TicketID:" + ticket.tid
								+",Coach:" + ticket.coach
								+",Seat:" + ticket.seat
								+",Passenger:" + ticket.passenger);
						}
						else {
							boolean flag = tds.refundTicket(ticket);
							//ntotalRefundTime += System.nanoTime() - nrefundStartTime;
			
							if (outputFlag) {
								System.out.println(threadLocalNum + " - "  + currentThreadid + " Refund[OK] - "
										+"Route:" + ticket.route
										+",Departure:" + ticket.departure
										+",Arrival:" + ticket.arrival
										
										+" - TicketID:" + ticket.tid
										+",Coach:" + ticket.coach
										+",Seat:" + ticket.seat
										+",Passenger:" + ticket.passenger);
							}
						}
					}
					else
					{
						if (outputFlag) {
							System.out.println(threadLocalNum + " - "  + currentThreadid + " Refund - Failure - TicketID:" + ticknum);
						}
					}
				}
			}
		} while (threadLocalNum > 0);
	}
}