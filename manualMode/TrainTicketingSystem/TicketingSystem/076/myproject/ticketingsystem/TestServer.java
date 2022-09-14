package ticketingsystem;
import java.util.*;
import java.util.Date;
public class TestServer {
	public static void main(String[] args) throws InterruptedException {
        // Data are used to define our system.
        final int[] threadnums = { 1, 2, 4, 8, 16, 32, 64, 128 };
        final int routenum = 5;
        final int coachnum = 8;
        final int seatnum = 100;
        final int stationnum = 10;
        // Data are used to test system performance.
        final int testnum = 1000000;
        // 10% asking is refunding tickets.
        final int refpc = 10;
        // 30% asking is buying tickets.
        final int buypc = 40;
        // 60% asking is inquiring tickets.
        final int inqpc = 100;
        System.out.println("TestServer: routenum: " + routenum +
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
                // One thread simulates a user of our system.
                threads[i] = new Thread(new Runnable() {
                    public void run() {
                        Random rand = new Random();
                        Ticket ticket = new Ticket();
                        ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();
                        //Now let's invoke the functions.
                        for (int i = 0; i < testnum; i++) {
                            int sel = rand.nextInt(inqpc);
                            // Let's try to refund a sold ticket.
                            if (0 <= sel && sel < refpc && soldTicket.size() > 0) {
                                int select = rand.nextInt(soldTicket.size());
                                ticket = soldTicket.remove(select);
                                if (ticket != null) {
                                    if (tds.refundTicket(ticket)) {
                                        // refundTicket() successfully.
                                        // System.out.println("refundTicket() successfully.");
                                    } else {
                                        // refundTicket() failed.
                                        // System.out.println("refundTicket() failed.");
                                    }
                                } else {
                                    // ErrOfRefund1
                                        // System.out.println("ErrOfRefund1");
                                }
                            // Let's try to buy a sold ticket.
                            } else if (refpc <= sel && sel < buypc) {
                                String passenger = "john";
                                int route = rand.nextInt(routenum) + 1;
                                int departure = rand.nextInt(stationnum - 1) + 1;
                                int arrival = departure
                                    + rand.nextInt(stationnum - departure) + 1;
                                ticket = tds.buyTicket(
                                    passenger, route, departure, arrival);
                                if (ticket != null) {
                                    soldTicket.add(ticket);
                                    // System.out.println("Bought a ticket.");
                                } else {
                                    // Ticket had sold out.
                                    // System.out.println("Ticket had sold out.");
                                }
                            // Let's try to inquiry a ticket.
                            } else if (buypc <= sel && sel < inqpc) {
                                int route = rand.nextInt(routenum) + 1;
                                int departure = rand.nextInt(stationnum - 1) + 1;
                                int arrival = departure
                                    + rand.nextInt(stationnum - departure) + 1;
                                int leftTicket = tds.inquiry(
                                    route, departure, arrival);
                                // System.out.println("Count of losing free tickets " + leftTicket);
                            }
                        }
                    }
                });
                threads[i].start();
            }
            // The main thread will wait all sub-threads for those to die.
            for (int i = 0; i < threadnum; i++) {
                threads[i].join();
            }
            // Calculate the time we have used.
            long endTime = System.currentTimeMillis();
            // Convert millisecond to second.
            double timeused = (endTime - startTime) / 1000.0;
            // Then show the result.
            System.out.format("CountOfThreads: %3d TimeUsed: %3.2fs Throughout: %.2f%n", 
            		threadnum, timeused, (threadnum * testnum / timeused));
        }
	}
}