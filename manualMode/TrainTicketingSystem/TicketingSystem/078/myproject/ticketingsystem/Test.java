package ticketingsystem;

import java.sql.Time;
import java.text.DecimalFormat;

public class Test {
    private final static int threadnum = 96; // concurrent thread number
    private final static int routenum = 5; // route is designed from 1 to 3
    private final static int coachnum = 8; // coach is arranged from 1 to 5
    private final static int seatnum = 100; // seat is allocated from 1 to 20
    private final static int stationnum = 10; // station is designed from 1 to 5
    private final static int testnum = 500000;

    private static long  time_buy;
    private static long  time_inq;
    private static long  time_ref;


    private static long  time_buy_all;
    private static long  time_inq_all;
    private static long  time_ref_all;

    private static long  min_time_buy_all = Long.MAX_VALUE;
    private static long  min_time_inq_all = Long.MAX_VALUE;
    private static long  min_time_ref_all = Long.MAX_VALUE;


    public static void main(String[] args) throws InterruptedException {
            long num_buy = 0;
            long num_inq = 0;
            long num_ref = 0;
            long num_persecondof_all = 0;
        long num_buy_all = 0;
        long num_inq_all = 0;
        long num_ref_all = 0;

//            Thread[] threads = new TestThread[threadnum];
        for (int k = 0; k < 1;  k++){
            for (int j = 0; j < 1; j++) {
                Thread[] threads = new TestThread[threadnum];
                final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
                int[] set = {routenum, stationnum, testnum};
                //time[thread_num][time_buy,time_inq,time_ref]
                long[][] time = new long[threadnum][4];
                int[][] numOfmethods = new int[threadnum][4];
                for (int i = 0; i < threadnum; i++) {
                    threads[i] = new TestThread(i, time, tds, set, numOfmethods);
                }
                for (int i = 0; i < threadnum; i++) {
                    threads[i].start();
                }
                long maintime = System.currentTimeMillis();
                for (int i = 0; i < threadnum; i++) {
                    threads[i].join();
                }
                maintime = System.currentTimeMillis()- maintime;
                for (int i = 0; i < threadnum; i++) {
                    time_buy += time[i][0];
                    time_inq += time[i][1];
                    time_ref += time[i][2];
                    num_persecondof_all += time[i][3];
                }
                for (int i = 0; i < threadnum; i++) {
                    num_buy += numOfmethods[i][0];
                    num_inq += numOfmethods[i][1];
                    num_ref += numOfmethods[i][2];
                }
                DecimalFormat fnum = new DecimalFormat("#0.0000000");
                double timeperbuy = (double)time_buy / (double)num_buy ;
                double timeperinq = (double)time_inq / (double)num_inq ;
                double timeperref = (double)time_ref / (double)num_ref ;
                String timeperbuystr = fnum.format(timeperbuy);
                String timeperinqstr = fnum.format(timeperinq);
                String timeperrefstr = fnum.format(timeperref);
                DecimalFormat fnum2 = new DecimalFormat("#00000000");
//                System.out.println("num of buy = " + fnum2.format(num_buy) + "    time_buy = " + fnum2.format(time_buy ) + "ms" + "  time per buy = " + timeperbuystr + "ms");
//                System.out.println("num of inq = " + fnum2.format(num_inq) + "    time_inq = " + fnum2.format(time_inq ) + "ms" + "  time per inq = " + timeperinqstr + "ms");
//                System.out.println("num of ref = " + fnum2.format(num_ref) + "    time_ref = " + fnum2.format(time_ref ) + "ms" + "  time per ref = " + timeperrefstr + "ms");
//                System.out.println("num of all = " + (num_ref + num_buy + num_inq));
//                System.out.println("all_time = " + ((time_buy) + (time_inq) + (time_ref )));
//                System.out.println("all_time = " + time_buy);
//                System.out.println("time coast = " + maintime);
                System.out.println("num per second = " + threadnum * testnum / maintime * 1000);
//                System.out.println("threadnum = " + threadnum);

                //ToDo
            }



        }

    }
}
