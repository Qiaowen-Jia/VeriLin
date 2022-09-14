package ticketingsystem;

//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


public class Test_latency {
    
    final static int threadnum = 16; // concurrent thread number
    final static int routenum = 10; // route is designed from 1 to 3
    final static int coachnum = 24; // coach is arranged from 1 to 5
    final static int seatnum = 200; // seat is allocated from 1 to 20
    final static int stationnum = 60; // station is designed from 1 to 5

    final static int testnum = 10000;
    final static int retpc = 10; // return ticket operation is 10% percent
    final static int buypc = 40; // buy ticket operation is 30% percent
    final static int inqpc = 100; //inquiry ticket operation is 60% percent
    
    final static String passenger = "passenger";
    

    public static void main(String[] args) throws InterruptedException {

        long start_time, end_time;
        double elapsed;
        int ops;
        double throughput;
        
        System.out.println("==================== Test 2 start ====================");
        System.out.println("route: " + routenum + ", coach: " + coachnum + ", seat: " + seatnum +
                ", #stations: " + stationnum);
        
        for (int t = 1; t <= 512; t *= 2) {
            final Thread[] threads = new Thread[t];
            for (int i = 1; i < 10; i += 2) {
                final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, t);
                start_time = System.currentTimeMillis();
                test(tds, threads);
                end_time = System.currentTimeMillis();
                elapsed = (end_time - start_time) / 1000.0;
                ops = testnum * i * t;
                throughput = ops / elapsed;
                System.out.printf("#threads: %3d, #ops/thread: %2dw, #operations: %3dw, time: %fs, throughtput: %.2f ops/s\n",
                        t, testnum * i / 10000, ops / 10000, elapsed, throughput);
            }
        }
        System.out.println("=================== Test 2 finished ===================");
        
    }
    
    private static void smallTest(final TicketingDS tds) {
        Random rand = new Random();
        Ticket ticket = new Ticket();
        ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();
        for (int i = 0; i < 1000; i++) {
            int departure = rand.nextInt(9) + 1;
            int arrival = departure + rand.nextInt(10 - departure) + 1;
            if((ticket = tds.buyTicket(passenger, 1, departure, arrival)) != null)
                soldTicket.add(ticket);
        }
        for (Ticket t : soldTicket) {
            if(t == null)
                System.out.println("ErrOfRefund1");
            if(!tds.refundTicket(t))
                System.out.println("ErrOfRefund2");
        }
    }
    
    private static void buyTest(final TicketingDS tds, final Thread[] threads) {
        for (int i = 0; i< threads.length; i++) {
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    Random rand = new Random();
                    Ticket ticket = new Ticket();
                    ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();
                    //System.out.println(ThreadId2.get());
                    for (int i = 0; i < testnum; i++) {
                        int route = rand.nextInt(routenum) + 1;
                        int departure = rand.nextInt(stationnum - 1) + 1;
                        int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
                        if ((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null) 
                            soldTicket.add(ticket);
                    }
                }
            });
              threads[i].start();
        }
    
        for (int i = 0; i< threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
    }
    
    
    private static void queryTest(final TicketingDS tds, final Thread[] threads) {
        for (int i = 0; i< threads.length; i++) {
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    Random rand = new Random();                    
                    //System.out.println(ThreadId2.get());
                    for (int i = 0; i < testnum; i++) {
                        int route = rand.nextInt(routenum) + 1;
                        int departure = rand.nextInt(stationnum - 1) + 1;
                        int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
                        tds.inquiry(route, departure, arrival);
                    }
                }
            });
              threads[i].start();
        }
        for (int i = 0; i< threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    private static void buyAndRefundTest(final TicketingDS tds, final Thread[] threads) {
        for (int i = 0; i< threads.length; i++) {
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    Random rand = new Random();
                    Ticket ticket = new Ticket();
                    ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();
                    //System.out.println(ThreadId2.get());
                    for (int i = 0; i < testnum; i++) {
                        int sel = rand.nextInt(inqpc);
                        if (sel % 2 == 0 && soldTicket.size() > 0) { // return ticket
                            int select = rand.nextInt(soldTicket.size());
                            if ((ticket = soldTicket.remove(select)) != null) {
                                if (tds.refundTicket(ticket)) {
                                } else {
                                    System.out.println("ErrOfRefund1");
                                    System.out.println(ticket.tid + " " + ticket.passenger + 
                                            " route:" + ticket.route + " coach:" + ticket.coach + 
                                            " from:" + ticket.departure + " to:" + ticket.arrival + 
                                            " seat:" + ticket.seat);
                                    System.out.flush();
                                }
                            } else {
                                System.out.println("ErrOfRefund2");
                                System.out.flush();
                            }
                        } else { // buy ticket
                            int route = rand.nextInt(routenum) + 1;
                            int departure = rand.nextInt(stationnum - 1) + 1;
                            int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
                            if ((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null) 
                                soldTicket.add(ticket);
                        } 
                    }
                }
            });
              threads[i].start();
        }
    
        for (int i = 0; i< threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
    }
  
    private static void buyAndQueryTest(final TicketingDS tds, final Thread[] threads) {
        for (int i = 0; i< threads.length; i++) {
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    Random rand = new Random();
                    Ticket ticket = new Ticket();
                    ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();
                    
                    //System.out.println(ThreadId2.get());
                    for (int i = 0; i < testnum; i++) {
                        int sel = rand.nextInt(inqpc);
                        if (sel % 2 == 0) { // buy ticket
                            int route = rand.nextInt(routenum) + 1;
                            int departure = rand.nextInt(stationnum - 1) + 1;
                            int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
                            if ((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null) 
                                soldTicket.add(ticket);
                        } else if (buypc <= sel && sel < inqpc) { // inquiry ticket
                            int route = rand.nextInt(routenum) + 1;
                            int departure = rand.nextInt(stationnum - 1) + 1;
                            int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
                            tds.inquiry(route, departure, arrival);
                        }
                    }
                }
            });
              threads[i].start();
        }
    
        for (int i = 0; i< threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
    }

    private static void test(final TicketingDS tds, final Thread[] threads) {
        for (int i = 0; i< threads.length; i++) {
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    long start_time, end_time;
                    double elapsed;
//                    int magic_count = 0;
                    Random rand = new Random();
                    Ticket ticket = new Ticket();
                    ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();

                    start_time = System.currentTimeMillis();
                    
                    //System.out.println(ThreadId2.get());
                    for (int i = 0; i < testnum; i++) {
                        int sel = rand.nextInt(inqpc);
                        if (0 <= sel && sel < retpc && soldTicket.size() > 0) { // return ticket
                            int select = rand.nextInt(soldTicket.size());
                        if ((ticket = soldTicket.remove(select)) != null) {
                                if (tds.refundTicket(ticket)) {
                                } else {
                                    System.out.println("ErrOfRefund1");
                                    System.out.println(ticket.tid + " " + ticket.passenger + 
                                            " route:" + ticket.route + " coach:" + ticket.coach + 
                                            " from:" + ticket.departure + " to:" + ticket.arrival + 
                                            " seat:" + ticket.seat);
                                    System.out.flush();
                                }
                            } else {
                                System.out.println("ErrOfRefund2");
                                System.out.flush();
                            }
                        } else if (retpc <= sel && sel < buypc) { // buy ticket
                            int route = rand.nextInt(routenum) + 1;
                            int departure = rand.nextInt(stationnum - 1) + 1;
                            int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
                            if ((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null) 
                                soldTicket.add(ticket);
//                            else
//                                magic_count++;
                        } else if (buypc <= sel && sel < inqpc) { // inquiry ticket
                            int route = rand.nextInt(routenum) + 1;
                            int departure = rand.nextInt(stationnum - 1) + 1;
                            int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
                            tds.inquiry(route, departure, arrival);
                        }
                    }
//                    if (magic_count > 2500) System.out.println(magic_count);
                    end_time = System.currentTimeMillis();
                    elapsed = (end_time - start_time) / 1.0;
                    double latency = elapsed / testnum;
                    System.out.printf("latency: %f ms/op\n", latency);
                }
            });
              threads[i].start();
        }
    
        for (int i = 0; i< threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
    }
}
