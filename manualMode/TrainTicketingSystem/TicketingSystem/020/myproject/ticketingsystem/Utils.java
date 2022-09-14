package ticketingsystem;

public class Utils {
    static boolean ticketEquals(Ticket a, Ticket b) {
        return a.tid == b.tid
                && a.passenger.equals(b.passenger)
                && a.route == b.route
                && a.coach == b.coach
                && a.seat == b.seat
                && a.departure == b.departure
                && a.arrival == b.arrival;
    }
}
