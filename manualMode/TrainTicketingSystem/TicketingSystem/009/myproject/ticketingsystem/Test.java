package ticketingsystem;


import java.util.ArrayList;
import java.util.Random;


public class Test {

	final static int threadnum = 96; // concurrent thread number
	final static int routenum = 20;
	final static int coachnum = 15;
	final static int seatnum = 100;
	final static int stationnum = 10;

	final static int testnum = 500000;
	final static int retpc = 5; // return ticket operation is 5% percent
	final static int buypc = 20; // buy ticket operation is 15% percent
	final static int inqpc = 100; //inquiry ticket operation is 80% percent

	static String passengerName() {
		Random rand = new Random();
		long uid = rand.nextInt(testnum);
		return "passenger" + uid;
	}

	public static void main(String[] args) throws InterruptedException {

		int[] nums = {4,8,16,32,64,96};


		for(int threadnum : nums){

			Seller[] threads = new Seller[threadnum];
			TicketingSystem tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);

			for (int i = 0; i < threadnum; i++) {
				threads[i] = new Seller(tds);
			}

			long start = System.nanoTime();
			for (int i = 0; i < threadnum; i++) {
				threads[i].start();
			}
			for (int i = 0; i< threadnum; i++) {
				threads[i].join();
			}
			double totalTime = (System.nanoTime()-start)/1000000000.0;

			int countBuyTicket = 0;
			int countRefundTicket = 0;
			int countInquiry = 0;
			double timeOfBuyTicket = 0;
			double timeOfRefundTicket = 0L;
			double timeOfInquiry = 0L;

			for (int i=0; i<threadnum; i++) {
				timeOfBuyTicket += threads[i].timeOfBuyTicket/1000000000.0;
				countBuyTicket += threads[i].countBuyTicket;
				timeOfRefundTicket += threads[i].timeOfRefundTicket/1000000000.0;
				countRefundTicket += threads[i].countRefundTicket;
				timeOfInquiry += threads[i].timeOfInquiry/1000000000.0;
				countInquiry += threads[i].countInquiry;
			}

			double avgOfBuyTicket = timeOfBuyTicket / countBuyTicket;
			double avgOfRefundTicket = timeOfRefundTicket / countRefundTicket;
			double avgOfInquiry = timeOfInquiry / countInquiry;
			int totalInvoke = countBuyTicket+countRefundTicket+countInquiry;
			double tps = totalInvoke / totalTime;

			System.out.println("--------------------------------------------------------");
			System.out.println("Thread number: " +threadnum);
			System.out.println("total invokation: " + totalInvoke);
			System.out.println("total time: " + totalTime);
			System.out.println("average time of buyTicket: " + avgOfBuyTicket + " second");
			System.out.println("average time of refundTicket: " + avgOfRefundTicket + " second");
			System.out.println("average time of inquiry: " + avgOfInquiry + " second");
			System.out.println("method called per second: " + tps);
			System.out.println("--------------------------------------------------------");
		}
	}

	static class Seller extends Thread {

		private TicketingSystem tds;
		public int countBuyTicket = 0;
		public int countRefundTicket = 0;
		public int countInquiry = 0;
		public long timeOfBuyTicket = 0;
		public long timeOfRefundTicket = 0;
		public long timeOfInquiry = 0;

		public Seller(TicketingSystem tds) {
			this.tds = tds;
		}

		@Override
		public void run() {
			Random rand = new Random();
			Ticket ticket;
			ArrayList<Ticket> soldTicket = new ArrayList<>();

			for (int i = 0; i < testnum; i++) {

				int sel = rand.nextInt(inqpc);
				if (0 <= sel && sel < retpc && soldTicket.size() > 0) {
					int select = rand.nextInt(soldTicket.size());
					if ((ticket = soldTicket.remove(select)) != null) {

						long startTime = System.nanoTime();
						tds.refundTicket(ticket);
						long endTime = System.nanoTime();
						timeOfRefundTicket += endTime - startTime;
						countRefundTicket++;
					}

				} else if (retpc <= sel && sel < buypc) {
					String passenger = Test.passengerName();
					int route = rand.nextInt(routenum) + 1;
					int departure = rand.nextInt(stationnum - 1) + 1;
					int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure

					long startTime = System.nanoTime();
					ticket = tds.buyTicket(passenger, route, departure, arrival);
					long endTime = System.nanoTime();
					timeOfBuyTicket += endTime - startTime;
					countBuyTicket++;

					if (ticket != null) {
						soldTicket.add(ticket);
					}

				} else if (buypc <= sel && sel < inqpc) {
					int route = rand.nextInt(routenum) + 1;
					int departure = rand.nextInt(stationnum - 1) + 1;
					int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure

					long startTime = System.nanoTime();
					tds.inquiry(route, departure, arrival);
					long endTime = System.nanoTime();
					timeOfInquiry += endTime - startTime;
					countInquiry++;
				}
			}

		}

	}
}

