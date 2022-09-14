package ticketingsystem;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Random;

class Seat {

    private final int SeatId; //座位id
    private AtomicLong BitMap; //用位图保存该座位对应的车站信息

    public Seat(final int seatId) {
        this.SeatId = seatId;
        this.BitMap = new AtomicLong(0);
    }

    public void setBitMap(AtomicLong BitMap){
        this.BitMap = BitMap;
    }

    public AtomicLong getBitMap(){
        return this.BitMap;
    }

    //查票
    public int inquiry(final int departure, final int arrival) {
        long preBitMap = this.BitMap.get();
        long newBitMap = 0;
        long base;
        long i = departure-1;


        do{
            base = 1;
            base = base << i;
            newBitMap = newBitMap | base;
            i++;
        }while(i<arrival-2);
          

        long result = newBitMap & preBitMap;

        if(result==0){
            return 1;
        }else{
            return 0;
        }
    }

    //购票操作,先判断是否有重复区间,再进行位图合并
    public int buyTicket(final int departure, final int arrival) {
        long preBitMap = 0;
        long curBitMap = 0;
        long newBitMap = 0;
        long base;
        long i = departure-1;
        
        do{
            base = 1;
            base = base << i;
            newBitMap = newBitMap | base;
            i++;
        }while(i<arrival-2);
    

        do {
            preBitMap = this.BitMap.get();

            /*将newBitMap与旧位图进行与操作,如果不为0则说明两者至少有一位同时为1
              说明当前座位与本方法提供路线至少在一个车站是冲突的,此时返回-1
             */
            long result = newBitMap & preBitMap;
            if (result != 0) {
                return -1;
            } else {
                curBitMap = newBitMap | preBitMap; //更新位图
            }
        } while (!this.BitMap.compareAndSet(preBitMap, curBitMap));

        return this.SeatId;
    }



    //退票
    public boolean refundTicket(final int departure, final int arrival) {
        long preBitMap = 0; //旧位图
        long curBitMap = 0; //合并之后的位图
        long newBitMap = 0; //只含有和退票相关的车站信息的位图
        long base;
        long i = departure - 1;

        do{
            base = 1;
            base = base << i;
            newBitMap = newBitMap | base;
            i++;
        }while(i<arrival-2);

        newBitMap = ~newBitMap;

        do {
            preBitMap = this.BitMap.get();
            curBitMap = newBitMap & preBitMap;
        } while (!this.BitMap.compareAndSet(preBitMap, curBitMap));

        return true;
    }

}

class Coach {
    private final int CoachId; //车厢号
    private final int SeatNum; //每节车厢的座位数
    private ArrayList<Seat> SeatList; //座位列表

    public Coach(final int coachId, final int seatNum) {
        this.CoachId = coachId;
        this.SeatNum = seatNum;
        SeatList = new ArrayList<>(seatNum);

        for (int i = 1; i <= seatNum; i++) {
            this.SeatList.add(new Seat(i)); //初始化座位列表
        }
    }

    public Ticket buyTicket(final int departure, final int arrival) {
        Ticket ticket = new Ticket();

        //使用随机数提高查询速度
        int randomSeat = ThreadLocalRandom.current().nextInt(this.SeatNum);
        for (int i = 0; i < this.SeatNum; i++) {
            int inquiriedSeatId = this.SeatList.get(randomSeat).buyTicket(departure, arrival);
            if (inquiriedSeatId != -1) {
                ticket.coach = this.CoachId;
                ticket.seat = inquiriedSeatId;
                return ticket;
            }
            randomSeat = (randomSeat+1) % this.SeatNum;
        }
        return null;
    }

    public int inquiry(final int departure, final int arrival) {
        int result = 0;
        for (int i = 0; i < this.SeatNum; i++)
            result += this.SeatList.get(i).inquiry(departure, arrival);
        return result;
    }

    public boolean refundTicket(final int seatId, final int departure, final int arrival) {
        return this.SeatList.get(seatId-1).refundTicket(departure, arrival);
    }

}

