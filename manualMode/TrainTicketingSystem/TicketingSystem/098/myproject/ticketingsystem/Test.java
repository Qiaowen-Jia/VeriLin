package ticketingsystem;

import java.util.ArrayList;
import java.util.Random;

public class Test {
    public static void main(String[] args) throws InterruptedException {
        final int[] threadNums = {4, 8, 16, 32, 64,96};
        final int routeNum = 20;
        final int coachNum = 15;
        final int seatNum = 100;
        final int stationNum = 10;

        final int a=80;//inquiry
        final int b=15;//buy
        final int c=5;//refund

        final int testNum = 100000;
        final int refPc = c;
        final int buyPc = b+c;
        final int inqPc = a+b+c;

        System.out.println("Test: routeNum: " + routeNum +
                " coachNum: " + coachNum +
                " seatNum: " + seatNum +
                " stationNum: " + stationNum +
                " testNum: " + testNum + "/thread");

        for (int threadNum : threadNums) {
            Thread[] threads = new Thread[threadNum];

            final TicketingDS tds = new TicketingDS(
                    routeNum, coachNum, seatNum, stationNum, threadNum);

            long startTime = System.currentTimeMillis();

            for (int i = 0; i < threadNum; i++) {
                threads[i] = new Thread(() -> {
                    Random rand = new Random();
                    ArrayList<Ticket> soldTicket = new ArrayList<>();

                    Ticket ticket;
                    for (int j = 0; j < testNum; j++) {
                        int sel = rand.nextInt(inqPc);
                        if (0 <= sel && sel < refPc && soldTicket.size() > 0) {// refund ticket
                            int select = rand.nextInt(soldTicket.size());
                            ticket = soldTicket.remove(select);
                            if (ticket != null) {
                                tds.refundTicket(ticket);
                            }
                        } else if (refPc <= sel && sel < buyPc) {// buy ticket
                            String passenger = "passenger" + j;
                            int route = rand.nextInt(routeNum) + 1;
                            int departure = rand.nextInt(stationNum - 1) + 1;
                            int arrival = departure
                                    + rand.nextInt(stationNum - departure) + 1;
                            ticket = tds.buyTicket(
                                    passenger, route, departure, arrival);
                            if (ticket != null) {
                                soldTicket.add(ticket);
                            }
                        } else if (buyPc <= sel && sel < inqPc) {// inquiry ticket
                            int route = rand.nextInt(routeNum) + 1;
                            int departure = rand.nextInt(stationNum - 1) + 1;
                            int arrival = departure
                                    + rand.nextInt(stationNum - departure) + 1;
                            tds.inquiry(route, departure, arrival);
                        }
                    }
                });
                threads[i].start();
            }

            for (int i = 0; i < threadNum; i++) {
                threads[i].join();
            }

            long endTime = System.currentTimeMillis();
            double timeUsed = (endTime - startTime) / 1000.0;
            System.out.format("线程数: %2d 平均执行时间: %.2fs 总吞吐率: %.2f/s%n", threadNum, timeUsed, (threadNum * testNum / timeUsed));
        }

    }
}
