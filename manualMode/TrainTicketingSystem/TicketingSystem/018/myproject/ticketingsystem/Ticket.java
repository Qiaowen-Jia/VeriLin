package ticketingsystem;

import java.util.concurrent.atomic.AtomicInteger;

public class Ticket {
    public static AtomicInteger count = new AtomicInteger(0);
    public String passenger;
    public int route;
    public int coach;
    public int seat;
    public int departure;
    public int arrival;
    public int tid = count.getAndIncrement();
    Ticket()
    {
        passenger = "nobody";
        route = 0;
        coach = 0;
        seat = 0;
        departure = 0;
        arrival = 0;
    }
    Ticket(String passenger, int route, int coach, int seat,
           int departure, int arrival)
    {
        this.route = route;
        this.coach = coach;
        this.seat = seat;
        this.departure = departure;
        this.arrival = arrival;
        this.passenger = passenger;
    }

    @Override
    public String toString() {
        return tid+" "+passenger+" "+route+" "+coach+" "
                +seat+" "+departure+" "+arrival;
    }
}
