package ticketingsystem;



import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author:毛翔宇
 * @Date 2019-12-17 20:40
 */
class Seat{
    int seatId;
    AtomicLong availableSeats;
    public Seat(int id, int initial){
        this.seatId = id;
        this.availableSeats = new AtomicLong(initial);
    }
    public int tryAcquireTicket(int departure, int arrival){
        long oldAvailableSeats;
        long tmp = 0;
        long newAvailableSeats = 0;
        for (int i = departure-1; i<arrival;i++){
            long move = 1;
            move =(move<< i);
            tmp |= move;
        }
        /*
        假设多个进程同时访问一个座位，avalableSeats涵盖了座位始发站和目的地的信息
        只要进程间对该域位信息不重叠，则该坐有效，可购票
        注意对该信息的并发修改
         */
        do {
            oldAvailableSeats = availableSeats.get();//linearization point
            if ((tmp & oldAvailableSeats) != 0)
                return -1;
            else
                newAvailableSeats = oldAvailableSeats | tmp;
        } while (!availableSeats.compareAndSet(oldAvailableSeats, newAvailableSeats));
        return seatId;//返回seatId
    }
    public int inquiryTickets(int departure, int arrival){
        long temp = 0;
        long current;
        for (int i = departure-1; i< arrival; i++){
            long mov = 1;
            mov = mov<<i;
            temp |= mov;
        }
        /*
        冲突访问点：当前查询线程同购票线程
        1、查询线程发生在购票线程之前，则得到票数比实际多1
        2、查询线程在购票后，得到正确结果
        冲突访问点：当前查询线程同退票线程
        1、查询线程发生在退票线程之前，则得到票数比实际少1
        2、查询线程在退票后，得到正确结果
        TODO：是否采用读写锁控制线程间结果一致性？加时间戳？
        多个线程查询结果可能不一致

        若要保证一致性，则需要限制读写顺序，即规定满足何种一致性，此处为松弛一致性
         */
        current = availableSeats.get();
        return ((current & temp) == 0)?1:0;
    }
    /*
    从departure->arrival站的票退票，正确逻辑是
    修改该座位仅对应departure->arrival里程的位图信息，
    可能出现的并发访问冲突：
    1、不同里程同一座位的退票，保证availableSeats修改最终结果一致 --循环CAS
    2、对同一座位同一里程购票、退票 可线性化点 availableSeats.set(80行)和availableSeats.get(35行),如果前者先发生，则可购得刚退得票；如果后者
    先发生，则购票失败，购票线程cacheNode.tryAcquireTicket循环遍历下一个座位位置，继续尝试其他座位。
    3、对同一座位不同里程的购票、退票，get与set操作可并发执行，无先后顺序之分。
     */
    public boolean tryRefundTicket(int departure,  int arrival){
        long temp = 0;
        long old;
        long newAvailable;
        for (int i = departure-1; i< arrival; i++){
            long mov = 1;
            mov = mov<<i;
            temp |= mov;
        }
        temp = ~temp;
        do {
            old = availableSeats.get(); //每个线程获取对应值
            newAvailable = temp | old;

        }while (!availableSeats.compareAndSet(old,newAvailable));//linearization point
        return true;
    }
}
class CoachNode{
    int coachId;
    int seatNum; //每节车厢座位总数
    ArrayList<Seat> seats; //座位 不同线程访问同一座位的概率较小
    public CoachNode(int coachId, int seatNum){
        this.coachId = coachId;
        this.seatNum = seatNum;
        seats = new ArrayList<>(seatNum);
        for (int i = 1; i<=seatNum; i++)
            seats.add(new Seat(i,0));
    }
    /*
    一个车厢具有多个座位，为降低不同线程访问同一座位的概率，每个线程拥有线程内部座位索引值
    即采用ThreadLocalRandom
     */
    public Ticket tryAcquireTicket(int departure, int arrival){
        Ticket ticket = new Ticket();
        int randomPick = ThreadLocalRandom.current().nextInt(seatNum);
        for (int i = 0;i<seatNum;i++){
            //int randomPick = ThreadLocalRandom.current().nextInt(seatNum);
            /*
            该处可优化成wait-free即如果
             */

            int ticId = seats.get(randomPick).tryAcquireTicket(departure,arrival);
            if (ticId != -1){
                ticket.coach = coachId;
                ticket.seat = ticId;
                return ticket;
            }
            randomPick = (randomPick+1) % seatNum;
        }
        return null;
    }
    public int inquiryTicket(int departure, int arrival){
        int res = 0;
        for (int i =0;i<seats.size();i++){
           res +=seats.get(i).inquiryTickets(departure,arrival);
        }
        return res;
    }
    public boolean tryRefundTicket(Ticket ticket){
        int seat = ticket.seat;
        int dep = ticket.departure;
        int des = ticket.arrival;
        return seats.get(seat-1).tryRefundTicket(dep,des);
    }

}
class RouteNode{
    int routeId;
    int coachNum;
    AtomicLong ticketId; //保证每张票:unique ID
    ArrayList<CoachNode> carriages;
    //ConcurrentLinkedQueue<Long> sold_tickets;
    ConcurrentSkipListSet<Long> sold_tickets;

