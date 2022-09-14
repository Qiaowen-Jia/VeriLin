package ticketingsystem;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicLong;

// 开放时间为2020年11月20日至2020年12月31日。IP：124.16.138.31。只能用ssh访问，ssh端口号为22345。
public class TicketingDS implements TicketingSystem {
		
	//ToDo
	private int routeNum, coachNum, seatNum, stationNum, threadNum;
	static private Object lock;
	RouteDS [] route;

	public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum)
	{
		this.routeNum = routenum;
		this.coachNum = coachnum;
		this.seatNum = seatnum;
		this.stationNum = stationnum;
		this.threadNum = threadNum;
		lock = new Object();
		route = new RouteDS[routeNum];
		for (int i = 0; i < routeNum; i++)
		{
			route[i] = new RouteDS(i + 1, stationNum, coachNum, seatNum, threadnum);
		}
	}

	@Override
	public int inquiry(int routeID, int departureID, int arrivalID)
	{
		try {
			if (routeID > routeNum) {
				throw new IllegalArgsException();
			}

			return route[routeID - 1].inquiry(departureID, arrivalID);
		} catch (IllegalArgsException iae) {
			return 0;
		}
	}



	// should we check ticket tid and passenger name in validity check?
	@Override
	public boolean refundTicket(Ticket ticket)
	{
		//synchronized (lock) {
			try {
				route[ticket.route - 1].refund(ticket);
				return true;
			} catch (InvalidTicketException ite) {
				return false;
			}
		//}

	}

	@Override
	public Ticket buyTicket(String passenger, int routeID, int departureID, int arrivalID) {
		//synchronized (lock) {


			try {
				if (routeID > routeNum) {
					throw new IllegalArgsException();
				}
				Ticket retTicket;
				retTicket = route[routeID - 1].buy(passenger, departureID, arrivalID);

				return retTicket;
			} catch (IllegalArgsException | NoTicketsException iae) {

				return null;
			}
		//}
	}
}
