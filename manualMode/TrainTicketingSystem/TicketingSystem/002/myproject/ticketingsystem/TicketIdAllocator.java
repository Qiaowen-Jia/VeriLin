package ticketingsystem;

import java.util.concurrent.atomic.AtomicInteger;

public class TicketIdAllocator {
  
  private static int threadNum;
  private static AtomicInteger nextThreadMyId = new AtomicInteger(0);
  
  public static void setThreadNum(int n) {
    threadNum = n;
  }
  
  public static long getTicketId() {
    long newTicketId = ticketId.get().longValue();
    ticketId.set(Long.valueOf(newTicketId + threadNum));
    return newTicketId;
  }
  
  private static ThreadLocal<Long> ticketId = new ThreadLocal<Long>() {
    @Override
    protected Long initialValue() {
      return Long.valueOf((long)(nextThreadMyId.getAndIncrement()));
    }
  };

}
