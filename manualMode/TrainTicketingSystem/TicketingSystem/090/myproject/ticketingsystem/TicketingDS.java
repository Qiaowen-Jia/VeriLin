package ticketingsystem;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class TicketingDS implements TicketingSystem {
  private final int routenum;
  private final int stationnum;
  private ArrayList<route_node> route_list;

  public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
    this.routenum = routenum;
    this.stationnum = stationnum;

    this.route_list = new ArrayList<route_node>(routenum);
    for (int routeId = 1; routeId <= routenum; routeId++)
      this.route_list.add(new route_node(routeId, coachnum, seatnum));
  }

  public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
    if ((route <=0) || (route > this.routenum) || (arrival > this.stationnum) || (departure >= arrival)) 
      return null;
    return this.route_list.get(route-1).trySealTic(passenger, departure, arrival);
  }

  public int inquiry(int route, int departure, int arrival) {
    if ((route <=0) || (route > this.routenum) || (arrival > this.stationnum) || (departure >= arrival)) 
      return -1;
    return this.route_list.get(route-1).inquiryTic(departure, arrival);
  }

  public boolean refundTicket(Ticket ticket) {
    final int routeId = ticket.route;
    if ((ticket == null) || (routeId <=0) || (routeId > this.routenum)) 
      return false;
    return this.route_list.get(routeId-1).tryRefundTic(ticket);
  }

}
