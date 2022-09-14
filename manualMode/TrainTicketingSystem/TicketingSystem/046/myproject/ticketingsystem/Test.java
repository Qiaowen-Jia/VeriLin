package ticketingsystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Test {
	public static boolean DEBUG = false;

	public static void main(String[] args){
        if(args.length > 0 && args[0].equals("debug")){
			DEBUG = true;
		}
		int[] threadNums = {1, 4, 8, 16, 32, 64, 128};
		int testNum = 1000000;

		long startTime = System.currentTimeMillis();
		for(int i=0; i<threadNums.length; ++i){
			mutiThreadTest(testNum, threadNums[i]);
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Total Time Cost : " + (endTime - startTime) + "ms.");
	}

    public static void mutiThreadTest(int testNum, int threadNum){
		long startTime = System.currentTimeMillis();
    	final TicketingDS tds = new TicketingDS();
    	
    	TicketAgent[] threads = new TicketAgent[threadNum];
    	
    	for(int i=0; i<threadNum; ++i) {
    		threads[i] = new TicketAgent(tds, testNum);
    		threads[i].start();
    	}

        double[] methodTimeCost = new double[]{0.0, 0.0, 0.0};
		int[] methodExecTimes = new int[]{1, 1, 1};
	
    	for(int i=0; i<threadNum; ++i) {
    		try {
				threads[i].join();

				for(int j=0; j<3; ++j){
				   methodExecTimes[j] += threads[i].methodExecTimes[j];
				   methodTimeCost[j] += threads[i].methodTimeCost[j];
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
		long endTime = System.currentTimeMillis();
		double totalTime = (endTime - startTime)/1000.0;

		double refundAvg = methodTimeCost[TicketAgent.REFUND_INDEX]/methodExecTimes[TicketAgent.REFUND_INDEX];
		double buyAvg = methodTimeCost[TicketAgent.BUY_INDEX]/methodExecTimes[TicketAgent.BUY_INDEX]; 
		double inqueryAvg = methodTimeCost[TicketAgent.INQUERY_INDEX]/methodExecTimes[TicketAgent.INQUERY_INDEX]; 
		double throughoutput = testNum*threadNum/totalTime; 
   
		System.out.printf("ThreadNum(%d), TestNum(%d) : Total Time Cost : %f s.\n", threadNum, testNum, totalTime);
		System.out.println("\tRefund Ticket Average Time :  " + refundAvg + " ms");
		System.out.println("\tBuy Ticket Average Time :  " + buyAvg + " ms");
		System.out.println("\tInquery Average Time :  " + inqueryAvg + " ms");
		System.out.println("\tThroughoutput " + throughoutput + " jobs/s");
		System.out.printf("%d %f %f %f %d %f %f\n\n", threadNum, refundAvg, buyAvg, inqueryAvg, testNum, throughoutput, totalTime);
    }
}


class TicketAgent extends Thread{

	private TicketingDS tds;
	private int testNum;
	
	private List<Ticket> soldTickets;
	
	private double totalTimeCost;
	double methodTimeCost[];
	int methodExecTimes[];
	
	private Random random;
	
	public TicketAgent(TicketingDS tds, int testNum) {
		this.tds = tds;
		this.testNum = testNum;
		
		soldTickets = new ArrayList<>();
		methodTimeCost = new double[] {0, 0, 0};
		methodExecTimes = new int[] {0, 0, 0};
		
		random = new Random(0);
	}
	
	
	public static final double REFUND_RATE = 0.1;
	public static final double BUY_RATE = 0.3 + REFUND_RATE;
	public static final double INQUERT_RATE = 0.6;
	
	public static final int REFUND_INDEX = 0;
	public static final int BUY_INDEX = 1;
	public static final int INQUERY_INDEX = 2;
	
	private String threadName="";
	
	@Override
	public void run() {
		threadName = Thread.currentThread().getName();
		long startTime = System.currentTimeMillis();
		
		for(int i=0; i<testNum; ++i) {
			double random = Math.random();
			if(random <= REFUND_RATE) {
				methodExecTimes[REFUND_INDEX] ++;
				long methodStartTime = System.currentTimeMillis();
				refundTicket();
				long methodEndTime = System.currentTimeMillis();
				methodTimeCost[REFUND_INDEX] += (methodEndTime - methodStartTime);
			}else if(random <= BUY_RATE){
				methodExecTimes[BUY_INDEX] ++;
				long methodStartTime = System.currentTimeMillis();
				buyTicket();
				long methodEndTime = System.currentTimeMillis();
				methodTimeCost[BUY_INDEX] += (methodEndTime - methodStartTime);
			}else {
				methodExecTimes[INQUERY_INDEX] ++;
				long methodStartTime = System.currentTimeMillis();
				inqueryTicket();
				long methodEndTime = System.currentTimeMillis();
				methodTimeCost[INQUERY_INDEX] += (methodEndTime - methodStartTime);
			}
		}
		
		long endTime = System.currentTimeMillis();
		totalTimeCost = endTime - startTime;
		if(Test.DEBUG){
			System.err.println(stats());
		}
	}
	
	public void refundTicket() {
		int size = soldTickets.size();
		
		if(size == 0) {
			print(threadName + " There is no ticket to refund...");
			return;	
		}
		
		Ticket ticket = soldTickets.remove(random.nextInt(size));
		
		if(ticket == null) {
			print(threadName + "There is no ticket to refund...");
			return;	
		}
		
		boolean ret = tds.refundTicket(ticket);
		print(threadName + " Refund " + ticket + (ret ? " succeed!" : " failed!"));	
	}
	
	public void buyTicket() {
		int route = random.nextInt(tds.getRoutenum()) + 1;
		int arrival = random.nextInt(tds.getStationnum()) + 1;
		int departure = random.nextInt(arrival) + 1;
		Ticket ticket = tds.buyTicket("zhangsan", route, departure, arrival);
		print(threadName + " Buy " + (ticket!=null ? ticket + " scueed!" : ("Route " + route + " from " + departure + " to " + arrival + " failed!")));
		
		// used by refund function
		if(ticket != null)
			soldTickets.add(ticket);
	}
	
	public void inqueryTicket() {
		int route = random.nextInt(tds.getRoutenum()) + 1;
		int arrival = random.nextInt(tds.getStationnum()) + 1;
		int departure = random.nextInt(arrival) + 1;
		int seats = tds.inquiry(route, departure, arrival);
		print(threadName + " Inquery Route " + route + " from " + departure + " to " + arrival + " has " + seats + " tickets.");
	}
	
	public String stats() {
		StringBuilder sb = new StringBuilder(Thread.currentThread().getName() + "\n");
		sb.append("----" + " Total Time Cost " + totalTimeCost + "ms\n");
		sb.append("----" + " Average Time Cost " + totalTimeCost/(testNum==0 ? 1 : testNum) + "ms\n");
		sb.append(methodStats("Refund", REFUND_INDEX));
		sb.append(methodStats("Buy", BUY_INDEX));
		sb.append(methodStats("Inquery", INQUERY_INDEX));
		return sb.toString();
	}
	
	public String methodStats(String methodName, int index) {
		StringBuilder sb = new StringBuilder();
		sb.append("-----------------------------------------------------\n");
		sb.append("-------- " + methodName + " Method Cost " + methodTimeCost[index] + "ms\n");
		sb.append("-------- " + methodName + " Method Exection Times " + methodExecTimes[index] + "\n");
		sb.append("-------- " + methodName + " Method Average Cost " + methodTimeCost[index]/(methodExecTimes[index]==0? 1 : methodExecTimes[index]) + "ms\n");
		sb.append("-----------------------------------------------------\n");	
		return sb.toString();
	}

	public void print(String str){
		if(Test.DEBUG){
			System.out.println(str);
		}
	}
}

