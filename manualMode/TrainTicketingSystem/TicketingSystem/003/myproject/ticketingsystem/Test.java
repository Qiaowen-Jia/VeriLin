package ticketingsystem;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLongArray;

class ThreadId_t {
	private static final AtomicInteger nextId = new AtomicInteger(0);
	private static final ThreadLocal<Integer> threadId = new ThreadLocal<Integer>() {
		@Override
		protected Integer initialValue() {
			return nextId.getAndIncrement();
		}
	};

	public static int get() {
		return threadId.get();
	}
}

public class Test {
	static int coachnum = 10;
	static int routenum = 20;
	static int seatnum = 100;
	static int stationnum = 16;
	static int threadnum = 64;

	static int testnum = (int) 1E5; // 10E3
	static int retpc = 10;
	static int buypc = 30;
	static int inqpc = 100;

	final static Long INI = (long) 1 << 64;
	private static final AtomicLongArray Delay = new AtomicLongArray(3);
	private static final AtomicLongArray Times = new AtomicLongArray(3);

	public static void main(String[] args) throws InterruptedException {
		
		for(int i = 0;i<args.length;i++){
			String arg = args[i];
			if (arg.equals("--threadnum")) {
				threadnum = Integer.parseInt(args[i+1]);
			}
			if (arg.equals("--routenum")) {
				routenum = Integer.parseInt(args[i+1]);
			}
			if (arg.equals("--coachnum")) {
				coachnum = Integer.parseInt(args[i+1]);
			}
			if (arg.equals("--seatnum")) {
				seatnum = Integer.parseInt(args[i+1]);
			}
			if (arg.equals("--stationnum")) {
				stationnum = Integer.parseInt(args[i+1]);
			}
			if (arg.equals("--testnum")) {
				testnum = Integer.parseInt(args[i+1]);
			}
		}
		Thread[] threads = new Thread[threadnum];
		final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
		final long startTime = System.nanoTime();
		final Name name = new Name();

		for (int i = 0; i < threadnum; i++) {
			threads[i] = new Thread(new Runnable() {
				public void run() {
					Random rand = new Random();
					Ticket ticket = new Ticket();
					ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();

					for (int i = 0; i < testnum; i++) {
						int sel = rand.nextInt(inqpc);
						if (0 <= sel && sel < retpc && soldTicket.size() > 0) { // return ticket
							int select = rand.nextInt(soldTicket.size());
							if ((ticket = soldTicket.remove(select)) != null) {
								long preTime = System.nanoTime() - startTime;
								if (tds.refundTicket(ticket)) {
									long postTime = System.nanoTime() - startTime;
									// Record the time delay and throughput.
									long delta = postTime - preTime;
									Delay.addAndGet(2, delta);
									Times.incrementAndGet(2);
									// System.out.println(preTime + " " + postTime + " " + ThreadId_t.get() + " "
									// 		+ "TicketRefund" + " id:" + ticket.tid + " " + ticket.passenger + " rt:"
									// 		+ ticket.route + " co:" + ticket.coach + " dp" + ticket.departure + " ar"
									// 		+ ticket.arrival + " st" + ticket.seat);
									// System.out.flush();
								} else {
									long postTime = System.nanoTime() - startTime;
									System.out.println(
											preTime + " " + postTime + " " + ThreadId_t.get() + " " + "ErrOfRefund");
									System.out.flush();
								}
							} else {
								long preTime = System.nanoTime() - startTime;
								System.out.println(preTime + " " + String.valueOf(System.nanoTime() - startTime) + " "
										+ ThreadId_t.get() + " " + "ErrOfRefund");
								System.out.flush();
							}
						} else if (retpc <= sel && sel < buypc) { // buy ticket
							String passenger = name.passengerName();
							int route = rand.nextInt(routenum) + 1;
							int departure = rand.nextInt(stationnum - 1) + 1;
							int arrival = departure + rand.nextInt(stationnum - departure) + 1;
							long preTime = System.nanoTime() - startTime;
							if ((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null) {
								long postTime = System.nanoTime() - startTime;
								// Record the time delay and throughput.
								long delta = postTime - preTime;
								Delay.addAndGet(1, delta);
								Times.incrementAndGet(1);
								// System.out.println(preTime + " " + postTime + " " + ThreadId_t.get() + " "
								// 		+ "TicketBought" + " id" + ticket.tid + " " + ticket.passenger + " rt"
								// 		+ ticket.route + " co" + ticket.coach + " " + ticket.departure + "->"
								// 		+ ticket.arrival + " st" + ticket.seat);
								// System.out.flush();
								soldTicket.add(ticket);
							} else {
								// System.out.println(preTime + " " + String.valueOf(System.nanoTime() - startTime) + " "
								// 		+ ThreadId_t.get() + " " + "TicketSoldOut" + " " + route + " " + departure + " "
								// 		+ arrival);
								// System.out.flush();
							}
						} else if (buypc <= sel && sel < inqpc) { // inquiry ticket
							int route = rand.nextInt(routenum) + 1;
							int departure = rand.nextInt(stationnum - 1) + 1;
							int arrival = departure + rand.nextInt(stationnum - departure) + 1;
							long preTime = System.nanoTime() - startTime;
							int leftTicket = tds.inquiry(route, departure, arrival);
							long postTime = System.nanoTime() - startTime;
							// Record the time delay and throughput.
							long delta = postTime - preTime;
							Delay.addAndGet(0, delta);
							Times.incrementAndGet(0);
							// System.out.println(preTime + " " + postTime + " " + ThreadId_t.get() + " " + "RemainTicket"
							// 		+ " " + leftTicket + " " + route + " " + departure + " " + arrival);
							// System.out.flush();
						}
					}

				}
			});
			threads[i].start();
		}
		for (int i = 0; i < threadnum; i++) {
			threads[i].join();
		}
		
		// Record the time delay and throughput.
		long StopTime = System.nanoTime();
		long TotalDelay = StopTime - startTime;
		long TotalTimes = Times.get(0) + Times.get(1) + Times.get(2);
		// Date date = new Date();
		// System.out.println('\n' + date.toString());
		// System.out.println("ThreadNum:\t" + threadnum + "\ntestnum:\t" + testnum + "\nRefund rate:\t" + retpc
		// 		+ "%\nBuy rate:\t" + (buypc - retpc) + "%\nInquiry rate:\t" + (inqpc - buypc) + "%");
		// System.out.println("routenum:\t" + routenum + "\ncoachnum:\t" + coachnum + "\nseatnum:\t" + seatnum
		// 		+ "\nstationnum:\t" + stationnum);
		// System.out.println("Inquiry Delay:\t" + 1.0 * Delay.get(0) / Times.get(0) + "ns\nBuy Delay:\t"
		// 		+ 1.0 * Delay.get(1) / Times.get(1) + "ns\nRefund Delay:\t" + 1.0 * Delay.get(2) / Times.get(2)
		// 		+ "ns\nThroughput:\t" + 1E9 * TotalTimes / TotalDelay);
		// System.out.flush();

		System.out.printf("\n%d\t%.2fns\t%.2fns\t%.2fns\t%d", 
				threadnum, 1.0 * Delay.get(0) / Times.get(0), 1.0 * Delay.get(1) / Times.get(1)
						, 1.0 * Delay.get(2) / Times.get(2), (int)1E9 * TotalTimes / TotalDelay);
		System.out.flush();
	}
}

class Name {
	private String[] name = { "Ada", "Allen", "Ann", "Ava", "Alice", "Amy", "Anna", "Apple", "Abel", "Adam", "Bart",
			"Betty", "Bess", "Belle", "Ben", "Bill", "Blake", "Bob", "Booth", "Borg", "Chad", "Carol", "Candy", "Cathy",
			"Claude", "Cherry", "Cash", "Chester", "Carl", "Cliff", "Dean", "Daisy", "Dana", "Dolly", "Doris", "David",
			"Devin", "Donald", "Duke", "Duncan", "Elliot", "Emily", "Edith", "Ella", "Elton", "Ed", "Elma", "Eric",
			"Edison", "Eve", "Fanny", "Fiona", "Fitch", "Felix", "Freda", "Fabian", "Ford", "Frank", "Gale", "Gary",
			"Gloria", "Gene", "Gavin", "Gill", "Grace", "Hedda", "Helen", "Hilary", "Honey", "Hunter", "Hale", "Harvey",
			"Hardy", "Harley", "Henry", "Ian", "Ida", "Ira", "Iris", "Isaac", "Ivan", "Ives", "Jack", "Jane", "Jeff",
			"Jean", "Jessica", "Jim", "Jimmy", "Judy", "Jo", "Joyce", "Kate", "Kent", "Kelly", "Ken", "Kay", "Kitty",
			"Lena", "Linda", "Lily", "Lisa", "Leo", "Louis", "Lucien", "Lucy", "Lewis", "Martin", "Matt", "Max", "Mary",
			"Maria", "Mike", "Morgan", "May", "Merry", "Monica", "Nancy", "Nat", "Nick", "Neil", "Nicole", "Oliver",
			"Omar", "Otis", "Ophelia", "Owen", "Page", "Parker", "Paddy", "Paul", "Peter", "Polly", "Prima", "Queena",
			"Quincy", "Quinn", "Rex", "Ruby", "Rudy", "Rose", "Rita", "Robert", "Rock", "Rory", "Sam", "Sampson",
			"Sally", "Shelly", "Sandy", "Steven", "Susan", "Sunny", "Sara", "Selena", "Tess", "Tina", "Tab", "Ted",
			"Tracy", "Taylor", "Tom", "Tony", "Tyler", "Upton", "Ula", "Victoria", "Vivian", "Vincent", "Vito", "Vicky",
			"Vic", "Victor", "Wade", "Ward", "Wendy", "Walt", "Will", "Wallis", "Winni", "Ziv", "Zona", "Zora" };
	static Random rand = new Random();

	Name() {

	}

	String passengerName() {
		int uid = rand.nextInt(name.length);
		return name[uid];
	}
}