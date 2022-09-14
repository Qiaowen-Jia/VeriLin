package ticketingsystem;

import java.util.*;

public class TicketingDS implements TicketingSystem 
{		
  private final int routenum;
  private final int coachnum;
  private final int seatnum;
  private final int stationnum;
  private ArrayList<Route> routelist;

  public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum)
  {
    this.routenum = routenum;
    this.coachnum = coachnum;
    this.seatnum = seatnum;
    this.stationnum = stationnum;
    //把route都添加到list里
    this.routelist = new ArrayList<Route>(routenum);

    for (int route = 1; route <= routenum; route++)
    {
      this.routelist.add(new Route(route, coachnum, seatnum));
    }
  }

  public Ticket buyTicket(String passenger, int route, int departure, int arrival) 
  {
    if (route <=0 || route > this.routenum)
    {
      System.out.println("route_err:"+route);
      System.out.flush();
      return null;
    }
    else if (arrival > this.stationnum || arrival <= departure ) 
    {
      System.out.println("arrival_err:"+arrival);
      System.out.flush();
      return null;
    }
    else
    {
      return this.routelist.get(route-1).buyt(passenger, departure, arrival);
    }
  }

  public int inquiry(int route, int departure, int arrival) 
  {
    if (route <=0 || route > this.routenum )
    {
      System.out.println("route_err:"+route);
      System.out.flush();
      return -1;
    }
    else if (arrival > this.stationnum || departure >= arrival) 
    {
      System.out.println("arrival_err:"+arrival);
      System.out.flush();
      return -1;
    }
    else
    {
      return this.routelist.get(route-1).inquiryt(departure, arrival);
    }
  }

  public boolean refundTicket(Ticket ticket) 
  {
    final int route = ticket.route;
    if (ticket == null || route <=0 || route > this.routenum) 
    {
      System.out.println("stupid_err:"+route);
      System.out.flush();
      return false;
    }
    else
    {
      return this.routelist.get(route-1).refundt(ticket);
    }
  }

}
