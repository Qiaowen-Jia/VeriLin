package ticketingsystem;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * @author 965087276@qq.com
 * @date 2019/12/10 19:54
 */
public class MyTest {
    private final static int ROUTE_NUM = 5;
    private final static int COACH_NUM = 8;
    private final static int SEAT_NUM = 1000;
    private final static int STATION_NUM = 10;



    public static void main(String[] args) {
        List<Ticket> list = new ArrayList<>();
        TicketingDS ds = new TicketingDS(ROUTE_NUM, COACH_NUM, SEAT_NUM, STATION_NUM, 1);
        for (int i = 0; i < COACH_NUM * SEAT_NUM; i++) {
            for (int j = 1; j < STATION_NUM; j++) {
                int count1 = ds.inquiry(1, j, j+1);
                Ticket ticket = ds.buyTicket("wang" + i + j, 1, j, j+1);
                list.add(ticket);
                int count2 = ds.inquiry(1, j, j+1);
                if (ticket == null) {
                    System.out.println(i + " :error! should not empty!");
                }
                if (count1 - 1 != count2) {
                    System.out.println("count error");
                }
            }
        }
        int cnt = 0;
        for (int i = 0; i < COACH_NUM * SEAT_NUM; i++) {
            for (int j = 1; j < STATION_NUM; j++) {
                Ticket ticket = ds.buyTicket("wang" + i + j, 1, j, j+1);
                if (ticket != null) {
                    System.out.println(i + " :error! should be empty!");
                }
                ticket = list.get(cnt++);
                if (!ds.refundTicket(ticket)) {
                    System.out.println("error! should can refund");
                }
                int count1 = ds.inquiry(1, j, j+1);
                if (count1 != 1) {
                    System.out.println("should one");
                }
                ticket = ds.buyTicket("wang" + i + j, 1, j, j+1);
                if (ticket == null) {
                    System.out.println(i + " :error! should not be empty!");
                }
                int count2 = ds.inquiry(1, j, j+1);
                if (count2 != 0) {
                    System.out.println(i + " :error! should empty!");
                }
            }
        }

    }
}
