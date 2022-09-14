package ticketingsystem;

import java.util.ArrayList;
import java.util.Random;

public class Test {
  final static int threadnum = 64; // concurrent thread number
  final static int routenum = 20; // route is designed from 1 to 3
  final static int coachnum = 20; // coach is arranged from 1 to 5
  final static int seatnum = 100; // seat is allocated from 1 to 20
  final static int stationnum = 15; // station is designed from 1 to 5
  final static int testnum = 640000;
  final static int retpc = 10; // return ticket operation is 10% percent
  final static int buypc = 40; // buy ticket operation is 30% percent
  final static int inqpc = 100; //inquiry ticket operation is 60% percent

  static String passengerName() {
    Random rand = new Random();
    long uid = rand.nextInt(testnum);
    return "passenger_" + uid;
  }

  public static void main(String[] args) throws InterruptedException {
    Thread[] threads = new Thread[threadnum];
    final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
    long startTime =  System.nanoTime();
    
    for (int i = 0; i< threadnum; i++) {
      threads[i] = new Thread(new Runnable() {
        public void run() {
          long startTime, endTime;
          Random rand = new Random();
          Ticket ticket = new Ticket();
          ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();

          for (int i = 0; i < testnum; i++) {
            int sel = rand.nextInt(inqpc);
            if (0 <= sel && sel < retpc && soldTicket.size() > 0) { // return ticket
              int select = rand.nextInt(soldTicket.size());
              if ((ticket = soldTicket.remove(select)) != null) {
                if (!tds.refundTicket(ticket))
                  System.out.println("ErrOfRefund");
              } else {
                  System.out.println("ErrOfRefund");
              }
            } else if (retpc <= sel && sel < buypc) { // buy ticket
              String passenger = passengerName();
              int route = rand.nextInt(routenum) + 1;
              int departure = rand.nextInt(stationnum - 1) + 1;
              int arrival = departure + rand.nextInt(stationnum - departure) + 1;
              if ((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null) {
                soldTicket.add(ticket);
              }
            } else if (buypc <= sel && sel < inqpc) { // inquiry ticket
              int route = rand.nextInt(routenum) + 1;
              int departure = rand.nextInt(stationnum - 1) + 1;
              int arrival = departure + rand.nextInt(stationnum - departure) + 1;
              int leftTicket = tds.inquiry(route, departure, arrival);
            }
          }
        }
      });
      threads[i].start();
    }

    for (int i = 0; i< threadnum; i++) {
      threads[i].join();
    }

    long endTime =  System.nanoTime();
    System.out.println("Used Time(s): "  + (double)(endTime-startTime)/1000000000L);
  }
}
