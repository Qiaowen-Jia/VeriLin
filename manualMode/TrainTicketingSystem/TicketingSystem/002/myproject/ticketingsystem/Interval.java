package ticketingsystem;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;

// A stack of TicketNode
public class Interval {
  
  private AtomicInteger size = new AtomicInteger(0);
  AtomicReference<TicketNode> top = new AtomicReference<TicketNode>(null);
  
  public Interval() {}
  
  public void incSize() { size.getAndIncrement(); }
  public void decSize() { size.getAndDecrement(); }
  
  public boolean tryPush(TicketNode node) {
    TicketNode oldTop = top.get();
    node.next = oldTop;
    return top.compareAndSet(oldTop, node);
  }
  
  public TicketNode tryPop() {
    TicketNode oldTop = top.get();
    if (oldTop == null)
      return null;
    TicketNode newTop = oldTop.next;
    if (top.compareAndSet(oldTop, newTop))
      return oldTop;
    return null;
  }
  
  public void push(TicketNode value) {
    incSize();
    while (true) {
      if (tryPush(value)) break;
    }
  }
  
  public void pushWithoutExchange(TicketNode value) {
    incSize();
    while (!tryPush(value))
      continue;
  }
  
  public TicketNode pop() {
    while (top.get() != null) {
      TicketNode resNode = tryPop();
      if (resNode != null && resNode.isUsable()) {
        decSize();
        return resNode;
      }
    }
    return null;
  }
  
  public int getNumOfFreeTickets() {
    int localNum = size.get();
    return (localNum < 0) ? 0 : localNum;
  }
}
