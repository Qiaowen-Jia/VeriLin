package ticketingsystem;
import ticketingsystem.*;

class Ticket{
    long tid;           //车票编号
    String passenger;   //乘客名字
    int route;          //列车车次
    int coach;          //车厢号
    int seat;           //座位号
    int departure;      //出发站编号
    int arrival;        //到达站编号
}


public interface TicketingSystem {
    Ticket buyTicket(String passenger, int route, int departure, int arrival);      //购票方法
    int inquiry(int route, int departure, int arrival);                             //查询余票方法
    boolean refundTicket(Ticket ticket);                                            //退票方法
}
