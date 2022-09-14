package ticketingsystem;

public interface TicketingSystem {
    //买票
    Ticket buyTicket(String passenger, int route, int departure, int arrival);
    //查询余票
    int inquiry(int route, int departure, int arrival);
    //退票
    boolean refundTicket(Ticket ticket);
}
