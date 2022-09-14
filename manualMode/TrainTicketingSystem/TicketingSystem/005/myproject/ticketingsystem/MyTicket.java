package ticketingsystem;

public class MyTicket extends Ticket{
     public MyTicket(long tid, String passenger, int route, int coach, int seat, int departure, int arrival ) {
    	this.tid = tid;//车票编号
    	this.passenger = passenger;//乘客名字
    	this.route = route;//列车车次
    	this.coach = coach;//车厢号
    	this.seat = seat;//座位号
    	this.departure = departure;//出发站编号
    	this.arrival = arrival ;//到达站编号
     }
  
     @Override
     public boolean equals(Object obj) {
    	 //System.out.println("dccdcd" + " " + obj.getClass() + " " + (obj instanceof Ticket));
         if (obj != null && obj instanceof Ticket) {
        	 Ticket ticket = (Ticket) obj;
        	 //System.out.println(this.toString() + " *** " + ticket.toString() + (tid == ticket.tid)  + " " + (passenger.equals(ticket.passenger))+ " " +" " );
             if (tid == ticket.tid && passenger.equals(ticket.passenger) &&  route == ticket.route 
            		 && coach == ticket.coach && seat == ticket.seat && departure == ticket.departure && arrival == ticket.arrival) {
            //	 System.out.println(this.toString() + " *** " + ticket.toString() + (tid == ticket.tid)  + " " + (passenger.equals(ticket.passenger))+ " " +" " );
                 
                 return true;
             }
         }
         return false;
    }
   
     public long getTid() {
    	 return tid; 
     }
}
