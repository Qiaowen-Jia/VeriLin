package ticketingsystem;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TicketingDS implements TicketingSystem {

	public static final boolean CHECKREQ = false;

	private int routeNum   = 5;
	private int coachNum   = 8;
	private int seatNum    = 100;
	private int stationNum = 10;
	private int threadNum  = 16;
	public Train[] trains;
	public AtomicInteger ticketID;
	private ConcurrentHashMap<Long, Ticket> soldTicket;

	private void initTrain() {
		trains = new Train[routeNum];
		ticketID = new AtomicInteger(1);
		soldTicket = new ConcurrentHashMap<>();
        for (int i = 0; i < routeNum; i++) {
            trains[i] = new Train(coachNum, seatNum, stationNum);
        }
	}

	// Construct method
	public TicketingDS() {
		initTrain();
	}

	public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
		this.routeNum   = routenum;
		this.coachNum   = coachnum;
		this.seatNum    = seatnum;
		this.stationNum = stationnum;
		this.threadNum  = threadnum;
		initTrain();
	}

	// Implement TicketingSystem interface
	@Override
	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
		if (invalidReq(passenger, route, departure, arrival)) {
			return null;
		}
		int seat = trains[route-1].lockSeat(departure-1, arrival-1);
		if (seat < 0) {
            //System.out.println("No seats");
            return null;
        }
		Ticket ticket = new Ticket();
        ticket.tid = ticketID.getAndIncrement();
        ticket.passenger = passenger;
        ticket.route = route;
        ticket.departure = departure;
        ticket.arrival = arrival;
        ticket.coach = (seat / seatNum) + 1;
		ticket.seat = (seat % seatNum) + 1;
		soldTicket.put(ticket.tid, ticket);

        return ticket;
	}

	@Override
	public int inquiry(int route, int departure, int arrival) {
		if (invalidReq("__inquiry__", route, departure, arrival)) {
			return 0;
		}
		return trains[route-1].querySeat(departure-1, arrival-1);
	}

	@Override
	public boolean refundTicket(Ticket ticket) {
		if (ticket == null) {
			return false;
		}
		if(!soldTicket.containsKey(ticket.tid) || !ticket.equals(soldTicket.get(ticket.tid))){
			return false;
		}
		int seat = (ticket.coach-1) * seatNum + (ticket.seat-1);
        boolean ret = trains[ticket.route-1].unlockSeat(seat, ticket.departure-1, ticket.arrival-1);
        soldTicket.remove(ticket);
        return ret;
	}

	private boolean invalidReq(String passenger, int route, int departure, int arrival) {
		if (CHECKREQ)
			return (passenger == null || passenger.equals("") ||
					route <= 0 || departure <= 0 || arrival <= 0 ||
					route > routeNum || departure > stationNum || arrival > stationNum ||
					departure >= arrival);
		else
			return false;
	}
}
