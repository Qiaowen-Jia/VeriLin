package ticketingsystem;

import java.util.*;

public class TicketingDS implements TicketingSystem {
  private final int routeNum;
  private final int stationNum;
  private ArrayList<Route> routeList;

  public TicketingDS(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
    this.routeNum = routeNum;
    this.stationNum = stationNum;
    this.routeList = new ArrayList<Route>(routeNum);
    for (int routeId = 1; routeId <= routeNum; routeId++)
      this.routeList.add(new Route(routeId, coachNum, seatNum));
  }
  @Override
  public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
    if (route <=0 || route > this.routeNum || arrival > this.stationNum || departure >= arrival) 
      return null;
    return this.routeList.get(route-1).trySellTicket(passenger, departure, arrival);
  }
  @Override
  public int inquiry(int route, int departure, int arrival) {
    if (route <=0 || route > this.routeNum || arrival > this.stationNum || departure >= arrival) 
      return -1;
    return this.routeList.get(route-1).inquiryTicket(departure, arrival);
  }
  @Override
  public boolean refundTicket(Ticket ticket) {
    final int routeId = ticket.route;
    if (ticket == null || routeId <=0 || routeId > this.routeNum) 
      return false;
    return this.routeList.get(routeId-1).tryRefundTicket(ticket);
  }

}
