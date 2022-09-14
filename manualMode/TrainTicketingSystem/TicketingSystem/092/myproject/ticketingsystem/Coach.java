package ticketingsystem;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Coach {
	private final int coachId;
	private final int seatNum;
	private ArrayList<Seat> seatList;

	public Coach(final int coachId, final int seatNum) {
		this.coachId = coachId;
		this.seatNum = seatNum;
		seatList = new ArrayList<Seat>(seatNum);
		for (int i = 0; i < this.seatNum; i++) {
			seatList.add(new Seat(i + 1));
		}
	}

	public Ticket tryBuyTicket(final int departure, final int arrival) {
		Ticket ticket = new Ticket();
		for (int i = 0; i < this.seatNum; i++) {
			int seatId = this.seatList.get(i).tryBuyTicket(departure, arrival);
			if(seatId!=-1) {
				ticket.coach = this.coachId;
				ticket.seat = seatId;
				return ticket;
			}
		}
		return null;
	}

	public int tryInqueryTicket(final int departure, final int arrival) {
		int ticketNum = 0;
		for (int i = 0; i < this.seatNum; i++) {
			ticketNum += this.seatList.get(i).tryInqueryTicket(departure, arrival);
		}
		return ticketNum;
	}

	public boolean tryRefundTicket(final int seatId, final int departure, final int arrival) {
		return this.seatList.get(seatId - 1).tryRefundTicket(departure, arrival);
	}
}
