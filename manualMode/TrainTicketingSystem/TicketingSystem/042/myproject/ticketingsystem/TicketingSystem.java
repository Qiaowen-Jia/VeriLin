package ticketingsystem;
import java.lang.String;
//
class Ticket
{
	//>=1
	long tid;
	String passenger;
	//1-N
	int route;
	//1-N
	int coach;
	//1-N
	int seat; 
	//1-N-1
	int departure;
	//2-N
	int arrival;
}

public interface TicketingSystem
{
	Ticket buyTicket(String passenger,int route,int departure,int arrival);
	int inquiry(int route,int departure,int arrival);
	boolean refundTicket(Ticket ticket);
}
