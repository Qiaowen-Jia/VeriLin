package ticketingsystem;

class Ticket{
	long tid; //车票编号
	String passenger;//乘客名字
	int route;//列车车次
	int coach;//车厢号
	int seat;//座位号
	int departure;//出发站编号
	int arrival;//到达站编号

	public Ticket(long tid, String passenger, int route, int coach, int seat, int departure, int arrival) {
		this.tid = tid;
		this.passenger = passenger;
		this.route = route;
		this.coach = coach;
		this.seat = seat;
		this.departure = departure;
		this.arrival = arrival;
	}
	public Ticket() {

	}
}


public interface TicketingSystem {
	Ticket buyTicket(String passenger, int route, int departure, int arrival);
	int inquiry(int route, int departure, int arrival);
	boolean refundTicket(Ticket ticket);
}
