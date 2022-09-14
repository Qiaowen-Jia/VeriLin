package ticketingsystem;

import java.util.ArrayList;
import java.util.Random;

class TestBench
{
	final static int routenum = 20; // route is designed from 1 to 3
	final static int coachnum = 10; // coach is arranged from 1 to 5
	final static int seatnum = 100; // seat is allocated from 1 to 20
	final static int stationnum = 16; // station is designed from 1 to 5

	final static int testnum = 100000;
	final static int retpc = 10; // return ticket operation is 10% percent
	final static int buypc = 30; // buy ticket operation is 20% percent
	final static int inqpc = 100; //inquiry ticket operation is 70% percent
	private int threadnum;

	static String passengerName()
	{
		Random rand = new Random();
		long uid = rand.nextInt(testnum);
		return "passenger" + uid;
	}
	public TestBench(int tn)
	{
		threadnum = tn;
	}
	public void test() throws InterruptedException
	{
		Thread[] threads = new Thread[threadnum];

		final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);

		final long startTime = System.nanoTime();
		for (int i = 0; i < threadnum; i++)
		{
			threads[i] = new Thread(new Runnable()
			{
				public void run()
				{
					Random rand = new Random();
					Ticket ticket = new Ticket();
					ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();

					for (int i = 0; i < testnum; i++)
					{
						int sel = rand.nextInt(inqpc);
						if (0 <= sel && sel < retpc && soldTicket.size() > 0)
						{ // return ticket
							int select = rand.nextInt(soldTicket.size());
							if ((ticket = soldTicket.remove(select)) != null)
							{
								tds.refundTicket(ticket);
							}
						} else if (retpc <= sel && sel < buypc)
						{ // buy ticket
							String passenger = passengerName();
							int route = rand.nextInt(routenum) + 1;
							int departure = rand.nextInt(stationnum - 1) + 1;
							int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
							if ((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null)
							{
								soldTicket.add(ticket);
							}
						} else if (buypc <= sel && sel < inqpc)
						{ // inquiry ticket

							int route = rand.nextInt(routenum) + 1;
							int departure = rand.nextInt(stationnum - 1) + 1;
							int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
							int leftTicket = tds.inquiry(route, departure, arrival);
						}
					}

				}
			});
		}

		for (int i = 0; i < threadnum; i++)
		{
			threads[i].start();
		}
		for (int i = 0; i < threadnum; i++)
		{
			threads[i].join();
		}
		long timeconsumed = System.nanoTime() - startTime;
		long opsnum = testnum * threadnum;
		double opsps = opsnum / (timeconsumed / 1e9);
		System.out.printf("Number of threads: %d, %f operations per socend!\n", threadnum, opsps);
	}
}
public class Test
{
	public static void main(String[] args) throws InterruptedException
	{
		TestBench[] tbSet;
		int len;
		if(args.length==0)
		{
			len = 1;
			tbSet = new TestBench[len];
			tbSet[0] = new TestBench(16);
		}
		else
		{
			len = args.length;
			tbSet = new TestBench[len];
			for (int i = 0; i < args.length; i++)
			{
				tbSet[i] = new TestBench(Integer.parseInt(args[i]));
			}
		}
		for (int i = 0; i < len; i++)
		{
			tbSet[i].test();
		}

	}
}
