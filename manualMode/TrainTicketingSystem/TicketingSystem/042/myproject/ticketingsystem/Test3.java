package ticketingsystem;

import java.util.concurrent.atomic.AtomicInteger;

public class Test3 {
	public static void main(String[] args) throws InterruptedException {
		boolean t=true;
		int TestCount = 100000;
		for(int j=1;j<=10;j++)
		{
			//System.out.println("No."+ j + " TestCount:" + TestCount);
			if(t)
			{
				MyTest(1,TestCount,false);
				MyTest(2,TestCount,false);
				MyTest(4,TestCount,false);
				MyTest(8,TestCount,false);
				MyTest(16,TestCount,false);
				MyTest(32,TestCount,false);
				MyTest(64,TestCount,false);
				MyTest(128,TestCount,false);
			}
			else
			{
				MyTest(1,TestCount,true);
				//MyTest(16,10000,true);
			}
		}
	}
	
	public static void MyTest(int Num_of_Thread, int Num_of_Test,boolean output)
	{	
		int NUM_OF_ROUTE   =5;
		int NUM_OF_COACH   =8;
		int NUM_OF_SEAT    =100;
		int NUM_OF_STATION =10;	
		
		int NUM_OF_THREAD = Num_of_Thread;
		//Test 
		int NUM_OF_TEST = Num_of_Test;
	
		TicketingDS tds = new TicketingDS(NUM_OF_ROUTE, NUM_OF_COACH, NUM_OF_SEAT, NUM_OF_STATION, NUM_OF_THREAD);
		TicketingWindowThread tbt=new TicketingWindowThread(tds);
	
		tbt.setTotalNum(new AtomicInteger(NUM_OF_TEST));
		tbt.setThreadLocal(new ThreadLocal<Integer>());
		tbt.setOutputFlag(output); //show output
		
		int i;
		
		Thread[] threads = new Thread[NUM_OF_THREAD];
		for (i = 0; i < NUM_OF_THREAD; i++) {
			threads[i] = new Thread(tbt);
		}
		//STARTTIME
		long aTime = System.currentTimeMillis();
		
		for (i= 0; i < NUM_OF_THREAD; i++) {
			threads[i].start();
		}
		for (i = 0; i < NUM_OF_THREAD; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		aTime = System.currentTimeMillis()-aTime;
		
		System.out.println("Thread:" + NUM_OF_THREAD  + " Time:" + (float)(aTime)/1000 + " Throughout:" + NUM_OF_TEST*1000/aTime);
	}
}
