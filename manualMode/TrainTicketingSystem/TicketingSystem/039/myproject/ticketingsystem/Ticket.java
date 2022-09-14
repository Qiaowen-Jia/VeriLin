package ticketingsystem;

import java.util.*;

public class Ticket {
  long tid;
  String passenger;
  int route;
  int coach;
  int seat;
  int departure;
  int arrival;

  public Ticket() {
    
  }

  public Ticket(String passenger, int route, int coach, int seat, int departure, int arrival) {
    this.passenger = passenger;
    this.route = route;
    this.coach = coach;
    this.seat = seat;
    this.departure = departure;
    this.arrival = arrival;
  }

  public long setTid(long id) {
    tid = id;
    return id;
  }

  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null) 
      return false;
    if (getClass() != obj.getClass())
      return false;
    Ticket other = (Ticket) obj;

    if (passenger == null) {
      if (other.passenger != null)
        return false;
    } else if (!passenger.equals(other.passenger)) {
      return false;
    }

    if (tid != other.tid ||
        route != other.route ||
        coach != other.coach ||
        seat != other.seat ||
        departure != other.departure ||
        arrival != other.arrival)
      return false;
    
    return true;
  }


}
