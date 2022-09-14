package ticketingsystem;
import java.util.*;

public class Test {
	public static void main(String[] args) throws InterruptedException {
    final int[] threadnums = {4, 8, 16, 32, 64};
    final int routenum = 20;
    final int coachnum = 10;
    final int seatnum = 100;
    final int stationnum = 16;
    
    final int testnum = 100000;
    final int refundTicket = 10;
    final int buyTicket = 20;
    final int inquiry = 70;
    
    System.out.println("=======================initialize=====================" );
    System.out.println(" routeNum: " + routenum +
                        " coachNum: " + coachnum +
                        " seatNum: " + seatnum +
                        " stationNum: " + stationnum +
                        " testNum: " + testnum );
    
    int length = threadnums.length;
    System.out.println("=======================starting=======================" );

    for (int j = 0; j < length; j++) {
      int threadnum = threadnums[j];
      Thread[] threads = new Thread[threadnum];
      
      final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
          
      long startTime = System.currentTimeMillis();
      
      for (int i = 0; i < threadnum; i++) {
          threads[i] = new Thread(new Runnable() {
              public void run() {
                  Random rand = new Random();
                  Ticket ticket = new Ticket();
                  ArrayList<Ticket> sellTicket = new ArrayList<Ticket>();
                  
                  for (int i = 0; i < testnum; i++) {
                    int sell = rand.nextInt(inquiry);
                    if (0 <= sell && sell < refundTicket && sellTicket.size() > 0) {
                      int select = rand.nextInt(sellTicket.size());
                      ticket = sellTicket.remove(select);
                      if (ticket != null) {
                          if (tds.refundTicket(ticket)) {
                          } 
                          else {}
                      } 
                      else {}
                    } 
                    else if (refundTicket <= sell && sell < buyTicket) {
                      String passenger = "ycp";
                      int route = rand.nextInt(routenum) + 1;
                      int departure = rand.nextInt(stationnum - 1) + 1;
                      int arrival = departure + rand.nextInt(stationnum - departure) + 1;
                      ticket = tds.buyTicket(
                          passenger, route, departure, arrival);
                      if (ticket != null) {
                          sellTicket.add(ticket);
                      } 
                      else {
                      }
                    } 
                    else if (buyTicket <= sell && sell < inquiry) {
                      int route = rand.nextInt(routenum) + 1;
                      int departure = rand.nextInt(stationnum - 1) + 1;
                      int arrival = departure
                          + rand.nextInt(stationnum - departure) + 1;
                      int leftTicket = tds.inquiry(route, departure, arrival);
                    }
                }
              }
          });
          threads[i].start();
      }
      for (int i = 0; i < threadnum; i++) {
          threads[i].join();
      }
      long endTime = System.currentTimeMillis();
      double costTime = (endTime - startTime) / 1000.0;//转化成s
      System.out.format("threadNum:%4d  costTime: %4.3fs  throughout: %.3f%n", threadnum, costTime, (threadnum * testnum / costTime));
    }
                    
	}
}
