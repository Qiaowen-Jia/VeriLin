package ticketingsystem;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Test {
    private final static int threadnum = 96; // concurrent thread number
    private final static int routenum = 20; // route is designed from 1 to 3
    private final static int coachnum = 15; // coach is arranged from 1 to 5
    private final static int seatnum = 100; // seat is allocated from 1 to 20
    private final static int stationnum = 10; // station is designed from 1 to 5

    private final static int testnum = 500000;
    private final static int retpc = 5; // return ticket operation is 5% percent
    private final static int buypc = 20; // buy ticket operation is 15% percent
    private final static int inqpc = 100; //inquiry ticket operation is 80% percent

    private static String passengerName() {
        Random rand = new Random();
        long uid = rand.nextInt(testnum);
        return "passenger" + uid;
    }

    public static void main(String[] args) throws InterruptedException {
        for (int t = 0; t < 5; t++) {
            long startTime = System.currentTimeMillis();
            Thread[] threads = new Thread[threadnum];
            final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
//        AtomicInteger buyFailTimes = new AtomicInteger(0);
            for (int i = 0; i < threadnum; i++) {
                threads[i] = new Thread(() -> {
                    Random rand = new Random();
                    Ticket ticket;
                    ArrayList<Ticket> soldTicket = new ArrayList<>();
                    for (int j = 0; j < testnum; j++) {
                        int sel = rand.nextInt(inqpc);
                        if (0 <= sel && sel < retpc && soldTicket.size() > 0) { // return ticket
                            int select = rand.nextInt(soldTicket.size());
                            if ((ticket = soldTicket.remove(select)) != null) {
                                if (tds.refundTicket(ticket)) {
//                                System.out.println("TicketRefund" + " " + ticket.tid + " " + ticket.passenger + " " + ticket.route + " " + ticket.coach + " " + ticket.departure + " " + ticket.arrival + " " + ticket.seat);
//                                System.out.flush();
                                } else {
//                                System.out.println("ErrOfRefund");
//                                System.out.flush();
                                }
                            } else {
//                            System.out.println("ErrOfRefund");
//                            System.out.flush();
                            }
                        } else if (retpc <= sel && sel < buypc) { // buy ticket
                            String passenger = passengerName();
                            int route = rand.nextInt(routenum) + 1;
                            int departure = rand.nextInt(stationnum - 1) + 1;
                            int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
                            if ((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null) {
                                soldTicket.add(ticket);
//                            System.out.println("TicketBought" + " " + ticket.tid + " " + ticket.passenger + " " + ticket.route + " " + ticket.coach + " " + ticket.departure + " " + ticket.arrival + " " + ticket.seat);
//                            System.out.flush();
                            } else {
//                            buyFailTimes.getAndIncrement();
//                            System.out.println("TicketSoldOut" + " " + route + " " + departure + " " + arrival);
//                            System.out.flush();
                            }
                        } else if (buypc <= sel && sel < inqpc) { // inquiry ticket
                            int route = rand.nextInt(routenum) + 1;
                            int departure = rand.nextInt(stationnum - 1) + 1;
                            int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
                            int leftTicket = tds.inquiry(route, departure, arrival);
//                        System.out.println("RemainTicket" + " " + leftTicket + " " + route + " " + departure + " " + arrival);
//                        System.out.flush();

                        }
                    }

                });
                threads[i].start();
            }
            for (int i = 0; i < threadnum; i++) {
                threads[i].join();
            }
            long endTime = System.currentTimeMillis();
            System.out.println("done");
//        System.out.println("buy fail times: " + buyFailTimes);
            System.out.println((endTime - startTime) + "ms");
        }
    }
}
