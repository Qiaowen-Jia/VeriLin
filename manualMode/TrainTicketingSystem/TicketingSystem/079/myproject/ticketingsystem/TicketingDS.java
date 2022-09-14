package ticketingsystem;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class TicketingDS implements TicketingSystem {
  private final int routeNum;//车次数
  private final int stationNum;//车站数
  private ArrayList<RouteNode> routeList;//动态数组

  public TicketingDS(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
    this.routeNum = routeNum;
    this.stationNum = stationNum;

    this.routeList = new ArrayList<RouteNode>(routeNum);
    for (int routeId = 1; routeId <= routeNum; routeId++)//routeId从1开始
      this.routeList.add(new RouteNode(routeId, coachNum, seatNum));
  }

  public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
    if (route <=0 || route > this.routeNum || arrival > this.stationNum || departure >= arrival) 
      return null;
    return this.routeList.get(route-1).trySealTic(passenger, departure, arrival);
  }

  public int inquiry(int route, int departure, int arrival) {
    if (route <=0 || route > this.routeNum || arrival > this.stationNum || departure >= arrival) 
      return -1;
    return this.routeList.get(route-1).inquiryTic(departure, arrival);
  }

  public boolean refundTicket(Ticket ticket) {
    final int routeId = ticket.route;
    if (ticket == null || routeId <=0 || routeId > this.routeNum) 
      return false;
    return this.routeList.get(routeId-1).tryRefundTic(ticket);
  }

}
//座位节点
class SeatNode {
  private final int seatId;//座位号
  private AtomicLong availableSeat;//原子可用座位

  //构造函数
  public SeatNode(final int seatId) {
    this.seatId = seatId;
    this.availableSeat = new AtomicLong(0);//init 0
  }

  //尝试卖票--购票
  public int trySealTic(final int departure, final int arrival) {
    long oldAvailSeat = 0;
    long newAvailSeat = 0;
    long temp = 0;

    //从 出发站 到 到达站
    for (int i = departure-1; i < arrival-1; i++) {
      long pow = 1;
      pow = pow << i;//pow * 2^i
      temp |= pow;  //有1为1  0011110 2-5站
    } 

    do {
      oldAvailSeat = this.availableSeat.get();//读
      long result = temp & oldAvailSeat;//有0为0
      if (result != 0) {
        return -1;
      } 
      else {
        newAvailSeat = temp | oldAvailSeat;
      }
    } while (!this.availableSeat.compareAndSet(oldAvailSeat, newAvailSeat));//cas试图写

    return this.seatId;

  }

  //查询余票
  public int inquiryTic(final int departure, final int arrival) {
    long oldAvailSeat = this.availableSeat.get();//读
    long temp = 0;
    long pow;

    for (int i = departure-1; i < arrival-1; i++) {
      pow = 1;
      pow = pow << i;
      temp |= pow;//0011110
    } 
    long result = temp & oldAvailSeat;//有0为0

    return (result == 0) ? 1 : 0;

  }

  //退票
  public boolean tryRefundTic(final int departure, final int arrival) {
    long oldAvailSeat = 0;
    long newAvailSeat = 0;
    long temp = 0;

    for (int i = departure-1; i < arrival-1; i++) {
      long pow = 1;
      pow = pow << i;
      temp |= pow;
    } 
    temp = ~temp;//11000001
    do {
      oldAvailSeat = this.availableSeat.get();//读
      newAvailSeat = temp & oldAvailSeat;//result
    } while (!this.availableSeat.compareAndSet(oldAvailSeat, newAvailSeat));//CAS做

    return true;
  }

}

//车厢节点
class CoachNode {
  private final int coachId;
  private final int seatNum;
  private ArrayList<SeatNode> seatList;

  public CoachNode(final int coachId, final int seatNum) {
    this.coachId = coachId;
    this.seatNum = seatNum;
    seatList = new ArrayList<SeatNode>(seatNum);//可调整大小的数组实现

    for (int seatId = 1; seatId <= seatNum; seatId++)
      this.seatList.add(new SeatNode(seatId));//seatList 中加入 seatNum个seatNode节点 数组
  }

