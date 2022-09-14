package ticketingsystem;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Test {
    //final static int threadnum = 1;
    final static int routenum = 5;
    final static int coachnum = 8;
    final static int seatnum = 100;
    final static int stationnum = 10;

    final static int testnum = 50000;
    final static int retpc = 10;
    final static int buypc = 30;
    final static int inqpc = 100;

    final static int[] threadArray = {1, 4, 8, 16, 32, 64, 96, 128};
    static long[] buyTicketTime ,refundTime, inquiryTime;
    static long[] buyTicketCount, refundCount, inquiryCOunt;

    static AtomicInteger threadId = new AtomicInteger(0); 

    static String passengerName(){
        Random rand = new Random();
        long uid = rand.nextInt(testnum);
        return "passenger" + uid;
    }

    public static void main(String[] args) throws InterruptedException{
        for (int i = 0; i < threadArray.length; i++){
            //生成对应个数线程
            int threadnum = threadArray[i];
            //初始化线程号
            threadId.set(0);

            buyTicketTime = new long[threadnum];
            refundTime = new long[threadnum];
            inquiryTime = new long[threadnum];
            
            buyTicketCount = new long[threadnum];
            refundCount = new long[threadnum];
            inquiryCOunt = new long[threadnum];

            Thread[] threads = new Thread[threadnum];
            
            final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);

            for (int j = 0; j < threadnum; j++){
                threads[j] = new Thread(new Runnable(){
                    public void run(){
                        Random rand = new Random();
                        Ticket ticket = new Ticket();
                        ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();
                    
                        int thread_i = threadId.getAndIncrement();

                        for (int k = 0; k < testnum; k++){
                            int sel = rand.nextInt(inqpc);
                            if (0 <= sel && sel < retpc && soldTicket.size() > 0){//return ticket
                                int select = rand.nextInt(soldTicket.size());
                                if ((ticket = soldTicket.remove(select)) != null){
                                    long startTime = System.nanoTime();
                                    boolean isRefundTicket = tds.refundTicket(ticket);
                                    long endTime = System.nanoTime();
                                    refundTime[thread_i]+= endTime - startTime;
                                    refundCount[thread_i]++;
                                }
                                else
                                    System.out.println("ErrofRefund");
                            }
                            else if (retpc <= sel && sel < buypc){//buy ticket
                                String passenger = passengerName();
                                int route = rand.nextInt(routenum) + 1;
                                int departure = rand.nextInt(stationnum - 1) + 1;
                                int arrival = departure + rand.nextInt(stationnum - departure) + 1;//arrival
                                long startTime = System.nanoTime();
                                ticket = tds.buyTicket(passenger, route, departure, arrival);
                                long endTime = System.nanoTime();
                                buyTicketTime[thread_i] += endTime - startTime;
                                buyTicketCount[thread_i]++;
                                if (ticket != null)
                                    soldTicket.add(ticket);
                            }
                            else if (buypc <= sel && sel < inqpc){//inquiry ticket
                                int route = rand.nextInt(routenum) + 1;
                                int departure = rand.nextInt(stationnum - 1) + 1;
                                int arrival = departure + rand.nextInt(stationnum - departure) + 1;
                                long startTime = System.nanoTime();
                                int leftTicket = tds.inquiry(route, departure, arrival);
                                long endTime = System.nanoTime();
                                inquiryTime[thread_i] += endTime - startTime;
                                inquiryCOunt[thread_i]++;
                             }
                        }
                    }
                });
            }
            long startTime = System.nanoTime();
            for (int m = 0; m < threadnum; m++)
                threads[m].start();
            for (int m = 0; m < threadnum; m++)
                threads[m].join();
            long endTime = System.nanoTime();
            long executeTime = endTime - startTime;//总执行时间

            long buyTotalTime = sum(buyTicketTime);
            long refundTotalTime = sum(refundTime);
            long inquiryTotalTime = sum(inquiryTime);

            double buyTotalCount = sum(buyTicketCount);
            double refundTotalCount = sum(refundCount);
            double inquiryTotalCount = sum(inquiryCOunt);

            long averageBuyTime = (long)(buyTotalTime/buyTotalCount);
            long averageRefundTime = (long)(refundTotalTime/refundTotalCount);
            long averageInquiryTime = (long)(inquiryTotalTime/inquiryTotalCount);
        
            long throughOut = (long)(threadnum * testnum / ((double)executeTime / 1000000) * 1000);
            //System.out.println(executeTime);
            System.out.printf("ThreadNum: %d AverageBuyTime(ns): %d AverageRefundTime(ns): %d AverageInquiryTime(ns): %d ThroughOut(/s): %d\n", 
            threadnum, averageBuyTime, averageRefundTime, averageInquiryTime, throughOut);
        }
    }

    public static long sum(long[] array){
        long ans = 0;
        for (int i = 0; i < array.length; i++)
            ans += array[i];
        return ans;
    }
}
