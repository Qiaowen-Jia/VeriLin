package ticketingsystem;

import java.util.ArrayList;
import java.util.Random;

public class Test {
	
	static TicketingDS tds;
	static int threadnum = 4;
	final static int routenum = 20; 
	final static int coachnum = 10; 
	final static int seatnum = 100; 
	final static int stationnum = 16; 

	final static int testnum = 100000;
	final static int retpc = 10; // return ticket operation is 10% percent
	final static int buypc = 30; // buy ticket operation is 30% percent
	final static int inqpc = 100; //inquiry ticket operation is 60% percent
	
	static String passengerName() {
		Random rand = new Random();
		long uid = rand.nextInt(testnum);
		return "passenger" + uid; 
	}
	
	static class TestRunnable implements Runnable{
		private long[] counters;
		private long[] timers;
		
		public TestRunnable(long[] counters,long[] timers) {
			this.counters=counters;
			this.timers=timers;
		}
		
		@Override
        public void run() {
    		Random rand = new Random();
        	Ticket ticket = new Ticket();
    		ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();
    		long preTime,postTime;
     		for (int i = 0; i < testnum; i++) {
    			int sel = rand.nextInt(inqpc);
    			if (0 <= sel && sel < retpc ) { // return ticket
    				if(soldTicket.size() > 0) {
    				int select = rand.nextInt(soldTicket.size());
    				if ((ticket = soldTicket.remove(select)) != null) {
						preTime = System.nanoTime();
    					tds.refundTicket(ticket);
    					postTime = System.nanoTime();
    					timers[0]+=postTime-preTime;
    				} 
    				}
					counters[0]++;
    			} else if (retpc <= sel && sel < buypc) { // buy ticket
    				String passenger = passengerName();
    				int route = rand.nextInt(routenum) + 1;
    				int departure = rand.nextInt(stationnum - 1) + 1;
    				int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
					preTime = System.nanoTime();
    				ticket = tds.buyTicket(passenger, route, departure, arrival);
    				postTime = System.nanoTime();
    				timers[1]+=postTime-preTime;
    				counters[1]++;
    				soldTicket.add(ticket);
    			} else if (buypc <= sel && sel < inqpc) { // inquiry ticket
    				int route = rand.nextInt(routenum) + 1;
    				int departure = rand.nextInt(stationnum - 1) + 1;
    				int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
					preTime = System.nanoTime();
    				tds.inquiry(route, departure, arrival);
					postTime = System.nanoTime();
					timers[2]+=postTime-preTime;
					counters[2]++;
    			}
    		}
        }
    }
	
    public static void main(String[] args) throws InterruptedException {
        //测试线程数
    	int[] tests= {1,4,8,16,32,64,128};
        Thread[] threads;
        long[][] countersList;
        long[][] timersList;
        long[] counter,timer;
        int i,j,k;
        long startTime,endTime;
        double throughout;
        double[] avgTime=new double[3];
		for(j=0;j<tests.length;j++) {
			threadnum=tests[j];
	        threads = new Thread[threadnum];
			tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
			countersList=new long[threadnum][3];
			timersList=new long[threadnum][3];
			for(i=0;i<threadnum;i++) {
				for(k=0;k<3;k++) {
					countersList[i][k]=0;
					timersList[i][k]=0;
				}
			}
			for (i = 0; i< threadnum; i++) {
		    	threads[i] = new Thread(new TestRunnable(countersList[i],timersList[i]));
	        }
			startTime=System.currentTimeMillis();
			for(i=0;i<threadnum;i++) {
				threads[i].start();
			}
		    for (i = 0; i< threadnum; i++) {
		    	threads[i].join();
		    }
		    endTime=System.currentTimeMillis();
		    counter=new long[3];
		    timer=new long[3];
		    for(i=0;i<3;i++) {
		    	counter[i]=0;
		    	timer[i]=0;
		    }
		    System.out.println(tests[j]+" threads:");
		    for(i=0;i<threadnum;i++) {
		    	for(k=0;k<3;k++) {
		    		counter[k]+=countersList[i][k];
		    		timer[k]=timersList[i][k];
		    	}
		    }
		    for(i=0;i<3;i++) {
		    	avgTime[i]=(double)timer[i]/counter[i];
		    }
		    double calls=(double)counter[0]+counter[1]+counter[2];
		    throughout=calls/(endTime-startTime)*Math.pow(10, 3);
		    System.out.printf("avgtime: refund %.2fns, buy %.2fns, inquiry %.2fns\n",avgTime[0],avgTime[1],avgTime[2]);
		    System.out.printf("througout:%.2f\n",throughout);
		}
    }
}
