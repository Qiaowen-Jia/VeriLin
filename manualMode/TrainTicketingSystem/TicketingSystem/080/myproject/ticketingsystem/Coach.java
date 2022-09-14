package ticketingsystem;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;


public class Coach {
    private final int CoachId; //车厢号
    private final int SeatNum; //每节车厢的座位数
    private ArrayList<Seat> SeatList; //座位列表

    public Coach(final int coachId, final int seatNum) {
        this.CoachId = coachId;
        this.SeatNum = seatNum;
        SeatList = new ArrayList<>(seatNum);

        for (int i = 1; i <= seatNum; i++) {
            this.SeatList.add(new Seat(i)); //初始化座位列表
        }
    }

    public Ticket buyTicket(final int departure, final int arrival) {
        Ticket ticket = new Ticket();

        //使用随机数提高查询速度
        int randomSeat = ThreadLocalRandom.current().nextInt(this.SeatNum);
        for (int i = 0; i < this.SeatNum; i++) {
            int inquiriedSeatId = this.SeatList.get(randomSeat).buyTicket(departure, arrival);
            if (inquiriedSeatId != -1) {
                ticket.coach = this.CoachId;
                ticket.seat = inquiriedSeatId;
                return ticket;
            }
            randomSeat = (randomSeat+1) % this.SeatNum;
        }
        return null;
    }

    public int inquiry(final int departure, final int arrival) {
        int result = 0;
        for (int i = 0; i < this.SeatNum; i++)
            result += this.SeatList.get(i).inquiry(departure, arrival);
        return result;
    }

    public boolean refundTicket(final int seatId, final int departure, final int arrival) {
        return this.SeatList.get(seatId-1).refundTicket(departure, arrival);
    }

}

