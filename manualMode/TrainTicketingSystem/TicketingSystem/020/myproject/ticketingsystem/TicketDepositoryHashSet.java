package ticketingsystem;

public class TicketDepositoryHashSet implements TicketDepository {
    private LockFreeHashSet<Ticket> set;

    TicketDepositoryHashSet(int routeNum, int coachNum, int seatNum) {
        set = new LockFreeHashSet<>(routeNum * coachNum * seatNum);
    }

    @Override
    public void add(Ticket ticket) {
        set.addWithKey(ticket.tid, ticket);
    }

    @Override
    public boolean remove(Ticket ticket) {
        Ticket tmp = set.find(ticket.tid);
        if (tmp != null && Utils.ticketEquals(tmp, ticket)) {
            set.removeWithKey(ticket.tid);
            return true;
        }
        return false;
    }
}