    public RouteNode(int routeId,int coachNum,int seatNum){
        ticketId = new AtomicLong(0);
        this.routeId = routeId;
        this.coachNum = coachNum;
        this.sold_tickets = new ConcurrentSkipListSet<>();
        this.carriages = new ArrayList<>(coachNum);
        for (int i =1;i<=coachNum;i++){
            carriages.add(new CoachNode(i,seatNum));
        }
    }
    //购票操作
    public Ticket tryAcquireTicket(String passenger, int departure, int arrival){
        int randomPick = ThreadLocalRandom.current().nextInt(coachNum);
        for (int i = 0;i<coachNum;i++){

            //int randomPick = ThreadLocalRandom.current().nextInt(coachNum);
            Ticket ticket = carriages.get(randomPick).tryAcquireTicket(departure,arrival);
            /*
            线程获取到该车次、特定里程的车票，将该票插入进并发队列中
            为提升查找效率，将每张票对应的hashcode作为关键字存入sold_tickets中
            //todo：若采用set存储，则需要保证key不重复
            ticket 的hashcode包含该ticket所有字段的值（route,coach,seat,departure,arrival）
             */
            if (ticket != null){
                ticket.tid = this.routeId*1000000 + this.ticketId.getAndIncrement();
                ticket.route = routeId;
                ticket.passenger = passenger;
                ticket.departure = departure;
                ticket.arrival = arrival;

                long key = 0;
                key |= ticket.tid<<32;
                key |= ticket.coach<<24;
                key |= ticket.seat<<12;
                key |= ticket.departure <<6;
                key |= ticket.arrival;

                sold_tickets.add(key);
                return ticket;
            }
            randomPick = (randomPick+1) % coachNum;
        }
        return null;
    }
    //查询剩余座位数目
    public int inquiryTickets(int departure, int arrival){
        int res = 0;
        for (int i = 0; i< carriages.size();i++){
            res +=carriages.get(i).inquiryTicket(departure,arrival);
        }
        return res;
    }
    //尝试退票
    public boolean tryRefundTicket(Ticket ticket){
        //得到ticket的hashcode
        long key = 0;
        key |= ticket.tid<<32;
        key |= ticket.coach<<24;
        key |= ticket.seat<<12;
        key |= ticket.departure <<6;
        key |= ticket.arrival;
        /*
        根据hashcode查询集合
         */
        if (!sold_tickets.contains(key)) //已购票队列不含该票，则票无效
            return false;
        else{
            sold_tickets.remove(key);
            return carriages.get(ticket.coach-1).tryRefundTicket(ticket);
        }
    }
}


public class TicketingDS implements TicketingSystem{
    int routeNum;
    int stationNum;
    ArrayList<RouteNode> routes;

    public TicketingDS(int routeNum,int coachNum,int seatNum,int stationNum,int threadNum){
        this.routeNum = routeNum;
        this.stationNum = stationNum;
        routes = new ArrayList<>(routeNum);
        for (int i =1;i<=routeNum;i++){
            routes.add(new RouteNode(i,routeNum,seatNum));
        }
    }

    public Ticket buyTicket(String passenger, int route, int departure, int arrival){

        if (route > routeNum || arrival >stationNum)
            return null;

        return routes.get(route-1).tryAcquireTicket(passenger,departure,arrival);
    }

    public int inquiry(int route, int departure, int arrival){
        if (route <1 || route> routeNum || arrival > stationNum ||departure >= arrival)
            return -1;
        return routes.get(route-1).inquiryTickets(departure,arrival);
    }

    public boolean refundTicket(Ticket ticket){
        int id = ticket.route;
        if (id <=0 || id >routeNum || ticket == null)
            return false;
        return routes.get(id-1).tryRefundTicket(ticket);

    }
}
