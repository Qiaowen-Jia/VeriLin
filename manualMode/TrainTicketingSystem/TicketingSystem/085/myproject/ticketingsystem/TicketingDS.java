package ticketingsystem;

import java.util.concurrent.ConcurrentHashMap;

public class TicketingDS implements TicketingSystem {
    public int routenum;//routenum是车次总数（缺省为5个）
    public int coachnum;//coachnum是列车的车厢数目（缺省为8个）
    public int seatnum; //seatnum是每节车厢的座位数（缺省为100个）
    public int stationnum;//stationnum 是每个车次经停站的数量（缺省为10个，含始发站和终点站）
    public int threadnum;//threadnum 是并发购票的线程数（缺省为16个）
    //车次
    public RouteDS[] Route;

    //存储已经购票的tid值
    public static ConcurrentHashMap<Long, Boolean> hasAllot;

    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
        this.routenum = (routenum == 0) ? 5 : routenum;
        this.coachnum = (coachnum == 0) ? 8 : coachnum;
        this.seatnum = (seatnum == 0) ? 100 : seatnum;
        this.stationnum = (stationnum == 0) ? 10 : stationnum;
        this.threadnum = (threadnum == 0) ? 16 : threadnum;
        this.Route = new RouteDS[routenum + 1];
        for (int i = 1; i <= routenum; i++) {
            this.Route[i] = new RouteDS(this.routenum,this.coachnum, this.seatnum, this.stationnum, i);
        }
        hasAllot = new ConcurrentHashMap<>(5000000);
    }

    public Ticket buyTicket(String passenger, int routenum, int departure, int arrival) {
        if (!is_Request(routenum, departure, arrival)) return null;
        int[] route_seate = Route[routenum].buyTicket(departure, arrival);
        if (route_seate != null) {
            Ticket ticket = new Ticket();
            ticket.tid = route_seate[0];
            ticket.coach = route_seate[1];
            ticket.seat = route_seate[2];
            ticket.passenger = passenger;
            ticket.route = routenum;
            ticket.departure = departure;
            ticket.arrival = arrival;
            hasAllot.put(ticket.tid, true);
            return ticket;
        }
        return null;
    }//购票方法即乘客passenger购买route车次从departure站到arrival站的车票1张。若购票成功，返回有效的Ticket对象；若失败（即无余票），返回无效的Ticket对象（即return null）

    public int inquiry(int rout, int departure, int arrival) {
        if (!is_Request(rout, departure, arrival)) return -1;
        return Route[rout].inquiry(departure, arrival);
    }//查询余票方法，即查询route车次从departure站到arrival站余票数
    public boolean refundTicket(Ticket ticket) {
        if (!is_Request(ticket.route, ticket.departure, ticket.arrival)) return false;
        if (hasAllot.remove(ticket.tid) != null) {
            return Route[ticket.route].refund(ticket, ticket.departure, ticket.arrival);
        }
        return false;
    }//退票方法，对有效的Ticket对象返回true，对错误或无效的Ticket对象返回false

    private boolean is_Request(int route, int departure, int arrival) {
        if (route < 1 || route > this.routenum || departure < 1 || departure >= this.stationnum || arrival <= 1 || arrival > this.stationnum || arrival < departure) {
            return false;
        }
        return true;
    }
}