class Route {
    private final int RouteId; //车次号
    private final int CoachNum; //车厢数
    private ArrayList<Coach> CoachList; //车厢列表
    private AtomicLong TicketId; //车票id
    private Queue<Long> SoldTicketQueue; //保存已售出的车票信息

    public Route(final int routeId, final int coachNum, final int seatNum) {
        this.RouteId = routeId;
        this.CoachNum = coachNum;
        this.CoachList = new ArrayList<>(coachNum);
        this.TicketId = new AtomicLong(0);
        this.SoldTicketQueue = new ConcurrentLinkedQueue<>();

        for (int i = 1; i <= coachNum; i++) {
            this.CoachList.add(new Coach(i, seatNum));
        }
    }

    public Ticket buyTicket(final String passenger, final int departure, final int arrival) {

        int randomCoach = ThreadLocalRandom.current().nextInt(this.CoachNum);
        for (int i = 0; i < this.CoachNum; i++) {
            Ticket ticket = this.CoachList.get(randomCoach).buyTicket(departure, arrival);
            if (ticket != null) {
                ticket.tid = this.RouteId*10000000 + this.TicketId.getAndIncrement();
                ticket.passenger = passenger;
                ticket.route = this.RouteId;
                ticket.departure = departure;
                ticket.arrival = arrival;

                //每张车票使用一个long型数据来保存基本信息
                long ticketInformation = 0;
                ticketInformation = ticketInformation | (ticket.tid << 32);
                ticketInformation = ticketInformation | (ticket.coach << 24);
                ticketInformation = ticketInformation | (ticket.seat << 12);
                ticketInformation = ticketInformation | (ticket.departure << 6);
                ticketInformation = ticketInformation | (ticket.arrival);
                this.SoldTicketQueue.add(new Long(ticketInformation));
                return ticket;
            }
            randomCoach = (randomCoach+1) % this.CoachNum;
        }
        return null;
    }

    public int inquiry(final int departure, final int arrival) {
        int result = 0;
        for (int i = 0; i < this.CoachNum; i++)
            result += this.CoachList.get(i).inquiry(departure, arrival);
        return result;
    }

    public boolean refundTicket(final Ticket ticket) {
        long ticketInformation = 0;
        ticketInformation = ticketInformation | (ticket.tid << 32);
        ticketInformation = ticketInformation | (ticket.coach << 24);
        ticketInformation = ticketInformation | (ticket.seat << 12);
        ticketInformation = ticketInformation | (ticket.departure << 6);
        ticketInformation = ticketInformation | (ticket.arrival);
        if (!this.SoldTicketQueue.contains(ticketInformation))
            return false;
        else {
            this.SoldTicketQueue.remove(ticketInformation);
            return this.CoachList.get(ticket.coach-1).refundTicket(ticket.seat, ticket.departure, ticket.arrival);
        }
    }

}

public class TicketingDS implements TicketingSystem {
    private final int RouteNum;
    private final int StationNum;
    private ArrayList<Route> routeList;

    public TicketingDS(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
        this.RouteNum = routeNum;
        this.StationNum = stationNum;

        this.routeList = new ArrayList<Route>(routeNum);
        for (int routeId = 1; routeId <= routeNum; routeId++)
            this.routeList.add(new Route(routeId, coachNum, seatNum));
    }


    public int inquiry(int route, int departure, int arrival) {
        if (route <=0 || route > this.RouteNum || arrival > this.StationNum || departure >= arrival)
            return -1;
        return this.routeList.get(route-1).inquiry(departure, arrival);
    }
    
    public boolean refundTicket(Ticket ticket) {
        final int routeId = ticket.route;
        if (ticket == null || routeId <=0 || routeId > this.RouteNum)
            return false;
        return this.routeList.get(routeId-1).refundTicket(ticket);
    }

    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        if (route <=0 || route > this.RouteNum || arrival > this.StationNum || departure >= arrival)
            return null;
        return this.routeList.get(route-1).buyTicket(passenger, departure, arrival);
    }
   

}
