package ticketingsystem;

import java.lang.Thread;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class TESTCONFIG
{
	static int THREADNUM = 1;
	static int THREADOP = 400000;
	static int ROUTENUM = 5;
	static int COACHNUM = 8;
	static int SEATNUM = 100;
	static int STATIONNUM = 10;
}
public class Test {
	
	public static void main(String[] args) throws InterruptedException {
		Agent agent = new Agent();
		long startTime;
		Thread agents[] = new Thread[TESTCONFIG.THREADNUM];
		for (int i = 0; i <TESTCONFIG.THREADNUM ; i++) {
			agents[i] = new Thread(agent);
			agents[i].setName("agent" + i);
		}
		startTime = System.nanoTime();
		for (Thread thread : agents)
		{
			thread.start();
		}
	    for (Thread thread : agents) {
	    	thread.join();
	    }
	    long endTime = System.nanoTime();
	    System.out.println("thread numbers:" + TESTCONFIG.THREADNUM
	    +"\ttotal run:"+ TESTCONFIG.THREADNUM * TESTCONFIG.THREADOP
	    +"\tduring time:" + (endTime - startTime));
		/*Thread agentThread1 = new Thread(agent);
		Thread agentThread2 = new Thread(agent);
		Thread agentThread3 = new Thread(agent);
		agentThread1.setName("agent1");
		agentThread2.setName("agent2");
		agentThread3.setName("agent3");
		agentThread1.start();
		agentThread2.start();
		agentThread3.start();*/
		//final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
	}
}

class Agent extends Thread
{
	TicketingDS ticketSystem = new TicketingDS();
	Map<Long,Ticket> tickets = new ConcurrentHashMap<Long,Ticket>();
	public void run()
	{
		for(int i = 0 ; i < TESTCONFIG.THREADOP ; i++)
		{
			double behavior = java.lang.Math.random();
			//for test
			behavior = 0.5;
			if((behavior * 100) < 60)
			{
				//查票
				/*
				System.out.println("\n " + Thread.currentThread().getName()
				+":route"
				+ (i % CONSTANT.ROUTENUM + 1)//route
				+"\t"
				+(i % CONSTANT.STATIONNUM + 1)//departure
				+" to "
				+((i + (int)(behavior * 10)) % CONSTANT.STATIONNUM + 1)//arrival
				+" remain " 
				+ ticketSystem.inquiry(i % CONSTANT.ROUTENUM + 1,//route
						i % CONSTANT.STATIONNUM + 1,//departure
						(i + (int)(behavior * 10)) % CONSTANT.STATIONNUM + 1)//arrival
				+ " tickets");*/
				continue;
			}
			if((behavior * 100) < 90)
			{
				//购票
				Ticket ticket = ticketSystem.buyTicket(Thread.currentThread().getName(),
						i % TESTCONFIG.ROUTENUM + 1, (i % TESTCONFIG.STATIONNUM + 1),
						(i + (int)(behavior * 10)) % TESTCONFIG.STATIONNUM + 1);
				/*if(ticket == null)
				{
					System.out.println("ticket selled out\n");
				}
				else 
				{
					
					tickets.put(ticket.tid, ticket);
					System.out.println("\nagent = " + ticket.passenger
					+ "\ntid = " + ticket.tid
					+ "\nroute = " + ticket.route 
					+ "\ncoach = " + ticket.coach
					+ "\nseat = " + ticket.seat
					+ "\nstation = " + ticket.departure + "to" + ticket.arrival + "\n");					
				}*/			
				continue;
			}
			//退票
			ticketSystem.refundTicket(tickets.get((long)(behavior * tickets.size())));
			/*
			if(ticketSystem.refundTicket(tickets.get((long)(behavior * tickets.size()))))
			{
				System.out.println("\n" + Thread.currentThread().getName() + " :refund successful tid = " + tickets.get((long)(behavior * tickets.size())).tid);
			}
			else
			{
				System.out.println("\n " + Thread.currentThread().getName() + " :refund failed, invaild tid = " + tickets.get((long)(behavior * tickets.size())));
			}*/
		}
	}
}