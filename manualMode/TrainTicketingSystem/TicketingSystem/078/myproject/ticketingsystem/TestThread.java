package ticketingsystem;

import java.util.ArrayList;
import java.util.Random;

public class TestThread extends Thread{
    private int[][] numOfmethod;
    private int num;
    private long[][] time;
    private TicketingDS tds;
    private int routenum ; // route is designed from 1 to 3
    private int stationnum ; // station is designed from 1 to 5
    private static int testnum ;
    private final static int retpc = 5; // return ticket operation is 10% percent
    private final static int buypc = 20; // buy ticket operation is 30% percent
    private final static int inqpc = 100; //inquiry ticket operation is 60% percent

    public TestThread(int thread_num, long [][] t, TicketingDS ticketds, int [] set, int[][] numofmethod) {
        numOfmethod = numofmethod;
        num = thread_num;
        time = t;
        tds = ticketds;
        routenum = set[0];
        stationnum = set[1];
        testnum = set[2];
    }

    static String passengerName() {
        Random rand = new Random();
        long uid = rand.nextInt(testnum);
        return "passenger" + uid;
    }

    public void run() {
        Random rand = new Random();
        Ticket ticket = new Ticket();
        ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();
        long time_start_all;
        long time_end;
        long time_start;
        time_start_all = System.currentTimeMillis();
        for (int i = 0; i < testnum; i++) {
            int sel = rand.nextInt(inqpc);
            if (0 <= sel && sel < retpc && soldTicket.size() > 0) { // return ticket
                int select = rand.nextInt(soldTicket.size());
                if ((ticket = soldTicket.remove(select)) != null) {
                    time_start = System.currentTimeMillis();
                    if (tds.refundTicket(ticket)) {
                        time_end = System.currentTimeMillis();
                        time[num][2] += (time_end - time_start);
                        numOfmethod[num][2]++;
                    } else {
                        time_end = System.currentTimeMillis();
                        time[num][2] += (time_end - time_start);
                        numOfmethod[num][2]++;
                    }
                }
            } else if (retpc <= sel && sel < buypc) { // buy ticket
                String passenger = passengerName();
                int route = rand.nextInt(routenum) + 1;
                int departure = rand.nextInt(stationnum - 1) + 1;
                int arrival = departure + rand.nextInt(stationnum - departure) + 1;// arrival is always greater than departure
                time_start = System.currentTimeMillis();
                if ((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null) {
                    time_end = System.currentTimeMillis();
                    time[num][0] += (time_end - time_start);

                    numOfmethod[num][0]++;
                    soldTicket.add(ticket);
//                    System.out.println("TicketBought" + " " + ticket.tid + " " + ticket.passenger + " " + ticket.route + " " + ticket.coach + " " + ticket.departure + " " + ticket.arrival + " " + ticket.seat);
//                    System.out.flush();
                } else {
                    time_end = System.currentTimeMillis();
                    time[num][0] += (time_end - time_start);
                    numOfmethod[num][0]++;
//                    System.out.println("TicketSoldOut" + " " + route+ " " + departure+ " " + arrival);
//                    System.out.flush();

                }
            } else if (buypc <= sel && sel < inqpc) { // inquiry ticket

                int route = rand.nextInt(routenum) + 1;
                int departure = rand.nextInt(stationnum - 1) + 1;
                int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
                time_start = System.currentTimeMillis();

                int leftTicket = tds.inquiry(route, departure, arrival);
                time_end = System.currentTimeMillis();
                time[num][1] += (time_end - time_start);
                numOfmethod[num][1]++;

            }
        }
        long time_cause = System.currentTimeMillis() - time_start_all;
        int numall = numOfmethod[num][0] + numOfmethod[num][1] + numOfmethod[num][2];
        time[num][3] = numall / time_cause * 1000;

    }
}
