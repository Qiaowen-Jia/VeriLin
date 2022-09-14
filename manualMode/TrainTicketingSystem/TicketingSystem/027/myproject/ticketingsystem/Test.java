package ticketingsystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 965087276@qq.com
 * @date 2019/12/11 10:50
 */
public class Test {

    /**
     * 每次测试的线程数
     */
    private static final int[] threadsCount = {96, 96, 96, 96, 96};

    final static int routeNum = 20; // route is designed from 1 to 3
    final static int coachNum = 15; // coach is arranged from 1 to 5
    final static int seatNum = 100; // seat is allocated from 1 to 20
    final static int stationNum = 10; // station is designed from 1 to 5

    final static int testNum = 500000;
    final static int retpc = 5; // return ticket operation is 10% percent
    final static int buypc = 20; // buy ticket operation is 30% percent
    final static int inqpc = 100; //inquiry ticket operation is 60% percent

    static String passengerName() {
        Random rand = new Random();
        long uid = rand.nextInt(testNum);
        return "passenger" + uid;
    }

    public static void main(String[] args) throws InterruptedException {
        for (int threadNum : threadsCount) {
            AtomicInteger threadCount = new AtomicInteger(0);
            long[] buyCounts = new long[threadNum];
            long[] refundCounts = new long[threadNum];
            long[] inquiryCounts = new long[threadNum];
            long[] buyCost = new long[threadNum];
            long[] refundCost = new long[threadNum];
            long[] inquiryCost = new long[threadNum];
            Thread[] threads = new Thread[threadNum];
            TicketingDS tds = new TicketingDS(routeNum, coachNum, seatNum, stationNum, threadNum);
            for (int p = 0; p < threadNum; p++) {
                threads[p] = new Thread(() -> {
                    int threadID = threadCount.getAndIncrement();
                    Random random = new Random();
                    Ticket ticket = null;
                    List<Ticket> soldTickets = new ArrayList<>();
                    for (int i = 0; i < testNum; i++) {
                        int sel = random.nextInt(inqpc);
                        // refund ticket
                        if (0 <= sel && sel < retpc && soldTickets.size() > 0) {
                            int select = random.nextInt(soldTickets.size());
                            if ((ticket = soldTickets.remove(select)) != null) {
                                long start = System.nanoTime();
                                boolean ok = tds.refundTicket(ticket);
                                long cost = System.nanoTime() - start;
                                if (ok) {
                                    ++refundCounts[threadID];
                                    refundCost[threadID] += cost;
                                }
                                else {
                                    System.out.println("ErrOfRefund");
                                    System.out.flush();
                                }
                            }
                            else {
                                System.out.println("ErrOfRefund");
                                System.out.flush();
                            }
                        }
                        // buy ticket
                        else if (retpc <= sel && sel < buypc) {
                            String passager = passengerName();
                            int route = random.nextInt(routeNum) + 1;
                            int departure = random.nextInt(stationNum - 1) + 1;
                            int arrival = departure + random.nextInt(stationNum - departure) + 1;
                            long start = System.nanoTime();
                            ticket = tds.buyTicket(passager, route, departure, arrival);
                            long cost = System.nanoTime() - start;
                            ++buyCounts[threadID];
                            buyCost[threadID] += cost;
                            if (ticket != null) {
                                soldTickets.add(ticket);
                            }
                            else {
                                // sold out
                            }
                        }
                        // inquiry ticket
                        else if (buypc <= sel) {
                            int route = random.nextInt(routeNum) + 1;
                            int departure = random.nextInt(stationNum - 1) + 1;
                            int arrival = departure + random.nextInt(stationNum - departure) + 1;
                            long start = System.nanoTime();
                            int leftCount = tds.inquiry(route, departure, arrival);
                            long cost = System.nanoTime() - start;
                            ++inquiryCounts[threadID];
                            inquiryCost[threadID] += cost;
                        }
                    }
                });
            }
            long start = System.currentTimeMillis();
            for (Thread thread : threads) {
                thread.start();
            }
            for (Thread thread : threads) {
                thread.join();
            }
            long allCost = System.currentTimeMillis() - start;

            long buyCountTotal = Arrays.stream(buyCounts).sum();
            long refundCountTotal = Arrays.stream(refundCounts).sum();
            long inquiryCountTotal = Arrays.stream(inquiryCounts).sum();

            long buyCostTotal = Arrays.stream(buyCost).sum();
            long refundCostTotal = Arrays.stream(refundCost).sum();
            long inquiryCostTotal = Arrays.stream(inquiryCost).sum();

            long avgBuyCost = buyCostTotal / buyCountTotal;
            long avgRefundCost = refundCostTotal / refundCountTotal;
            long avgInquiryCost = inquiryCostTotal / inquiryCountTotal;

            long throughtPut = (long) threadNum * (long) testNum * 1000L / allCost;

            double timeUsed = allCost / 1000.0;

            System.out.printf("ThreadCount: %d AvgBuyTime(ns): %d AvgRefundTime(ns): %d AvgInquiryTime(ns): %d TimeUsed(s): %.3f ThroughOut: %d\n", threadNum, avgBuyCost, avgRefundCost, avgInquiryCost, timeUsed, throughtPut);


        }
    }
}
