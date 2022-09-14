package ticketingsystem;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

class coach_node {
  private final int coach_id;
  private final int seatnum;
  private ArrayList<seat_node> seat;

  public coach_node(final int coach_id, final int seatnum) {
    this.coach_id = coach_id;
    this.seatnum = seatnum;
    seat = new ArrayList<seat_node>(seatnum);

    for (int seatId = 1; seatId <= seatnum; seatId++)
      this.seat.add(new seat_node(seatId));
  }

  public int inquiryTic(final int departure, final int arrival) {
    int ticSum = 0;
    for (int i = 0; i < this.seatnum; i++)
      ticSum += this.seat.get(i).inquiryTic(departure, arrival);
    return ticSum;
  }

  public Ticket trySealTic(final int departure, final int arrival) {
    Ticket ticket = new Ticket();
    int test_seat = ThreadLocalRandom.current().nextInt(this.seatnum);
    for (int i = 0; i < this.seatnum; i++) {
      int result = this.seat.get(test_seat).trySealTic(departure, arrival);
      if (result != -1) {
        ticket.coach = this.coach_id;
        ticket.seat = result;
        return ticket;
      } 
      test_seat = (test_seat+1) % this.seatnum;
    } 
    return null;
  }

  public boolean tryRefundTic(final int seatId, final int departure, final int arrival) {
    return this.seat.get(seatId-1).tryRefundTic(departure, arrival);
  }

}