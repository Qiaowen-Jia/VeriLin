package ticketingsystem;

public class TicketingDS implements TicketingSystem {
  
  private Route[] routes;
  
  private int coachNum;
  private int seatNum;
  private int stationNum;
  
  public TicketingDS(int routeNum, int coachNum, int seatNum, int stationNum,
                     int threadNum) {
    TicketIdAllocator.setThreadNum(threadNum);
    this.coachNum = coachNum;
    this.seatNum = seatNum;
    this.stationNum = stationNum;
    routes = new Route[routeNum];
    for (int id = 0; id < routeNum; id ++) {
      routes[id] = new Route(id, coachNum, seatNum, stationNum);
    }
  }
  
  @Override
  public Ticket buyTicket(String passenger, int route, int departure,
                          int arrival) {
    if (route <= 0 || route > routes.length) return null;
    if (!isValidDepartureAndArrival(departure, arrival)) return null;
    Ticket ticket = routes[route-1].buyTicket(passenger, departure, arrival);
    return ticket;
  }
  
  @Override
  public int inquiry(int route, int departure, int arrival) {
    if (!isValidDepartureAndArrival(departure, arrival)) return 0;
    return routes[route-1].inquiry(departure, arrival);
  }
  
  @Override
  public boolean refundTicket(Ticket ticket) {
    if (!isValidTicket(ticket)) return false;
    return routes[ticket.route-1].refundTicket(ticket);
  }
  
  private boolean isValidDepartureAndArrival(int departure, int arrival) {
    return (departure < arrival) && (departure > 0) && (arrival > 0)
        && (arrival <= stationNum);
  }
  
  private boolean isValidTicket(Ticket ticket) {
    return (ticket != null) && (ticket.passenger != null)
        && (ticket.route > 0) && (ticket.route <= routes.length)
        && (ticket.coach <= coachNum) && (ticket.coach > 0)
        && (ticket.seat <= seatNum) && (ticket.seat > 0)
        && isValidDepartureAndArrival(ticket.departure, ticket.arrival);
  }

}
