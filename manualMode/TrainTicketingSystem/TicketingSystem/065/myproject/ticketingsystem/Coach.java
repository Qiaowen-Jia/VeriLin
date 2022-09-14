package ticketingsystem;

import java.util.*;
import java.util.concurrent.*;

class Coach 
{
  //本课题中couch感觉不是很有必要
  private final int couch;
  private final int seatnum;
  private ArrayList<Seat> seatlist;

  public Coach(final int couch, final int seatnum) 
  {
    this.couch = couch;
    this.seatnum = seatnum;
    seatlist = new ArrayList<Seat>(seatnum);
    for (int seat = 1; seat <= seatnum; seat++)
    {
      this.seatlist.add(new Seat(seat));
    }
  }

  public Ticket buyt(final int departure, final int arrival) 
  {
    Ticket ticket = new Ticket();
    int randnum = ThreadLocalRandom.current().nextInt(this.seatnum);
    for (int i = 0; i < this.seatnum; i++) 
    {
      int seat = this.seatlist.get(randnum).buyt(departure, arrival);
      if (seat != -1) 
      {
        ticket.coach = this.couch;
        ticket.seat = seat;
        return ticket;
      } 
      randnum = (randnum+1) % this.seatnum;
    } 
    return null;
  }

  public int inquiryt(final int departure, final int arrival) 
  {
    int tickets_left = 0;
    for (int seat = 1; seat <= this.seatnum; seat++)
    {
      tickets_left += this.seatlist.get(seat-1).inquiryt(departure, arrival);
    }
    return tickets_left;
  }

  public boolean refundt(final int seat, final int departure, final int arrival) 
  {
    return this.seatlist.get(seat-1).refundt(departure, arrival);
  }

}