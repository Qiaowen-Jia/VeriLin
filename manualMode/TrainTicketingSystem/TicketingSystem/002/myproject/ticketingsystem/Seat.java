package ticketingsystem;

import java.util.concurrent.atomic.AtomicBoolean;

class TicketNode {
  long tid;
  int departure;
  int arrival;
  String passenger;
  Seat owner;
  TicketNode left, right;

  TicketNode next; // for push/pop
  AtomicBoolean free = new AtomicBoolean(true); // for lazy deletion
  
  public TicketNode(Seat owner, TicketNode left, TicketNode right,
                    int departure, int arrival) {
    this.tid = -1; // not used.
    this.departure = departure;
    this.arrival = arrival;
    this.owner = owner;
    this.left = left;
    this.right = right;
    this.next = null;
  }
  
  public boolean isUsable() {
    return free.compareAndSet(true, false);
  }
}

public class Seat {
  int fixedCoachId, fixedSeatId;
  TicketNode head = new TicketNode(this, null, null, -1, -1);
  
  public Seat(int fixedCoachId, int fixedSeatId) {
    this.fixedCoachId = fixedCoachId;
    this.fixedSeatId = fixedSeatId;
  }
  
  public TicketNode initTicketNode(int stationNum) {
    TicketNode node = new TicketNode(this, head, head, 1, stationNum);
    head.left = node;
    head.right = node;
    return node;
  }
  
  private void tryMergeLeft(Route route, TicketNode ticketNode) {
    TicketNode leftNode = ticketNode.left;
    if (leftNode != head && leftNode.isUsable()) {
      ticketNode.left = leftNode.left;
      ticketNode.departure = leftNode.departure;
      leftNode.left.right = ticketNode;
      route.intervals[leftNode.departure-1][leftNode.arrival-1].decSize();
    }
  }
  
  private void tryMergeRight(Route route, TicketNode ticketNode) {
    TicketNode rightNode = ticketNode.right;
    if (rightNode != head && rightNode.isUsable()) {
      ticketNode.right = rightNode.right;
      ticketNode.arrival = rightNode.arrival;
      rightNode.right.left = ticketNode;
      route.intervals[rightNode.departure-1][rightNode.arrival-1].decSize();
    }
  }
  
  public void trySplitTicketNode(Route route, TicketNode ticketNode,
                                 int departure, int arrival) {
    if (ticketNode.departure < departure) {
      TicketNode leftNode = new TicketNode(this, null, ticketNode,
                                           ticketNode.departure, departure);
      synchronized (this) {
        leftNode.left = ticketNode.left;
        ticketNode.left.right = leftNode;
        ticketNode.left = leftNode;
        tryMergeLeft(route, leftNode);
      }
      route.intervals[leftNode.departure-1][leftNode.arrival-1].push(leftNode);
    }
  
    if (arrival < ticketNode.arrival) {
      TicketNode rightNode = new TicketNode(this, ticketNode, null,
                                            arrival, ticketNode.arrival);
      synchronized (this) {
        rightNode.right = ticketNode.right;
        ticketNode.right.left = rightNode;
        ticketNode.right = rightNode;
        tryMergeRight(route, rightNode);
      }
      route.intervals[rightNode.departure-1][rightNode.arrival-1].push(rightNode);
    }
  }
  
  synchronized public TicketNode findTicketNode(long tid) {
    TicketNode ticketNode = head.right;
    while (ticketNode != head) {
      if (ticketNode.tid == tid)
        return ticketNode;
      ticketNode = ticketNode.right;
    }
    return null;
  }
  
  synchronized public void tryMergeTicketNode(Route route, TicketNode ticketNode) {
    // has been merged...
    if (ticketNode.free.get() == false) return;
    tryMergeLeft(route, ticketNode);
    tryMergeRight(route, ticketNode);
  }
  
}
