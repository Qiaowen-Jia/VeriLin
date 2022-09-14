package ticketingsystem;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.*;

public class TicketingDS implements TicketingSystem {
  private int routeNum;
  private int coachNum;
  private int seatNum;
  private int stationNum;
  private int seatTotal; // the number of seats in each route

  private static SeatMap[] map; // bitmap mainly for a quick inquiry
  private static SeatLock[][][] seats;

  private int randThres; // TODO: optimize the parameter
  public static final boolean debug = false;
  public static final boolean fairReadWrite = true;

  private AtomicLong tid = new AtomicLong(0);
  private ConcurrentHashMap<Long, Ticket> soldTickets;

  public static HashMap<Long, Integer> highestBit = new HashMap<Long, Integer>(Long.SIZE);
  
  public TicketingDS(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
    this.routeNum = routeNum;
    this.coachNum = coachNum;
    this.seatNum = seatNum;
    this.stationNum = stationNum;
    this.seatTotal = coachNum * seatNum;
    // randThres = seatTotal / threadNum / 64;
    randThres = 0;
    soldTickets = new ConcurrentHashMap<Long, Ticket>(1024, (float)0.75, threadNum);
    map = new SeatMap[routeNum];
    for (int r  = 0; r < routeNum; r++)
      map[r] = new SeatMap(coachNum, seatNum, stationNum);
    seats = new SeatLock[routeNum][coachNum][seatNum];
    for (int r = 0; r < routeNum; r++) {
      seats[r] = new SeatLock[coachNum][seatNum];
      for (int c = 0; c < coachNum; c++) {
        seats[r][c] = new SeatLock[seatNum];
        for (int s = 0; s < seatNum; s++)
          seats[r][c][s] = new SeatLock(stationNum, fairReadWrite);
      }
    }

    // 000...001 -> 0
    // 000...011 -> 1
    // 111...111 -> Long.SIZE-1
    for (int i = 0; i < Long.SIZE; i++)
      highestBit.put(((long)~0) >>> (Long.SIZE - 1 - i), i);
  }

  class Interval {
    int route;
    int begin;
    int end;
    public Interval(int r, int departure, int arrival) {
      route = r;
      begin = departure - 1;
      end = arrival - 2;
    }

    public boolean isLegal() { // TODO: is this necessary?
      return begin >= 0 && begin <= end && end < (stationNum - 1) && route >= 1 && route <= routeNum;
    }
  }

  public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
    int r = route - 1;
    boolean succeed = false;
    Interval intv = new Interval(route, departure, arrival);
    int begin = intv.begin;
    int end = intv.end;
    SeatInfo seatInfo = new SeatInfo();
    // 1. try random seat for randThres times
    for (int randTimes = 0; randTimes < randThres; randTimes++) {
      int c = (new Random()).nextInt(coachNum);
      int s = (new Random()).nextInt(seatNum);
      if (succeed = seats[r][c][s].lockAndSet(begin, end)) {
        seatInfo.coach = c + 1;
        seatInfo.seat = s + 1;
        break;
      }
    }

    // 2. if trying random seat fails, traverse the whole route from (c,s) in bitmap 'map'
    if (!succeed) {
      long[] avail = map[r].getAvailableBitMap(begin, end);
      for (int i = 0; i < avail.length; i++) {
        long l = ~avail[i]; // find 1 in l
        while (l != 0) {
          int off = highestBit.get(l ^ (l - 1));
          int n = i * Long.SIZE + off;
          int c = n / seatNum;
          int s = n - c * seatNum;
          try {
            if (succeed = seats[r][c][s].lockAndSet(begin, end)) {
              seatInfo.coach = c + 1;
              seatInfo.seat = s + 1;
              break;
            } else {
              l = l & (l - 1);
            }
          } catch (ArrayIndexOutOfBoundsException e) {
            System.out.printf("r=%d c=%d s=%d off=%d n=%d avail=%x\n", r, c, s, off, n, avail[i]);
            System.exit(0);
          }
        }

        if (succeed) break;
      }

    }

    // 3. return a ricket if succeed
    if (succeed) {
      map[r].setSeat(seatInfo.coach - 1, seatInfo.seat - 1, begin, end);
      Ticket ticket = new Ticket(passenger, route, seatInfo.coach, seatInfo.seat, departure, arrival);
      long thisTid = ticket.setTid(tid.getAndIncrement());
      synchronized (soldTickets) {
        soldTickets.put(thisTid, ticket);
      }
      return ticket;
    }
    return null;
  }

  public int inquiry(int route, int departure, int arrival) {
    Interval intv = new Interval(route, departure, arrival);
    return map[route - 1].findSeatNum(intv.begin, intv.end);
  }

  public boolean refundTicket(Ticket ticket) {
    boolean succeed = false;

    // 1. reomve ticket from soldTickets
    synchronized (soldTickets) {
      Ticket sold = soldTickets.get(ticket.tid);
      if (sold != null)
        if (sold.equals(ticket)) {
          soldTickets.remove(ticket.tid);
          succeed = true;
        }
    }
    
    // 2. remove seat from seats and bit map
    if (succeed) {
      int r = ticket.route - 1, c = ticket.coach - 1, s = ticket.seat - 1;
      Interval intv = new Interval(ticket.route, ticket.departure, ticket.arrival);

      SeatLock seat = seats[r][c][s];
      try {
        // refund cannot be interrupted between writing seats and clearing bitmap by buyTicket
        seat.writeLock().lock();
        seat.unset(intv.begin, intv.end);
        map[r].clearSeat(c, s, intv.begin, intv.end);
      } finally {
        seat.writeLock().unlock();
      }

      return true;
    }

    return false;
  }

}