package ticketingsystem;

public class TicketDepositoryArray implements TicketDepository {
    private LockFreeList<Ticket>[][][] tickets;

    public TicketDepositoryArray(int routenum, int coachnum, int seatnum) {
        tickets = (LockFreeList<Ticket>[][][]) new LockFreeList[routenum][coachnum][seatnum];
    }

    @Override
    public void add(Ticket ticket) {
        tickets[ticket.route][ticket.coach][ticket.seat].add(ticket.tid, ticket);
    }

    @Override
    public boolean remove(Ticket ticket) {
        LockFreeList<Ticket> list = tickets[ticket.route][ticket.coach][ticket.seat];
        LockFreeList.Node<Ticket> node = list.find(ticket.tid).curr;
        if (node.item != null && Utils.ticketEquals(ticket, node.item)) {
            list.remove(ticket.tid);
            return true;
        }
        return false;
    }
}
