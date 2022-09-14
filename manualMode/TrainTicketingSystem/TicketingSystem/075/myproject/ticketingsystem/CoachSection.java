package ticketingsystem;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

class CoachSection {

	private final int coachId;//车厢序号
	private final int seatNum;//座位数目
	private ArrayList<SeatSection> seatList;//座位列表

	public CoachSection(final int coachId, final int seatNum) {

		this.coachId = coachId;
		this.seatNum = seatNum;
		//建立数组，存储座位的情况
		seatList = new ArrayList<SeatSection>(seatNum);

		int seatId = 1;
		while(seatId <= seatNum){
			this.seatList.add(new SeatSection(seatId));
		    seatId++;
		}
	}

	//尝试买票
	public Ticket initSeal(final int departure, final int arrival) {

		Ticket ticket = new Ticket();
		int randSeat = ThreadLocalRandom.current().nextInt(this.seatNum);

		int i = 0;
		while(i < this.seatNum){

			int resultSeatId = this.seatList.get(randSeat).initSeal(
					departure, arrival);

			if (resultSeatId != -1) {
				ticket.coach = this.coachId;
				ticket.seat = resultSeatId;
				return ticket;
			}
			randSeat = (randSeat + 1) % this.seatNum;
			i++;
		}
		return null;
	}

	//尝试查询
	public int initInquiry(final int departure, final int arrival) {

		int ticSum = 0;
		int i = 0;
		while(i < this.seatNum){
			ticSum += this.seatList.get(i).initInquiry(departure, arrival);
			i++;
		}
		return ticSum;

	}

	//尝试退票
	public boolean initRefund(final int seatId, final int departure,
			final int arrival) {
		return this.seatList.get(seatId - 1).initRefund(departure, arrival);
	}

}