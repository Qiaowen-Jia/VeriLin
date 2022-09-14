package ticketingsystem;

public interface TicketDepository {
    void add(Ticket ticket);

    boolean remove(Ticket ticket);
}
