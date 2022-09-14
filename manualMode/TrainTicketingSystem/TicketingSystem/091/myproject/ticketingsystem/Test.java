package ticketingsystem;



import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.*;

/**
 * @Author:毛翔宇
 * @Date 2019-12-16 21:22
 * 多线程访问测试程序
 * 采用线程池，每个线程执行方法调用若干次(tasknum)
 * 计算总吞吐量
 */
public class Test {

    public static class Task implements Runnable{
        static int id = 0;
        TicketingDS ts;
        int routeNum;
        int stationNum;
        int iterNum;
        Random random;
        ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();
        public Task(int num, TicketingDS ts, int routeNum, int stationNum){
            id++;
            this.routeNum = routeNum;
            this.stationNum = stationNum;
            this.iterNum = num;
            random = new Random();
            this.ts = ts;
        }
        public void run(){
            //System.out.println("Thread ID:"+Thread.currentThread().getId());
            for (int i = 0;i< iterNum; i++){
                int seed = random.nextInt(100);
                if (seed>=20){ //查询操作
                    int route = random.nextInt(routeNum)+1;
                    int departure = random.nextInt(stationNum-1)+1;
                    int arrive = departure + random.nextInt(stationNum-departure)+1;
                    ts.inquiry(route,departure,arrive);
                }

                else if (seed>=5){ //购票操作
                    String passenger = "passenger"+ id + i;
                    int route = random.nextInt(routeNum)+1;
                    int departure = random.nextInt(stationNum-1)+1;
                    int arrive = departure + random.nextInt(stationNum-departure)+1;
                    Ticket ticket = ts.buyTicket(passenger,route,departure,arrive);
                    if (ticket != null)
                        soldTicket.add(ticket);
                }

                else{ //退票
                    if (soldTicket.size() >0) {
                        int index = random.nextInt(soldTicket.size());
                        Ticket ticket = soldTicket.remove(index);
                        if (ticket != null)
                            ts.refundTicket(ticket);
                    }
                }
            }
        }
    }
    public static void main(String[] args) {
        int routeNum = 20, coachNum = 15, seatNum = 100, stationNum = 10;
        int[] threadNums = {8, 16, 32, 64, 96};
        int testNum = 100000;
        for (int iter = 0; iter < threadNums.length; iter++) {
            System.out.println(threadNums[iter]+"次线程执行结果：");
            int threadNum = threadNums[iter];
            //MockTicketDS mtd = new MockTicketDS(0);
            TicketingDS ticketingDS = new TicketingDS(routeNum, coachNum, seatNum, stationNum, threadNum);
            ExecutorService es = Executors.newFixedThreadPool(96);
            long start = System.currentTimeMillis();
            for (int i = 0; i < threadNum; i++) {
                Task myTask = new Task(testNum, ticketingDS,routeNum,stationNum);
                es.execute(myTask);
            }
            es.shutdown();
            try {
                while (!es.awaitTermination(10, TimeUnit.SECONDS)) ;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            double elapsed = (System.currentTimeMillis() - start) / 1000.0;
            double through = (threadNum * testNum) / elapsed;
            System.out.println("total run time(s):" + elapsed + "\n" + "output through:" + through);
        }
    }

}
