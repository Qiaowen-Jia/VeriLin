package ticketingsystem;

import java.util.concurrent.atomic.AtomicLong;

public class TicketIdArranger {
  private static  AtomicLong tnum = new AtomicLong(0l);

  public static Long arrangeTid () {
    return tnum.getAndIncrement();
  }
  
}
