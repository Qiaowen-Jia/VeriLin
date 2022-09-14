package ticketingsystem;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

class SeatNode {
  private final int seatId;
  private AtomicLong availableSeat;

  public SeatNode(final int seatId) {
    this.seatId = seatId;
    this.availableSeat = new AtomicLong(0);
  }

  public int buyticket(final int departure, final int arrival) {
    long oldSeatState = 0;
    long newSeatState = 0;
    long temp = 0;//欲购票区间
    //每个bit位代表一个区间，区间号从1开始
    for (int i = departure-1; i < arrival-1; i++) {
      long stationSection = 1;
      stationSection = stationSection << i;
      temp |= stationSection;
    } 
    do {
      oldSeatState = this.availableSeat.get();
      long isoverlap = temp & oldSeatState;//判断该座位在预购区间内是否可被分配
      if (isoverlap != 0) {
        return -1;
      } 
      else {
        newSeatState = temp | oldSeatState;
      }
    } while (!this.availableSeat.compareAndSet(oldSeatState, newSeatState));
    //使用CAS原语写入新值，遇到竞争时，非阻塞自旋转
    return this.seatId;
  }

  public int inquiryticket(final int departure, final int arrival) {
    long oldSeatState = this.availableSeat.get();
    long temp = 0;
    long stationSection;//查询车票的区间

    for (int i = departure-1; i < arrival-1; i++) {
      stationSection = 1;
      stationSection = stationSection << i;
      temp |= stationSection;
    } 
    long result = temp & oldSeatState;//查询区间与座位已购区间不交叉时，座位可以被分配
    return (result == 0) ? 1 : 0;
  }

  public boolean refundticket(final int departure, final int arrival) {
    long oldSeatState = 0;
    long newSeatState = 0;
    long temp = 0;

    for (int i = departure-1; i < arrival-1; i++) {
      long stationSection = 1;
      stationSection = stationSection << i;
      temp |= stationSection;
    } 
    temp = ~temp;//退票后，更改座位状态
    do {
      oldSeatState = this.availableSeat.get();
      newSeatState = temp & oldSeatState;
    } while (!this.availableSeat.compareAndSet(oldSeatState, newSeatState));
    //使用CAS原语更改座位状态，遇竞争时，非阻塞自旋
    return true;//成功退票
  }
}

class CoachNode {
  private final int coachId;
  private final int seatNum;
  private ArrayList<SeatNode> seatList;

  public CoachNode(final int coachId, final int seatNum) {
    this.coachId = coachId;
    this.seatNum = seatNum;
    seatList = new ArrayList<SeatNode>(seatNum);

    for (int seatId = 1; seatId <= seatNum; seatId++)
      this.seatList.add(new SeatNode(seatId));
  }

  public Ticket buyticket(final int departure, final int arrival) {
    Ticket ticket = new Ticket();
    //座位号起点随机分配
    int randSeat = ThreadLocalRandom.current().nextInt(this.seatNum);
    //按序遍历所有seat
    for (int i = 0; i < this.seatNum; i++) {
      int returnSeatId = this.seatList.get(randSeat).buyticket(departure, arrival);
      if (returnSeatId != -1) {
        ticket.coach = this.coachId;
        ticket.seat = returnSeatId;
        return ticket;
      } 
      randSeat = (randSeat+1) % this.seatNum;
    } 
    return null;
  }

  public int inquiryticket(final int departure, final int arrival) {
    int availSeatNum = 0;
    for (int i = 0; i < this.seatNum; i++)//遍历查询可够座位数
      availSeatNum += this.seatList.get(i).inquiryticket(departure, arrival);
    return availSeatNum;
  }

  public boolean refundticket(final int seatId, final int departure, final int arrival) {
    return this.seatList.get(seatId-1).refundticket(departure, arrival);
  }

}

class RouteNode {
  private final int routeId;
  private final int coachNum;
  private ArrayList<CoachNode> coachList;
  private AtomicLong ticketId;//分配唯一的tid
  private Queue<Long> queueOfSoldTicket;//

