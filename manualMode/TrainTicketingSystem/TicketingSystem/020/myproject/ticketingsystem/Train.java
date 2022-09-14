package ticketingsystem;

public interface Train {
    int inquiry(int departure, int arrival);
    boolean get(int departure, int arrival, int[] coach, int[] seat);
    void put(int departure, int arrival, int coach, int seat);
}
