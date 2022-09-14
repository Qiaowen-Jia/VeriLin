package ticketingsystem;

public class Route {
  
  private int fixedId, coachNum, seatNum, stationNum;
  private Seat[][] seats; // sold tickets
  Interval[][] intervals; // available tickets
  
  public Route(int fixedId, int coachNum, int seatNum, int stationNum) {
    this.fixedId = fixedId;
    this.coachNum = coachNum;
    this.seatNum = seatNum;
    this.stationNum = stationNum;
    
    intervals = new Interval[stationNum][stationNum];
    for (int i = 0; i < stationNum; i ++) {
      for (int j = i+1; j < stationNum; j ++) {
        intervals[i][j] = new Interval();
      }
    }
    
    seats = new Seat[coachNum][seatNum];
    for (int i = 0; i < coachNum; i ++) {
      for (int j = 0; j < seatNum; j ++) {
        seats[i][j] = new Seat(i, j);
        TicketNode ticketNode = seats[i][j].initTicketNode(stationNum);
        intervals[0][stationNum-1].pushWithoutExchange(ticketNode);
      }
    }
  }
  
  private Ticket fillInTicket(TicketNode ticketNode) {
    Ticket ticket = new Ticket();
    ticket.tid = ticketNode.tid;
    ticket.departure = ticketNode.departure;
    ticket.arrival = ticketNode.arrival;
    ticket.passenger = ticketNode.passenger;
    ticket.coach = ticketNode.owner.fixedCoachId + 1;
    ticket.seat = ticketNode.owner.fixedSeatId + 1;
    ticket.route = fixedId + 1;
    return ticket;
  }
  
  private boolean isTwoTicketsMatch(Ticket ticket, TicketNode node) {
    return (ticket.passenger != null) && ticket.passenger.equals(node.passenger)
      && (ticket.departure == node.departure) && (ticket.arrival == node.arrival);
  }

  public Ticket buyTicket(String passenger, int departure, int arrival) {
    Ticket ticket = null;
    TicketNode ticketNode = findTicket(departure, arrival);
    if (ticketNode != null) {
      ticketNode.owner.trySplitTicketNode(this, ticketNode, departure, arrival);
      ticketNode.passenger = passenger;
      ticketNode.tid = TicketIdAllocator.getTicketId();
      ticketNode.departure = departure;
      ticketNode.arrival = arrival;
      ticket = fillInTicket(ticketNode);
    }
    return ticket;
  }
  
  public int inquiry(int departure, int arrival) {
    int sum = 0;
    for (int i = 0; i < departure; i ++) {
      for (int j = arrival-1; j < stationNum; j ++)
        sum += intervals[i][j].getNumOfFreeTickets();
    }
    return sum;
  }
  
  public boolean refundTicket(Ticket ticket) {
    Seat seat = seats[ticket.coach-1][ticket.seat-1];
    TicketNode ticketNode = seat.findTicketNode(ticket.tid);
    if ((ticketNode != null) && isTwoTicketsMatch(ticket, ticketNode)) {
      ticketNode.tid = -1;
      ticketNode.free.set(true);
      ticketNode.owner.tryMergeTicketNode(this, ticketNode);
      intervals[ticketNode.departure-1][ticketNode.arrival-1].push(ticketNode);
      return true;
    }
    return false;
  }
  
  private TicketNode findTicketNodeBestMatch(int departure, int arrival) {
    int d, a;
    TicketNode ticketNode;
    // first stage...
    // searching (d, a) -> (d-1, a) -> (d, a+1) -> ...
    while ((departure >= 1) && (arrival <= stationNum)) {
      d = (departure-1 >= 1) ? (departure-1) : 1;
      a = (arrival+1 <= stationNum) ? (arrival+1) : stationNum;
      ticketNode = intervals[d-1][a-1].pop();
      if (ticketNode != null) return ticketNode;
      if (d != departure) {
        ticketNode = intervals[d-1][arrival-1].pop();
        if (ticketNode != null)  return ticketNode;
      }
      if (a != arrival) {
        ticketNode = intervals[departure-1][a-1].pop();
        if (ticketNode != null) return ticketNode;
      }
      departure --;
      arrival ++;
    }
    // second stage...
    if (departure >= 1) {
      departure --;
      while (departure > 0) {
        ticketNode = intervals[departure-1][stationNum-1].pop();
        departure --;
        if (ticketNode != null)
          return ticketNode;
      }
    }
    if (arrival <= stationNum) {
      arrival ++;
      while (arrival <= stationNum) {
        ticketNode = intervals[0][arrival-1].pop();
        arrival ++;
        if (ticketNode != null)
          return ticketNode;
      }
    }
    return null;
  }
  
  private TicketNode findTicketNodeLeastMatch(int departure, int arrival) {
    TicketNode ticketNode;
    for (int i = 0; i < departure; i ++) {
      for (int j = stationNum - 1; j >= arrival - 1; j --) {
        ticketNode = intervals[i][j].pop();
        if (ticketNode != null)
          return ticketNode;
      }
    }
    return null;
  }
  
  private TicketNode findTicket(int departure, int arrival) {
    TicketNode ticketNode = intervals[departure-1][arrival-1].pop();
    if (ticketNode != null)
      return ticketNode;
    ticketNode = findTicketNodeLeastMatch(departure, arrival);
    if (ticketNode != null)
      return ticketNode;
    ticketNode = findTicketNodeBestMatch(departure, arrival);
    return ticketNode;
  }
}
