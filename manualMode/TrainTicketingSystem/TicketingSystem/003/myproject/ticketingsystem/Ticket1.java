package ticketingsystem;

public class Ticket1 extends Ticket {
    // int[] Mark = null;
    Ticket1(Ticket ticket, int[] Mark) {
        this.arrival = ticket.arrival;
        this.coach = ticket.coach;
        this.departure = ticket.departure;
        this.passenger = ticket.passenger;
        this.route = ticket.route;
        this.seat = ticket.seat;
        this.tid = ticket.tid;
        // this.Mark = Mark;
    }

    Ticket1(Ticket ticket) {
        this.arrival = ticket.arrival;
        this.coach = ticket.coach;
        this.departure = ticket.departure;
        this.passenger = ticket.passenger;
        this.route = ticket.route;
        this.seat = ticket.seat;
        this.tid = ticket.tid;
    }

    Ticket1() {
        this.arrival = 0;
        this.coach = 0;
        this.departure = 0;
        this.passenger = null;
        this.route = 0;
        this.seat = 0;
        this.tid = -1;
    }

    public boolean f_set(Ticket ticket) {
        this.arrival = ticket.arrival;
        this.coach = ticket.coach;
        this.departure = ticket.departure;
        this.passenger = ticket.passenger;
        this.route = ticket.route;
        this.seat = ticket.seat;
        this.tid = ticket.tid;
        return true;
    }
}
