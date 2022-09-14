package ticketingsystem;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

class Route 
{
  private final int route;
  private final int coachnum;
  private ArrayList<Coach> coachlist;
  // 设置成原子对象，因为tid要唯一
  private AtomicLong ticket_uniqueId;
  //已经卖出的票
  private Queue<Long> queue_SoldTicket;

  public Route(final int route, final int coachnum, final int seatNum) 
  {
    this.route = route;
    this.coachnum = coachnum;
    this.coachlist = new ArrayList<Coach>(coachnum);
    this.ticket_uniqueId = new AtomicLong(0);
    this.queue_SoldTicket = new ConcurrentLinkedQueue<Long>();
    for (int coach = 1; coach <= coachnum; coach++)
    {
      this.coachlist.add(new Coach(coach, seatNum));
    }
  }

  public Ticket buyt(final String passenger, final int departure, final int arrival) 
  {
    int randnum = ThreadLocalRandom.current().nextInt(this.coachnum);
    //每张票的票号(唯一)
    long ticket_infoId;
    for (int i = 0; i < this.coachnum; i++) 
    {
      Ticket ticket = this.coachlist.get(randnum).buyt(departure, arrival);
      if (ticket != null) 
      {
        //每个route卖的票不超过1000000次
        ticket.tid = this.route*1000000 + this.ticket_uniqueId.getAndIncrement();
        ticket.passenger = passenger;
        ticket.route = this.route;
        ticket.departure = departure;
        ticket.arrival = arrival;
        //设置票号
        ticket_infoId = 0;
        ticket_infoId |= ticket.tid << 32 + ticket.coach << 26 + ticket.seat << 16 + ticket.departure << 8+ ticket.arrival;
        this.queue_SoldTicket.add(new Long(ticket_infoId));
        return ticket;
      } 
      randnum = (randnum+1) % this.coachnum;
    } 
    //gg
    return null;
  }

  public int inquiryt(final int departure, final int arrival) 
  {
    int tickets_left = 0;
    for (int coach = 1; coach <= this.coachnum; coach++)
    {
      tickets_left += this.coachlist.get(coach-1).inquiryt(departure, arrival);
    }
    return tickets_left;
  }

  public boolean refundt(final Ticket ticket) 
  {
    long ticket_infoId = 0;
    ticket_infoId |= ticket.tid << 32 + ticket.coach << 26 + ticket.seat << 16 + ticket.departure << 8 + ticket.arrival;
    //按道理应该有个时间判断，总不能都坐过了还能退吧，但我们不考虑了
    if (!this.queue_SoldTicket.contains(ticket_infoId)) 
    {
      return false;
    }
    else 
    {
      this.queue_SoldTicket.remove(ticket_infoId);
      return this.coachlist.get(ticket.coach-1).refundt(ticket.seat, ticket.departure, ticket.arrival);
    }
  }

}