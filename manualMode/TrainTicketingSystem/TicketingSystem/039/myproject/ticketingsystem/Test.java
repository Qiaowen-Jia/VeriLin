package ticketingsystem;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Test {

  final static int routeNum = 5;
  final static int coachNum = 8;
  final static int seatNum = 100;
  final static int stationNum = 10;

  final static int testNum = 10000;
  final static int threadNum = 128;
  final static int refpc = 10; // refund ticket operation is 10%
  final static int buypc = 40; // buy ticket operation is 30%
  final static int inqpc = 100; // inquiry ticket operation is 60%

  final static long[] refTime = new long[threadNum];
  final static long[] buyTime = new long[threadNum];
  final static long[] inqTime = new long[threadNum];

  final static long[] refCount = new long[threadNum];
  final static long[] buyCount = new long[threadNum];
  final static long[] inqCount = new long[threadNum];

  final static AtomicInteger threadId = new AtomicInteger(0);

  static String passengerName() {
    Random rand = new Random();
    long uid = rand.nextInt(testNum);
    return "passenger" + uid;
  }

  public static void main(String[] args) throws InterruptedException {

    final int[] threadNums = {4, 8, 16, 32, 64, 128};
    final TicketingDS[] tdsArray = new TicketingDS[threadNums.length];
    for (int t = 0; t < threadNums.length; t++) {
      tdsArray[t] = new TicketingDS(routeNum, coachNum, seatNum, stationNum, threadNums[t]);
    }

    for (int t = 0; t < threadNums.length; t++) {
      TicketingDS tds = tdsArray[t];

      Thread[] threads = new Thread[threadNums[t]];

      for (int i = 0; i < threadNums[t]; i++) {
        threads[i] = new Thread(new Runnable() {
          public void run() {
            Random rand = new Random();
            Ticket ticket = new Ticket();
            ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();
            int id = threadId.getAndIncrement();

            for(int j = 0; j < testNum; j++) {
              int sel = rand.nextInt(inqpc);
              if (0 <= sel && sel < refpc && soldTicket.size() > 0) { // refund ticket
                int select = rand.nextInt(soldTicket.size());
                if ((ticket = soldTicket.remove(select)) != null) {
                  long stt = System.nanoTime();
                  tds.refundTicket(ticket);
                  long end = System.nanoTime();
                  refTime[id] += end - stt;
                  refCount[id] ++;
                } else {
                  System.out.println("ErrOfRefund");
                }
              } else if (refpc <= sel && sel < buypc) { // buy ticket
                String passenger = passengerName();
                int route = rand.nextInt(routeNum) + 1;
                int departure = rand.nextInt(stationNum - 1) + 1;
                int arrival = departure + rand.nextInt(stationNum - departure) + 1;
                long stt = System.nanoTime();
                ticket = tds.buyTicket(passenger, route, departure, arrival);
                long end = System.nanoTime();
                buyTime[id] += end - stt;
                buyCount[id] ++;
                if (ticket != null)
                  soldTicket.add(ticket);
              } else if (buypc <= sel && sel < inqpc) { // inquiry ticket
                int route = rand.nextInt(routeNum) + 1;
                int departure = rand.nextInt(stationNum - 1) + 1;
                int arrival = departure + rand.nextInt(stationNum - departure) + 1;
                long stt = System.nanoTime();
                tds.inquiry(route, departure, arrival);
                long end = System.nanoTime();
                inqTime[id] += end - stt;
                inqCount[id] ++;
              }
            }
          }
        });
      }

      long time1 = System.nanoTime();
      for (int i = 0; i < threadNums[t]; i++)
        threads[i].start();
      for (int i = 0; i < threadNums[t]; i++)
        threads[i].join();
      long time2 = System.nanoTime();
      long timeTotal = time2 - time1;

      long refTimeTotal = sum(refTime, threadNums[t]);
      long buyTimeTotal = sum(buyTime, threadNums[t]);
      long inqTimeTotal = sum(inqTime, threadNums[t]);

      long refCountTotal = sum(refCount, threadNums[t]);
      long buyCountTotal = sum(buyCount, threadNums[t]);
      long inqCountTotal = sum(inqCount, threadNums[t]);

      long refAvgTime = (long) (refTimeTotal / (double) refCountTotal);
      long buyAvgTime = (long) (buyTimeTotal / (double) buyCountTotal);
      long inqAvgTime = (long) (inqTimeTotal / (double) inqCountTotal);

      long throughput = (long) ((threadNums[t] * testNum / (double) timeTotal) * 1000000000);

      System.out.printf("%d threads\trefundAvgTime(ns) %d\tbuyAvgTime(ns) %d\tinquiryAvgTime(ns) %d\tthroughput(/s) %d\n",
        threadNums[t], refAvgTime, buyAvgTime, inqAvgTime, throughput);

      long[][] arrays = {refTime, buyTime, inqTime, refCount, buyCount, inqCount};
      for (int i = 0; i < arrays.length; i++)
        for (int j = 0; j < arrays[i].length; j++)
          arrays[i][j] = 0;

      threadId.set(0);

    }
  }

  private static long sum(long[] array, int length) {
    long sum = 0;
    for (int i = 0; i < length; i++)
      sum += array[i];
    return sum;
  }
}
