package ticketingsystem;


import java.util.ArrayList;
import java.util.Random;

public class TempTest {
    public static void main(String[] args) throws InterruptedException {
        final int[] threadnums = {96, 96, 96, 96, 96, 96, 96, 96};
        final int routenum = 20;
        final int coachnum = 15;
        final int seatnum = 100;
        final int stationnum = 10;

        final int testnum = 1000000;
        final int refpc = 10;
        final int buypc = 40;
        final int inqpc = 100;

        System.out.println("ThreadTest: routenum: " + routenum +
                " coachnum: " + coachnum +
                " seatnum: " + seatnum +
                " stationnum: " + stationnum +
                " testnum: " + testnum + "/thread");

        int length = threadnums.length;
        for (int j = 0; j < length; j++) {
            int threadnum = threadnums[j];
            Thread[] threads = new Thread[threadnum];

            final TicketingDS tds = new TicketingDS(
                    routenum, coachnum, seatnum, stationnum, threadnum);

            long startTime = System.currentTimeMillis();

            for (int i = 0; i < threadnum; i++) {
                threads[i] = new Thread(new Runnable() {
                    public void run() {
                        Random rand = new Random();
                        Ticket ticket = new Ticket();
                        ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();

                        for (int i = 0; i < testnum; i++) {
                            int sel = rand.nextInt(inqpc);
                            if (0 <= sel && sel < refpc && soldTicket.size() > 0) {
                                int select = rand.nextInt(soldTicket.size());
                                ticket = soldTicket.remove(select);
                                if (ticket != null) {
                                    if (tds.refundTicket(ticket)) {
                                    }
                                    else {
                                        System.out.println("refund false, ErrOfRefund");
                                        System.out.flush();
                                    }
                                }
                                else {
                                    System.out.println("Ticket null, ErrOfRefund");
                                    System.out.flush();
                                }
                            }
                            else if (refpc <= sel && sel < buypc) {
                                String passenger = "john";
                                int route = rand.nextInt(routenum) + 1;
                                int departure = rand.nextInt(stationnum - 1) + 1;
                                int arrival = departure
                                        + rand.nextInt(stationnum - departure) + 1;
                                ticket = tds.buyTicket(
                                        passenger, route, departure, arrival);
                                if (ticket != null) {
                                    soldTicket.add(ticket);
                                }
                                else {}
                            }
                            else if (buypc <= sel && sel < inqpc) {
                                int route = rand.nextInt(routenum) + 1;
                                int departure = rand.nextInt(stationnum - 1) + 1;
                                int arrival = departure
                                        + rand.nextInt(stationnum - departure) + 1;
                                int leftTicket = tds.inquiry(route, departure, arrival);
                            }
                        }
                    }
                });
                threads[i].start();
            }

            for (int i = 0; i < threadnum; i++) {
                threads[i].join();
            }

            long endTime = System.currentTimeMillis();
            double timeused = (endTime - startTime) / 1000.0;
            System.out.format("CountOfThreads: %3d TimeUsed: %3.2fs Throughout: %.2f%n", threadnum, timeused, (threadnum * testnum / timeused));
        }

    }
}
