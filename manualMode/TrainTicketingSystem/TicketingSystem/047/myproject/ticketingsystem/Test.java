package ticketingsystem;

/**
 * @author haoheipi
 * @date 2020/1/8
 */


import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class Test {

    private static class MyRunnable implements Runnable {

        private final TicketingDS tds;

        private final int routeNum;

        private final int stationNum;

        private final int testNum;

        private final boolean debug;

        private final AtomicInteger[] counters;

        private final AtomicLong[] timers;

        private final Collection<Ticket> allTickets;

        public MyRunnable(TicketingDS tds, int routeNum, int stationNum, int testNum, boolean debug, AtomicInteger[] counters, AtomicLong[] timers, Collection<Ticket> allTickets) {
            this.tds = tds;
            this.routeNum = routeNum;
            this.stationNum = stationNum;
            this.testNum = testNum;
            this.debug = debug;
            this.counters = counters;
            this.timers = timers;
            this.allTickets = allTickets;
        }

        @Override
        public void run() {
            try {
                List<Ticket> tickets = new LinkedList<>();
                Random random = new Random();

                long time1 = 0, time2 = 0;
                int sel, route, departure, arrival, left, index;
                Ticket ticket;
                for (int j = 0; j < testNum; ++j) {

                    sel = random.nextInt(100);
                    if (sel < 80) {
                        sel = 0;
                    } else if (sel < 95) {
                        sel = 1;
                    } else {
                        sel = 2;
                    }

                    if (sel == 0) {
                        // inquiry
                        route = random.nextInt(routeNum) + 1;
                        departure = random.nextInt(stationNum - 1) + 1;
                        arrival = departure + random.nextInt(stationNum - departure) + 1;

                        time1 = System.currentTimeMillis();
                        left = tds.inquiry(route, departure, arrival);
                        time2 = System.currentTimeMillis();
                        if (debug) {
                            System.out.println(String.format("route %d: %d -> %d, left %s", route, departure, arrival, left));
                        }
                    } else if (sel == 1) {
                        // buy
                        String passenger = "p" + j;
                        route = random.nextInt(routeNum) + 1;
                        departure = random.nextInt(stationNum - 1) + 1;
                        arrival = departure + random.nextInt(stationNum - departure) + 1;

                        time1 = System.currentTimeMillis();
                        ticket = tds.buyTicket(passenger, route, departure, arrival);
                        time2 = System.currentTimeMillis();

                        if (ticket == null) {
                            if (debug) {
                                System.out.println(String.format("route %d: %d -> %d, sold out!", route, departure, arrival));
                            }
                        } else {
                            if (debug) {
                                System.out.println(String.format("route %d: %d -> %d, bought!", route, departure, arrival));
                            }
                            tickets.add(ticket);
                        }
                    } else {
                        // refund
                        if (!tickets.isEmpty()) {
                            index = random.nextInt(tickets.size());

                            ticket = tickets.remove(index);

                            if (ticket != null) {
                                time1 = System.currentTimeMillis();
                                if (!tds.refundTicket(ticket)) {
                                    throw new RuntimeException("refund failed: " + ticket.toString());
                                }
                                time2 = System.currentTimeMillis();
                                if (debug) {
                                    System.out.println(String.format("route %d: %d -> %d, refund!", ticket.route, ticket.departure, ticket.arrival));
                                }
                            }
                        }
                    }
                    counters[sel].incrementAndGet();
                    timers[sel].addAndGet(time2 - time1);
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }

    private static float once(final String prefix, final int routeNum, final int coachNum, final int seatNum, final int stationNum, final int threadNum, final int testNum, final boolean debug) throws InterruptedException {
        System.out.printf("%s: %d route, %d coach, %d seat, %d station\n",
                prefix, routeNum, coachNum, seatNum, stationNum);



        final TicketingDS tds = new TicketingDS(routeNum, coachNum, seatNum, stationNum, threadNum);

        ExecutorService executor = Executors.newFixedThreadPool(threadNum);

        final List<AtomicInteger[]> countersList = new ArrayList<>(threadNum);
        final List<AtomicLong[]> timersList = new ArrayList<>(threadNum);
        final Queue<Ticket> allTickets = new ConcurrentLinkedQueue<>();

        long time1 = System.currentTimeMillis();
        for (int i = 0; i < threadNum; ++i) {
            AtomicInteger[] counters = new AtomicInteger[3];
            AtomicLong[] timers = new AtomicLong[3];
            for (int j = 0; j < 3; ++j) {
                counters[j] = new AtomicInteger(0);
                timers[j] = new AtomicLong(0);
            }
            countersList.add(counters);
            timersList.add(timers);
            executor.submit(new MyRunnable(tds, routeNum, stationNum, testNum, debug, counters, timers, allTickets));
        }

        executor.shutdown();

        int minutes = 0;
        while (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
            System.out.printf("%s: waiting %dmins..\n", prefix, ++minutes);
        }

        long time2 = System.currentTimeMillis();

        int[] cs = new int[3];
        int[] call = new int[3];
        long[] ts = new long[3];
        long[] tall = new long[3];
        for (int j = 0; j < countersList.size(); j++) {
            AtomicInteger[] counters = countersList.get(j);
            AtomicLong[] timers = timersList.get(j);
            for (int k = 0; k < 3; ++k) {
                cs[k] = counters[k].get();
                ts[k] = timers[k].get();
                call[k] += cs[k];
                tall[k] += ts[k];
            }
            if (debug) {
                System.out.printf("%s: %d with: %d inquiry %dms, %d buy %dms, %d refund %dms\n", prefix, j,
                        cs[0], ts[0],
                        cs[1], ts[1],
                        cs[2], ts[2]
                );
            }
        }
        System.out.printf("%s: %d inquiry %dms, %d buy %dms, %d refund %dms\n", prefix,
                call[0], tall[0],
                call[1], tall[1],
                call[2], tall[2]
        );

        double s = (tall[0] + tall[1] + tall[2]);
        double v0 = tall[0]  / s;
        double v1 = tall[1]  / s;
        double v2 = tall[2]  / s;

        long timediff = time2 - time1;
        float time2ns = (float) (timediff * Math.pow(10,6));
        System.out.printf("%s: inquiry avg %fns, buy avg %fns, refund avg %fns\n", prefix,
                time2ns * v0 / call[0],
                time2ns * v1 / call[1],
                time2ns * v2 / call[2]
        );
        float throughput = (threadNum * (float) testNum) / timediff;
        System.out.printf("%s: %d threads, %d tests, used %dms, %fkop/s\n",
                prefix, threadNum, testNum, timediff, throughput);

        System.out.println("-------------------------------------------");
        return timediff;
    }


    public static void main(String[] args) throws InterruptedException {
//        int core = Runtime.getRuntime().availableProcessors();
        int core = 96;

        int defaultRouteNum = 20;
        int defaultCoachNum = 15;
        int defaultSeatNum = 100;
        int defaultStationNum = 10;


        //once("1x50w", 1, 1, 5, 5, 1, 100000, false);
        System.out.println();

       int[] testCores = new int[]{1, 4, 8, 16, 32, 64, 96};
//        int[] testCores = new int[]{ 96};
        int testNum = 500000;
        for (int k = 0; k < testCores.length; ++k) {
            if (core >= testCores[k]) {
                int usedCore = testCores[k];
                long sumTime = 0;
                for (int i = 1; i <= 5; ++i) {
                    sumTime += once(usedCore + "x50w-" + i, defaultRouteNum, defaultCoachNum, defaultSeatNum, defaultStationNum, usedCore, testNum, false);
                }
                float avgThroughput = (usedCore * 5 * (float) testNum ) / sumTime ;
                System.out.printf("%d threads, %d tests, avg :used %dms, %fkop/s\n",
                        usedCore, testNum, sumTime / 5, avgThroughput);
                System.out.println();
            }
        }

        System.out.println("test finished");
    }
}