  //尝试卖票
  public Ticket trySealTic(final int departure, final int arrival) {
    Ticket ticket = new Ticket();
    //遍历所有seat
    int randSeat = ThreadLocalRandom.current().nextInt(this.seatNum);//多线程不同值randSeat
    for (int i = 0; i < this.seatNum; i++) {
      int resultSeatId = this.seatList.get(randSeat).trySealTic(departure, arrival);//任意一个座位 进行销售
      if (resultSeatId != -1) {//成功卖出
        ticket.coach = this.coachId;//票的车厢座位进行设置
        ticket.seat = resultSeatId;//seatId
        return ticket;//卖出 返回对象ticket
      } 
      randSeat = (randSeat+1) % this.seatNum;//下一个位置
    } 
    return null;//未卖出 返回null

  }

  //查询余票
  public int inquiryTic(final int departure, final int arrival) {
    int ticSum = 0;
    for (int i = 0; i < this.seatNum; i++)//车厢内遍历所有座位
      ticSum += this.seatList.get(i).inquiryTic(departure, arrival);//每个座位查询两站间余票
    return ticSum;
  }

  //退票
  public boolean tryRefundTic(final int seatId, final int departure, final int arrival) {
    return this.seatList.get(seatId-1).tryRefundTic(departure, arrival);//退票
  }

}

//车次节点
class RouteNode {
  private final int routeId;              //车次编号
  private final int coachNum;             //车厢数
  private ArrayList<CoachNode> coachList; //车厢数组
  private AtomicLong ticketId;            //票id
  private Queue<Long> queue_SoldTicket;   //队列售票

  public RouteNode(final int routeId, final int coachNum, final int seatNum) {
    this.routeId = routeId;
    this.coachNum = coachNum;
    this.coachList = new ArrayList<CoachNode>(coachNum);
    this.ticketId = new AtomicLong(0);//0
    this.queue_SoldTicket = new ConcurrentLinkedQueue<Long>();//FIFO链接节点的无界线程安全queue 

    for (int coachId = 1; coachId <= coachNum; coachId++)
      this.coachList.add(new CoachNode(coachId, seatNum));//数组中加入车厢节点
  }

  //尝试卖票
  public Ticket trySealTic(final String passenger, final int departure, final int arrival) {
    //遍历所有coach
    int randCoach = ThreadLocalRandom.current().nextInt(this.coachNum);//线程安全 随机数
    for (int i = 0; i < this.coachNum; i++) {
      Ticket ticket = this.coachList.get(randCoach).trySealTic(departure, arrival);//随机车厢的 尝试卖票
      if (ticket != null) {
        ticket.tid = this.routeId*10000000 + this.ticketId.getAndIncrement();//？k0000000+i  分配具体车票
        ticket.passenger = passenger;
        ticket.route = this.routeId;
        ticket.departure = departure;
        ticket.arrival = arrival;

        //每张车票hashCode
        long tic_hashCode = 0;
        tic_hashCode |= ticket.tid << 32;       //32位为1 低位tid有1为1
        tic_hashCode |= ticket.coach << 24;
        tic_hashCode |= ticket.seat << 12;
        tic_hashCode |= ticket.departure << 6;
        tic_hashCode |= ticket.arrival;
        //ymx
        //this.queue_SoldTicket.add(new Long(tic_hashCode));
        this.queue_SoldTicket.add(tic_hashCode);

        return ticket;//返回得到的ticket

      } 

      randCoach = (randCoach+1) % this.coachNum;//ticket为null 则查找下一个车厢
    } 

    return null;//返回null
  }

  //查询余票
  public int inquiryTic(final int departure, final int arrival) {
    int ticSum = 0;
    for (int i = 0; i < this.coachNum; i++) 
      ticSum += this.coachList.get(i).inquiryTic(departure, arrival);//遍历车厢 加和
    return ticSum;
  }

  //尝试退票
  public boolean tryRefundTic(final Ticket ticket) {
    long tic_hashCode = 0;
    tic_hashCode |= ticket.tid << 32;
    tic_hashCode |= ticket.coach << 24;
    tic_hashCode |= ticket.seat << 12;
    tic_hashCode |= ticket.departure << 6;
    tic_hashCode |= ticket.arrival;
    if (!this.queue_SoldTicket.contains(tic_hashCode)) //无票可退
      return false;
    else {
      this.queue_SoldTicket.remove(tic_hashCode);//队列中删除票
      return this.coachList.get(ticket.coach-1).tryRefundTic(ticket.seat, ticket.departure, ticket.arrival);//车厢中删除票
    }
  }

}

