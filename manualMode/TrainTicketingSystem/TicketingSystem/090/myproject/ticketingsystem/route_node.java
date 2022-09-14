package ticketingsystem;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

class route_node {
  private final int route_id;
  private final int coachnum;
  private ArrayList<coach_node> coach_list;
  private AtomicLong ticket_id;
  private Queue<Long> sold_ticket;

  public route_node(final int route_id, final int coachnum, final int seatnum) {
    this.route_id = route_id;
    this.coachnum = coachnum;
    this.coach_list = new ArrayList<coach_node>(coachnum);
    this.ticket_id = new AtomicLong(0);
    this.sold_ticket = new ConcurrentLinkedQueue<Long>();

    for (int coach_id = 1; coach_id <= coachnum; coach_id++)
      this.coach_list.add(new coach_node(coach_id, seatnum));
  }

  public int inquiryTic(final int departure, final int arrival) {
    int sum = 0;
    for (int i = 0; i < this.coachnum; i++) 
      sum += this.coach_list.get(i).inquiryTic(departure, arrival);
    return sum;
  }

  public Ticket trySealTic(final String passenger, final int departure, final int arrival) {
    int try_coach = ThreadLocalRandom.current().nextInt(this.coachnum);
    for (int i = 0; i < this.coachnum; i++) {
      Ticket ticket = this.coach_list.get(try_coach).trySealTic(departure, arrival);
      if (ticket != null) {
        ticket.tid = this.route_id*10000000 + this.ticket_id.getAndIncrement();
        ticket.departure = departure;
        ticket.arrival = arrival;
        ticket.passenger = passenger;
        ticket.route = this.route_id;
        

        long tic_hash = 0;
        tic_hash |= ticket.tid << 32;
        tic_hash |= ticket.coach << 24;
        tic_hash |= ticket.seat << 12;
        tic_hash |= ticket.departure << 6;
        tic_hash |= ticket.arrival;
        this.sold_ticket.add(new Long(tic_hash));
        return ticket;

      } 
      try_coach = (try_coach+1) % this.coachnum;
    } 
    return null;
  }

  public boolean tryRefundTic(final Ticket ticket) {
    long tic_hash = 0;
    tic_hash |= ticket.tid << 32;
    tic_hash |= ticket.coach << 24;
    tic_hash |= ticket.seat << 12;
    tic_hash |= ticket.departure << 6;
    tic_hash |= ticket.arrival;
    if (!this.sold_ticket.contains(tic_hash)) return false;
    else {
      this.sold_ticket.remove(tic_hash);
      return this.coach_list.get(ticket.coach-1).tryRefundTic(ticket.seat, ticket.departure, ticket.arrival);
    }
  }

}