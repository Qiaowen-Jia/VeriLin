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

        for (int seatId = 1; seatId <= seatNum; seatId++)
            this.seatList.add(new Seat(seatId));
    }

    public Ticket trySellTicket(final int departure, final int arrival) {
        Ticket ticket = new Ticket();
        //遍历所有seat，查看座位情况
        int randSeat = ThreadLocalRandom.current().nextInt(this.seatNum);
        for (int i = 0; i < this.seatNum; i++) {
            int resultSeatId = this.seatList.get(randSeat).trySellTicket(departure, arrival);
            if (resultSeatId != -1) {
                ticket.coach = this.coachId;
                ticket.seat = resultSeatId;
                return ticket;
            }
            randSeat = (randSeat+1) % this.seatNum;
        }
        return null;

    }

    public int inquiryTicket(final int departure, final int arrival) {
        int ticketSum = 0;
        for (int i = 0; i < this.seatNum; i++)
            ticketSum += this.seatList.get(i).inquiryTicket(departure, arrival);
        return ticketSum;
    }

    public boolean tryRefundTicket(final int seatId, final int departure, final int arrival) {
        return this.seatList.get(seatId-1).tryRefundTicket(departure, arrival);
    }

}
