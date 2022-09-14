package ticketingsystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Test {
    static AtomicInteger buy_exe_num = new AtomicInteger(0);
    static AtomicInteger inquiry_exe_num = new AtomicInteger(0);
    static AtomicInteger refund_exe_num = new AtomicInteger(0);
    static AtomicLong buy_sum_time = new AtomicLong(0);
    static AtomicLong inquiry_sum_time = new AtomicLong(0);
    static AtomicLong refund_sum_time = new AtomicLong(0);
    static int routenum = 5;
    static int coachnum = 8;
    static int seatnum = 100;
    static int stationnum = 10;
    static int threadnum = 96;
    static int testnum = 10000;

    static double buyRatio = 30;
    static double inquiryRatio = 60;
    static double rRatio = 10;


    public static class MyTask implements Runnable {
        @Override
        public void run() {
            //System.out.println(System.currentTimeMillis() + "Thread ID:" + Thread.currentThread().getId());
            try {
                List<Ticket> tickets = new ArrayList<>();
                final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);

                for (int i = 0; i < testnum; ++i) {
                    Random r = new Random();
                    double ratio = r.nextDouble() * 100;
                    //System.out.println(ratio);
                    if (ratio < inquiryRatio) {
                        int route = r.nextInt(routenum) + 1;
                        int departure = r.nextInt(stationnum);
                        int arrival = departure + r.nextInt(stationnum - departure) + 1;

                        long inquiry_start_time = System.currentTimeMillis();
                        tds.inquiry(route, departure, arrival);
                        long inquiry_end_time = System.currentTimeMillis();
                        inquiry_exe_num.getAndIncrement();
                        inquiry_sum_time.getAndAdd(inquiry_end_time - inquiry_start_time);

                    } else if (ratio < inquiryRatio + buyRatio) {
                        String passenger = "passenger" + Math.random() * 1000;
                        int route = r.nextInt(routenum) + 1;
                        int departure = r.nextInt(stationnum);
                        int arrival = departure + r.nextInt(stationnum - departure) + 1;

                        long buy_start_time = System.currentTimeMillis();
                        Ticket ticket = tds.buyTicket(passenger, route, departure, arrival);
                        long buy_end_time = System.currentTimeMillis();

                        if (ticket != null)
                            tickets.add(ticket);
                        buy_exe_num.getAndIncrement();
                        buy_sum_time.getAndAdd(buy_end_time - buy_start_time);
                    } else {
                        if (tickets.size() != 0) {
                            int index = r.nextInt(tickets.size());

                            long refund_start_time = System.currentTimeMillis();
                            tds.refundTicket(tickets.get(index));
                            long refund_end_time = System.currentTimeMillis();

                            refund_exe_num.getAndIncrement();
                            refund_sum_time.getAndAdd(refund_end_time - refund_start_time);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        MyTask task = new MyTask();
        ExecutorService es = Executors.newFixedThreadPool(threadnum);
        for(int i = 0; i < threadnum; ++ i){
            es.submit(task);
        }
        try {
            es.shutdown();
            if(!es.awaitTermination(20,TimeUnit.SECONDS)){//20S
                System.out.println("remain threads");
                es.shutdownNow();
            }
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            es.shutdownNow();
            e.printStackTrace();
        }
        System.out.println("buyTicket average time : " + (buy_sum_time.get() / (double) buy_exe_num.get()) + " ms");
        System.out.println("inquiry average time : " + (inquiry_sum_time.get() / (double) inquiry_exe_num.get()) + " ms");
        System.out.println("refundTicket average time : " + (refund_sum_time.get() / (double) refund_exe_num.get()) + " ms");

        int exeNum = buy_exe_num.get() + inquiry_exe_num.get() + refund_exe_num.get();
        System.out.println(exeNum);
        long sumTime = buy_sum_time.get() + inquiry_sum_time.get() + refund_sum_time.get();
        System.out.println("The throughput is :" + 1000 * exeNum / (double) sumTime + " thread per s");
        System.out.println("Sum time is " + sumTime + " ms");
    }
}