  public RouteNode(final int routeId, final int coachNum, final int seatNum) {
    this.routeId = routeId;
    this.coachNum = coachNum;
    this.coachList = new ArrayList<CoachNode>(coachNum);
    this.ticketId = new AtomicLong(0);
    this.queueOfSoldTicket = new ConcurrentLinkedQueue<Long>();

    for (int coachId = 1; coachId <= coachNum; coachId++)
      this.coachList.add(new CoachNode(coachId, seatNum));
  }

  public Ticket buyticket(final String passenger, final int departure, final int arrival) {
    //车厢号起点随机分配
    int randCoach = ThreadLocalRandom.current().nextInt(this.coachNum);
    //循环遍历车厢完成购票工作
    for (int i = 0; i < this.coachNum; i++) {
      Ticket ticket = this.coachList.get(randCoach).buyticket(departure, arrival);
      if (ticket != null) {
        ticket.tid = this.routeId*10000000 + this.ticketId.getAndIncrement();
        ticket.passenger = passenger;
        ticket.route = this.routeId;
        ticket.departure = departure;
        ticket.arrival = arrival;

        //为每张车票取hashcode，用于查票操作。
        long tic_hashCode = 0;
        tic_hashCode |= ticket.tid << 20;//负载测试，最多20个车次
        tic_hashCode |= ticket.coach << 15;//最多15节车厢
        tic_hashCode |= ticket.seat << 8;//最多100个座位
        tic_hashCode |= ticket.departure << 4;//最多10个站台
        tic_hashCode |= ticket.arrival;//最多10个站台
        this.queueOfSoldTicket.add(new Long(tic_hashCode));
        return ticket;
      } 
      randCoach = (randCoach+1) % this.coachNum;
    } 
    return null;
  }

  public int inquiryticket(final int departure, final int arrival) {
    int availSeatNum = 0;
    for (int i = 0; i < this.coachNum; i++) 
      availSeatNum += this.coachList.get(i).inquiryticket(departure, arrival);
    return availSeatNum;
  }

  public boolean refundticket(final Ticket ticket) {
    long tic_hashCode = 0;
    tic_hashCode |= ticket.tid << 20;
    tic_hashCode |= ticket.coach << 15;
    tic_hashCode |= ticket.seat << 8;
    tic_hashCode |= ticket.departure << 4;
    tic_hashCode |= ticket.arrival;
    if (!this.queueOfSoldTicket.contains(tic_hashCode)) 
      return false;
    else {
      this.queueOfSoldTicket.remove(tic_hashCode);
      return this.coachList.get(ticket.coach-1).refundticket(ticket.seat, ticket.departure, ticket.arrival);
    }
  }

}

public class TicketingDS implements TicketingSystem {
  private final int routeNum;
  private final int stationNum;
  private ArrayList<RouteNode> routeList;

  public TicketingDS(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
    this.routeNum = routeNum;
    this.stationNum = stationNum;

    this.routeList = new ArrayList<RouteNode>(routeNum);
    for (int routeId = 1; routeId <= routeNum; routeId++)//routeId从1开始
      this.routeList.add(new RouteNode(routeId, coachNum, seatNum));
  }

  public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
    //判断购票人购票信息合法性
    if (route <=0 || route > this.routeNum || departure <= 0||arrival > this.stationNum || departure >= arrival) 
      return null;
    return this.routeList.get(route-1).buyticket(passenger, departure, arrival);
  }

  public int inquiry(int route, int departure, int arrival) {
    if (route <=0 || route > this.routeNum || departure <= 0 ||arrival > this.stationNum || departure >= arrival) 
      return -1;
    return this.routeList.get(route-1).inquiryticket(departure, arrival);
  }

  public boolean refundTicket(Ticket ticket) {
    final int routeId = ticket.route;
    if (ticket == null || routeId <=0 || routeId > this.routeNum) 
      return false;
    return this.routeList.get(routeId-1).refundticket(ticket);
  }

}
