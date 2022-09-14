package ticketingsystem;
//no need to change
class Ticket{
	long tid; /*车票编号*/
	String passenger; /*乘客姓名*/
	int route; /*列车车次*/
	int coach; /*车厢号*/
	int seat; /*座位号*/
	int departure; /*出发站编号*/
	int arrival; /*到达站编号*/
}


public interface TicketingSystem {
	/*
	* 购票方法，即乘客passenger购买route车次从departure站到arrival站的车票1张：
	* 若购票成功，返回有效的Ticket对象；
	* 若失败（即无余票），返回无效的Ticket对象（即return null）。
	* */
	Ticket buyTicket(String passenger, int route, int departure, int arrival);
	/*
	* 查询余票方法，即查询route车次从departure站到arrival站的余票数：
	* 返回从departure站到arrival站最小的余票数，可能为0
	* */
	int inquiry(int route, int departure, int arrival);
	/*
	* 退票方法：
	* 对有效的Ticket对象返回true；
	* 对错误或无效的Ticket对象返回false。
	* */
	boolean refundTicket(Ticket ticket);
}
