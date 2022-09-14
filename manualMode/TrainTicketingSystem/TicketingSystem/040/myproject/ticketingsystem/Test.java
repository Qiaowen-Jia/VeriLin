package ticketingsystem;

import java.util.ArrayList;
import java.util.Random;

public class Test {

    static int threadnum = 96;
    final static int routenum = 20;
    final static int coachnum = 15;
    final static int seatnum = 100;
    final static int stationnum = 10;

    final static int testnum = 500000;

    final static int retpc = 5; // 5%
    final static int buypc = 20; // 15%
    final static int inqpc = 100; // 80%

    final static double NANO_TO_MILLS = 1e-6;
    static String passengerName() {
        Random rand = new Random();
        long uid = rand.nextInt(testnum);
        return "passenger" + uid;
    }


    static class TicketingThread extends Thread {
        TicketingDS tds;

        public int retCount;
        public int buyCount;
        public int inqCount;

        public long retTime;
        public long buyTime;
        public long inqTime;


        public TicketingThread(TicketingDS tds) {
            this.tds = tds;
            retCount = 0;
            buyCount = 0;
            inqCount = 0;

            retTime = 0;
            buyTime = 0;
            inqTime = 0;

        }

        @Override
        public void run() {
            Random rand = new Random();
            Ticket ticket = new Ticket();
            ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();
            for(int i = 0; i < testnum; i++) {
                int roll = rand.nextInt(inqpc);
                if (0 <= roll && roll < retpc && soldTicket.size() > 0) {
                    int select = rand.nextInt(soldTicket.size());
                    if ((ticket = soldTicket.remove(select)) != null) {
                        retCount++;
                        long startRetTime = System.nanoTime();
                        boolean refundRes = tds.refundTicket(ticket);
                        retTime += System.nanoTime() - startRetTime;
                        if (refundRes) {

                        } else {
                            System.out.println("ErrOfRefund");
                            System.out.flush();
                        }
                    } else {
                        System.out.println("ErrOfRefund");
                        System.out.flush();
                    }
                } else if (retpc <= roll && roll < buypc) {
                    // buy ticket
                    String passenger = passengerName();
                    int route = rand.nextInt(routenum) + 1;
                    int departure = rand.nextInt(stationnum - 1) + 1;
                    int arrival = departure + rand.nextInt(stationnum - departure) + 1;
                    buyCount++;
                    long startBuyTime = System.nanoTime();
                    ticket = tds.buyTicket(passenger, route, departure, arrival);
                    buyTime += System.nanoTime() - startBuyTime;
                    if (ticket != null) {
                        soldTicket.add(ticket);
                    } else {
                        //System.out.println("TicketSoldOut" + " " + route + " " + departure + " " + arrival);
                        //System.out.flush();
                    }
                } else if (buypc <= roll && roll < inqpc) {
                    // inquiry ticket
                    int route = rand.nextInt(routenum) + 1;
                    int departure = rand.nextInt(stationnum - 1) + 1;
                    int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
                    inqCount++;
                    long startInqTime = System.nanoTime();
                    int leftTicket = tds.inquiry(route, departure, arrival);
                    inqTime += System.nanoTime() - startInqTime;
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int[] t = {4,8,16,32,64,96};
        for(int c = 0; c < 6; c++) {
            threadnum = t[c];
            final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
            // ToDo
            int totalRetCount = 0;
            int totalBuyCount = 0;
            int totalInqCount = 0;

            long totalRetTime = 0;
            long totalBuyTime = 0;
            long totalInqTime = 0;

            long totalTime = 0;

            TicketingThread[] threads = new TicketingThread[threadnum];
            for (int i = 0; i < threadnum; i++) {
                threads[i] = new TicketingThread(tds);

            }
            long startTime = System.nanoTime();
            for (int i = 0; i < threadnum; i++) {
                threads[i].start();
            }
            for (int i = 0; i < threadnum; i++) {
                threads[i].join();

            }
            long endTime = System.nanoTime();
            for(int i = 0; i < threadnum; i++) {
                totalRetCount += threads[i].retCount;
                totalBuyCount += threads[i].buyCount;
                totalInqCount += threads[i].inqCount;

                totalRetTime += threads[i].retTime;
                totalBuyTime += threads[i].buyTime;
                totalInqTime += threads[i].inqTime;
            }

            totalTime = endTime - startTime;
            System.out.println("Thread number:" + threadnum);
            System.out.println("Ret Avg Time:" + ((double)totalRetTime) / totalRetCount * NANO_TO_MILLS + " ms");
            System.out.println("Buy Avg Time:" + ((double)totalBuyTime) / totalBuyCount * NANO_TO_MILLS + " ms");
            System.out.println("Inq Avg Time:" + ((double)totalInqTime) / totalInqCount * NANO_TO_MILLS + " ms");
            System.out.println("吞吐率:" + ((double)(threadnum * testnum))/(totalTime * NANO_TO_MILLS) + " ops/ms");
        }
    }

}



