package ticketingsystem;

import java.util.*;
import java.util.Date;
    
public class Test {
	public static void main(String[] args) throws InterruptedException {
    final int[] threadnums = {4, 8, 16, 32, 64};
    final int routenum = 5;
    final int coachnum = 8;
    final int seatnum = 100;
    final int stationnum = 10;
    
    final int testnum = 10000;
    final int refpc = 10;//10%退票操作
    final int buypc = 40;//30%购票操作
    final int inqpc = 100;//60%查票操作
    
    System.out.println("Test: routenum: " + routenum +
      " coachnum: " + coachnum +
      " seatnum: " + seatnum +
      " stationnum: " + stationnum +
      " testnum: " + testnum + "/thread");
    
    int length = threadnums.length;
    for (int j = 0; j < length; j++) {//分别测试线程数4,8,16,32,64
      int threadnum = threadnums[j];
      Thread[] threads = new Thread[threadnum];
      
      final TicketingDS tds = new TicketingDS(
          routenum, coachnum, seatnum, stationnum, threadnum);
          
      long startTime = System.currentTimeMillis();//开始时间
      
      for (int i = 0; i < threadnum; i++) {
          threads[i] = new Thread(new Runnable() {
              public void run() {
                  Random rand = new Random();
                  Ticket ticket = new Ticket();
                  ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();//缓存已购车票信息
                  
                  for (int i = 0; i < testnum; i++) {
                    int sel = rand.nextInt(inqpc);

                    if (0 <= sel && sel < refpc && soldTicket.size() > 0) {//10%退票操作
                      int select = rand.nextInt(soldTicket.size());//从已购车票中随机选一张退票
                      ticket = soldTicket.remove(select);
                      if (ticket != null) {
                          if (tds.refundTicket(ticket)) {
                          } 
                          else {}
                      } 
                      else {}
                    } 
                    else if (refpc <= sel && sel < buypc) {//30%买票操作
                      String passenger = "wzq";
                      int route = rand.nextInt(routenum) + 1;//routenum从1开始
                      int departure = rand.nextInt(stationnum - 1) + 1;//出发station最大为stationnum-1
                      int arrival = departure
                          + rand.nextInt(stationnum - departure) + 1;
                      ticket = tds.buyTicket(
                          passenger, route, departure, arrival);
                      if (ticket != null) {
                          soldTicket.add(ticket);
                      } 
                      else {}
                    } 
                    else if (buypc <= sel && sel < inqpc) {//60%查询操作
                      int route = rand.nextInt(routenum) + 1;
                      int departure = rand.nextInt(stationnum - 1) + 1;
                      int arrival = departure
                          + rand.nextInt(stationnum - departure) + 1;
                      int AvailTicketNum = tds.inquiry(route, departure, arrival);
                    }
                  }
              }
          });
          threads[i].start();//线程并发工作
      }
      //同步每个线程
      for (int i = 0; i < threadnum; i++) {
          threads[i].join();
      }
      //结束时间
      long endTime = System.currentTimeMillis();
      double timeused = (endTime - startTime) / 1000.0;
      System.out.format("ThreadNum: %3d TimeUsed: %3.3f s Throughout: %.3f%n", threadnum, timeused, (threadnum * testnum / timeused));
    }                 
	}
}
