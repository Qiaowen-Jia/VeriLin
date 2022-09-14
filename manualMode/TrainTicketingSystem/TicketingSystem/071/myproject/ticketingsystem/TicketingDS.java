package ticketingsystem;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

class Ticket{
        long tid;
        String passenger;
        int route;
        int coach;
        int seat;
        int departure;
        int arrival;
}

class seatcard {
  private final int seatnum;
  private AtomicLong availableSeat;//可坐的位置用0，1来表示

  public seatcard(final int seatnum) {
    this.seatnum = seatnum;
    this.availableSeat = new AtomicLong(0);
  }

  public int trySealTic(final int startpos, final int finishpos) {
    long oldAvailSeat = 0;
    long newAvailSeat = 0;
    long temp = 0;

    for (int i = startpos-1; i < finishpos-1; i++) {
      long pow = 1;
      pow = pow << i;//经过的站点，用0-1二进制中的1表示
      temp |= pow;
    } 

    do {
      oldAvailSeat = this.availableSeat.get();//获得当前座位的可以被卖出站点节点
      long result = temp & oldAvailSeat;
      if (result != 0) {
        return -1;//如果这个座位不能卖，就返回-1，换个座位去进行判断
      } 
      else {
        newAvailSeat = temp | oldAvailSeat;
      }
    } while (!this.availableSeat.compareAndSet(oldAvailSeat, newAvailSeat));//这里使用cas原语，进行买票操作，如果验证过程中座位发生变化，就要重新验证能不能买

    return this.seatnum;//买票成果，返回这张票

  }

  public int inquiryTic(final int startpos, final int finishpos) {
    long oldAvailSeat = this.availableSeat.get();
    long temp = 0;
    long pow;

    for (int i = startpos-1; i < finishpos-1; i++) {
      pow = 1;
      pow = pow << i;
      temp |= pow;
    } 
    long result = temp & oldAvailSeat;

    return (result == 0) ? 1 : 0;//result=0意味着，当前的开始和结束节点之间尚未被占领，这个座位可以卖这个区间的票票。

  }

  public boolean tryRefundTic(final int departure, final int arrival) {//退票函数，退票成功返回true
    long oldAvailSeat = 0;
    long newAvailSeat = 0;//初始化两个标记座位占用区间的变量
    long temp = 0;
    
    
     try { for (int i = departure-1; i < arrival-1; i++) {
    long pow = 1;
    pow = pow << i;
    temp |= pow;
  } 
  temp = ~temp;
  do {
    oldAvailSeat = this.availableSeat.get();
    newAvailSeat = temp & oldAvailSeat;
  } while (!this.availableSeat.compareAndSet(oldAvailSeat, newAvailSeat));//释放了该区间范围的票，则返回值，被打断时重新赋值一次

  return true;
	
} catch(Exception e) { return false;//出现异常，说明退票失败
	
}
   
  }

}

class coachcard {
  private final int coachnum;
  private final int seatNum;
  private ArrayList<seatcard> seatList;
//一节车厢，有seatnum个seat，每个车厢有一个编号，有很多个位置（座位）
  public coachcard(final int coachId, final int seatNum) {
    this.coachnum = coachId;
    this.seatNum = seatNum;
    seatList = new ArrayList<seatcard>(seatNum);
//初始化座位数组，同时实例化。
    for (int seatId = 1; seatId <= seatNum; seatId++)
      this.seatList.add(new seatcard(seatId));
  }

  public Ticket trySealTic(final int departure, final int arrival) {
    Ticket ticket = new Ticket();
    //卖票，一个座位一个座位卖
    int randSeat = ThreadLocalRandom.current().nextInt(this.seatNum);//从随机的位置开始查票，相比顺序查找增加的效率
    for (int i = 0; i < this.seatNum; i++) {
      int resultSeatId = this.seatList.get(randSeat).trySealTic(departure, arrival);
      if (resultSeatId != -1) {
        ticket.coach = this.coachnum;
        ticket.seat = resultSeatId;
        return ticket;//判断当前座位的座位卖票成功的时候，就返回这一张票
      } 
      randSeat = (randSeat+1) % this.seatNum;
    } 
    return null;

  }

