package ticketingsystem;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class Test {

    private static class MyRunnable implements Runnable {
        private final TicketingDS tds;
        private final int routeNum,stationNum,testNum;
        private final boolean debug;
        private final AtomicInteger[] counters;
        private final AtomicLong[] timers;
        private final Collection<Ticket> wholeTickets;

        public MyRunnable(TicketingDS tds, int routeNum, int stationNum, int testNum, boolean debug, AtomicInteger[] counters, AtomicLong[] timers, Collection<Ticket> Tickets) {
            this.tds = tds;
            this.routeNum = routeNum;
            this.stationNum = stationNum;
            this.testNum = testNum;
            this.debug = debug;
            this.counters = counters;
            this.timers = timers;
            this.wholeTickets = Tickets;
        }

        public void run() {
            try {
                List<Ticket> tickets = new LinkedList<>();
                Random random = new Random();

                long timestart = 0, timeover = 0;
                int mode, r, d, a, l, ticNum;
                Ticket ticket;
                for (int j = 0; j < testNum; ++j) {
                	mode= random.nextInt(100);
                    if (mode< 60) {
                        mode= 0;
                    } 
                    else if (mode< 90) {
                        mode= 1;
                    }
                    else {
                        mode= 2;
                    }
                    
                    if (mode== 0) {// inquiry
                        r = random.nextInt(routeNum) + 1;
                        d = random.nextInt(stationNum - 1) + 1;
                        a = d + random.nextInt(stationNum - d) + 1;
                        timestart = System.currentTimeMillis();
                        l= tds.inquiry(r, d, a);
                        timeover = System.currentTimeMillis();
                        if (debug) {
                            System.out.println(String.format("route %d: %d -> %d, left %s", r, d, a,l));
                        }
                    } 
                    else if (mode== 1) {// buy
                        String p = "passenger" + j;
                        r= random.nextInt(routeNum) + 1;
                        d= random.nextInt(stationNum - 1) + 1;
                        a= d + random.nextInt(stationNum - d) + 1;
                        timestart = System.currentTimeMillis();
                        ticket = tds.buyTicket(p, r, d, a);
                        timeover = System.currentTimeMillis();

                        if (ticket == null) {
                            if (debug) {
                                System.out.println(String.format("route %d: %d -> %d,have been sold out!", r, d, a));
                           }
                        } 
                        else {
                            if (debug) {
                                System.out.println(String.format("route %d: %d -> %d,successfully bought!", r, d, a));
                            }
                            tickets.add(ticket);
                        }
                    } 
                    else {// refund
                        if (!tickets.isEmpty()) {
                        	ticNum = random.nextInt(tickets.size());
                            ticket = tickets.remove(ticNum);

                            if (ticket != null) {
                                timestart = System.currentTimeMillis();
                                if (!tds.refundTicket(ticket)) {
                                    throw new RuntimeException("unsuccessfully refund: " + ticket.toString());
                                }
                                timeover = System.currentTimeMillis();
                                if (debug) {
                                    System.out.println(String.format("route %d: %d -> %d, refund!", ticket.route, ticket.departure, ticket.arrival));
                                }
                            }
                        }
                    }
                    counters[mode].incrementAndGet();
                    timers[mode].addAndGet(timeover - timestart);
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }

    private static float ACycle(final String firsts, final int routeNum, final int coachNum, final int seatNum, final int stationNum, final int threadNum, final int testNum, final boolean debug) throws InterruptedException {
        System.out.printf("%s: %d route, %d coach, %d seat, %d station\n",firsts, routeNum, coachNum, seatNum, stationNum);

        final TicketingDS tds = new TicketingDS(routeNum, coachNum, seatNum, stationNum, threadNum);
        ExecutorService e= Executors.newFixedThreadPool(threadNum);

        final List<AtomicInteger[]> countersList = new ArrayList<>(threadNum);
        final List<AtomicLong[]> timersList = new ArrayList<>(threadNum);
        final Queue<Ticket> wholeTickets = new ConcurrentLinkedQueue<>();
        long timestart = System.currentTimeMillis();
        for (int i = 0; i < threadNum; ++i) {
            AtomicInteger[] counters = new AtomicInteger[3];
            AtomicLong[] timers = new AtomicLong[3];
            for (int j = 0; j < 3; ++j) {
                counters[j] = new AtomicInteger(0);
                timers[j] = new AtomicLong(0);
            }
            countersList.add(counters);
            timersList.add(timers);
            e.submit(new MyRunnable(tds, routeNum, stationNum, testNum, debug, counters, timers, wholeTickets));
        }
        e.shutdown();
        int mins = 0;
        while (!e.awaitTermination(1, TimeUnit.MINUTES)) {
            System.out.printf("%s: waiti %dmins..\n", firsts, ++mins);
        }
        long timeover = System.currentTimeMillis();
        int[] counters_J = new int[3];
        int[] counter_mode = new int[3];
        long[] timers_J = new long[3];
        long[] timer_mode = new long[3];
        for (int j = 0; j < countersList.size(); j++) {
            AtomicInteger[] counters = countersList.get(j);
            AtomicLong[] timers = timersList.get(j);
            for (int k = 0; k < 3; ++k) {
                counters_J[k] = counters[k].get();
                timers_J[k] = timers[k].get();
                counter_mode[k] += counters_J[k];
                timer_mode[k] += timers_J[k];
            }
            if (debug) {
                System.out.printf("%s: %d with: %d inquiry %dms, %d buy %dms, %d refund %dms\n", firsts, j,counters_J[0], timers_J[0],counters_J[1], timers_J[1],counters_J[2], timers_J[2]);
            }
        }
        System.out.printf("%s: %d inquiry %dms, %d buy %dms, %d refund %dms\n", firsts,counter_mode[0], timer_mode[0],counter_mode[1], timer_mode[1], counter_mode[2], timer_mode[2]);

        double sum= (timer_mode[0] + timer_mode[1] + timer_mode[2]);
        double p0 = timer_mode[0] / sum;
        double p1 = timer_mode[1] / sum;
        double p2 = timer_mode[2] / sum;

        long time_consume = timeover - timestart;
        float time_ns = (float) (time_consume * 1000000);
        System.out.printf("%s: inquiry avg %fns, buy avg %fns, refund avg %fns\n", firsts,time_ns * p0 / counter_mode[0],time_ns * p1 / counter_mode[1],time_ns * p2 / counter_mode[2]
        );
        float throughput = (threadNum * (float) testNum) / time_consume/10;
        System.out.printf("%s: %d threads, %d tests, used %dms, %fwop/s\n",
                firsts, threadNum, testNum, time_consume, throughput);

        System.out.println("-------------------------------------------\n");
        return time_consume;
    }

    public static void main(String[] args) throws InterruptedException {
        int thread_max = 128;
        int defaultRouteNum = 5;
        int defaultCoachNum = 8;
        int defaultSeatNum = 100;
        int defaultStationNum = 10;

        System.out.println();

       int[] testThreads = new int[]{4, 8, 16, 32, 64,128};
        int testNum =100000;
        for (int k = 0; k < testThreads.length; ++k) {
            if (testThreads[k]<=thread_max) {
                int usedthread = testThreads[k];
                long sum_time = 0;
                for (int i = 1; i <= 5; ++i) {
                	sum_time += ACycle(usedthread + "x100k-" + i, defaultRouteNum, defaultCoachNum, defaultSeatNum, defaultStationNum, usedthread, testNum, false);
                }
                float Throughput_avg = (usedthread * 5 * (float) testNum )/sum_time/10;
                System.out.printf("%d threads, %d tests, avg :used %dms, %fwop/s\n",usedthread, testNum,sum_time / 5,Throughput_avg);
                System.out.println("----------------------------------------------\n----------------------------------------------\n");
            }
        }
        System.out.println("test over");
    }
}

