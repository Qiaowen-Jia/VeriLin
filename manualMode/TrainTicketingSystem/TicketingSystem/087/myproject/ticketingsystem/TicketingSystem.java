/*
*
* TicketingDS.java
* Guo Jianing
* 2018-Jan-7th
*
*/

package ticketingsystem;

class Ticket{
	long tid;
	String passenger;
	int route;
	int coach;
	int seat;
	int departure;
	int arrival;
    
    @Override
    public boolean equals(Object o) {
        
        // We will not use this "equals()" method to check
        // validity of tickets for performace reason.
        if (o == this) return true;
        if (!(o instanceof Ticket)) {
            return false;
        }
        
        Ticket ticket = (Ticket) o;
        return ticket.tid == this.tid &&
            ticket.passenger.equals(this.passenger) &&
            ticket.route == this.route &&
            ticket.coach == this.coach &&
            ticket.seat == this.seat &&
            ticket.departure == this.departure &&
            ticket.arrival == this.arrival;
    }
    
    @Override
    public int hashCode() {
        // We will use a special string
        // to create a hash code for this object.
        String allInfo = "" + this.tid
            + "" + this.passenger
            + "" + this.route
            + "" + this.coach
            + "" + this.seat
            + "" + this.departure
            + "" + this.arrival;
        return allInfo.hashCode();
    }
}


public interface TicketingSystem {
    
	Ticket buyTicket(String passenger, 
        int route, int departure, int arrival);
        
	int inquiry(int route, int departure, int arrival);
	boolean refundTicket(Ticket ticket);
}