  public int inquiryTic(final int departure, final int arrival) {
    int ticSum = 0;
    for (int i = 0; i < this.seatNum; i++)
      ticSum += this.seatList.get(i).inquiryTic(departure, arrival);//把所有的位置都查一遍，可以卖就返回1，不可以卖就不算数
    return ticSum;
  }

  public boolean tryRefundTic(final int seatId, final int departure, final int arrival) {
    return this.seatList.get(seatId-1).tryRefundTic(departure, arrival);//根据座位号，去退对应座位号的站次座位信息。
  }

}

class routecard {
  private final int routenum;
  private final int coachNum;
  private ArrayList<coachcard> coachList;
  private AtomicLong ticketId;
  private Queue<Long> queue_SoldTicket;

  public routecard(final int routeId, final int coachNum, final int seatNum) {
    this.routenum = routeId;
    this.coachNum = coachNum;
    this.coachList = new ArrayList<coachcard>(coachNum);
    this.ticketId = new AtomicLong(0);
  

    for (int coachId = 1; coachId <= coachNum; coachId++)
      this.coachList.add(new coachcard(coachId, seatNum));//初始化每个车次的火车对应的车厢和座位
  }

  public Ticket trySealTic(final String passenger, final int startpos, final int finishpos) {
    //买票，根据乘客姓名，初始站点和终止站点进行买票
    int randCoach = ThreadLocalRandom.current().nextInt(this.coachNum);//引入随机数，对于一开始的买票操作有好处，有加速效果
    for (int i = 0; i < this.coachNum; i++) {
      Ticket ticket = this.coachList.get(randCoach).trySealTic(startpos, finishpos);
      
      //对某个车厢，进行买票操作
      if (ticket != null) {
        ticket.tid = this.routenum*10000000 + this.ticketId.getAndIncrement();
        ticket.passenger = passenger;
        ticket.route = this.routenum;
        ticket.departure = startpos;
        ticket.arrival = finishpos;

       //对卖出去的这张票进行信息的赋值，同时结束该方法，进行返回，不需要继续卖票了
        return ticket;

      } 

      randCoach = (randCoach+1) % this.coachNum;//如果某个车厢票卖完了，换下一个车厢找票卖，由于使用了随机数，可能会越界，需要求余数
    } 

    return null;
  }

  public int inquiryTic(final int startpos, final int finishpos) {
    int ticSum = 0;
    for (int i = 0; i < this.coachNum; i++) 
      ticSum += this.coachList.get(i).inquiryTic(startpos, finishpos);
    return ticSum;
  }

  public boolean tryRefundTic(final Ticket ticket) {
    

      return this.coachList.get(ticket.coach-1).tryRefundTic(ticket.seat, ticket.departure, ticket.arrival);
    
  }

}

//public class TicketingDS implements TicketingSystem {
public class TicketingDS {
  private final int routeNum;
  private final int stationNum;
  private ArrayList<routecard> routeList;
//初始化卖票系统
  public TicketingDS(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
    this.routeNum = routeNum;
    this.stationNum = stationNum;

    this.routeList = new ArrayList<routecard>(routeNum);
    for (int routeId = 1; routeId <= routeNum; routeId++)//初始化列车各个班次，加入本卖票系统的队列之中
      this.routeList.add(new routecard(routeId, coachNum, seatNum));
  }

  public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
    if (route <=0 || route > this.routeNum || arrival > this.stationNum || departure >= arrival) 
      return null;//对于非法的购票申请，直接返回null，无法购票
    return this.routeList.get(route-1).trySealTic(passenger, departure, arrival);//对于合法的购票申请，进行尝试购票，返回结果
  }

  public int inquiry(int route, int departure, int arrival) {
    if (route <=0 || route > this.routeNum || arrival > this.stationNum || departure >= arrival) 
      return -1;//对于非法的查票操作，不予理会
    return this.routeList.get(route-1).inquiryTic(departure, arrival);//对于合法的查询余票操作，返回正确的答案
  }

  public boolean refundTicket(Ticket ticket) {
    final int routeId = ticket.route;
    if (ticket == null || routeId <=0 || routeId > this.routeNum) 
      return false;//对于非法的退票申请，返回失败
    return this.routeList.get(routeId-1).tryRefundTic(ticket);//对于合法的退票申请，进行退票操作，返回退票结果
  }

}
