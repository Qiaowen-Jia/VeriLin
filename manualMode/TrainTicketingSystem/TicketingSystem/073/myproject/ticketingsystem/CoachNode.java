package ticketingsystem;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Author: fnbory
 * @Date: 4/1/2020 下午 8:53
 */
public class CoachNode {
    private final int coachId;
    private final int seatNum;
    private ArrayList<SeatNode> seatList;

    public CoachNode(final int coachId, final int seatNum) {
        this.coachId = coachId;
        this.seatNum = seatNum;
        seatList = new ArrayList<SeatNode>(seatNum);

        for (int seatId = 1; seatId <= seatNum; seatId++)
            this.seatList.add(new SeatNode(seatId));
    }

    public Ticket trySealTic(final int departure, final int arrival) {
        Ticket ticket = new Ticket();
        //在指定车厢遍历所有seat。此时问题化简为座位和站牌，不用关心车厢和车次。
        // 第一次随机，原因跟在车厢随机一样
        int randSeat = ThreadLocalRandom.current().nextInt(this.seatNum);
        for (int i = 0; i < this.seatNum; i++) {
            int resultSeatId = this.seatList.get(randSeat).trySealTic(departure, arrival);
            if (resultSeatId != -1) {
                ticket.coach = this.coachId;
                ticket.seat = resultSeatId;
                return ticket;
            }
            randSeat = (randSeat+1) % this.seatNum;
        }
        return null;

    }

    public int inquiryTic(final int departure, final int arrival) {
        int ticSum = 0;
        for (int i = 0; i < this.seatNum; i++)
            ticSum += this.seatList.get(i).inquiryTic(departure, arrival);
        return ticSum;
    }

    public boolean tryRefundTic(final int seatId, final int departure, final int arrival) {
        return this.seatList.get(seatId-1).tryRefundTic(departure, arrival);
    }

}

