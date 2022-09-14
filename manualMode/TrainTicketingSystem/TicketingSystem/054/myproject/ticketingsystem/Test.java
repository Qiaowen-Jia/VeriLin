package ticketingsystem;

import java.util.*;
import java.util.Date;

public class Test {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Basic function test:");
        TicketingDS basic = new TicketingDS(1, 2, 5, 8, 4);
        System.out.println(basic.inquiry(1,1,8));
        Thread[] bthread = new Thread[5];
        ArrayList<Ticket> TicketToSold = new ArrayList<Ticket>();
        for(int i=0;i<5;i++){
            int finalI = i;
            bthread[i] = new Thread(new Runnable() {
                public void run() {
                    if(finalI<4){
                        for(int j=0;j<4;j++){
                            Ticket ticket = basic.buyTicket("huzi",1, finalI +1,finalI +2+j);
                            if(ticket != null){
                                System.out.println("Thread " + finalI + " get ticket " + ticket.tid + " from " + (finalI+1) +
                                        " to " + (finalI + 2 + j) + " coach " + ticket.coach + " seat " + ticket.seat );
                                TicketToSold.add(ticket);
                            }
                            else{
                                System.out.println("Thread " + finalI + " buy ticket from " + (finalI+1) + " to " + (finalI + 2 + j)
                                        +" failed!");
                                System.out.println("No ticket form " + (finalI+1) + " to " + (finalI + 2 + j) + " have " +
                                        basic.inquiry(1,finalI+1,finalI + 2 + j)
                                        + " ticket left!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                            }
                        }
                    }
                    else{
                        for(int j=0;j<6;j++){
                            System.out.println("Inquiry ticket form " + (j+1) + " to " + (j+3) + " have " + basic.inquiry(1,j+1,j+3)
                            + " ticket left!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                            System.out.println("Inquiry ticket form " + (j+1) + " to " + (j+3) + " have " + basic.inquiry(1,j+1,j+3)
                                    + " ticket left!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        }
                    }
                }
            });
            bthread[i].start();
        }

        for (int i = 0; i < 5; i++) {
            bthread[i].join();
        }

        //System.out.println("Number: " + basic.getnum() );
        System.out.println("\n\n\n\n\n\n\n");
        for(int i=0;i<4;i++) {
            int finalI = i;
            Random rand = new Random();
            bthread[i] = new Thread(new Runnable() {
                public void run() {
                    if(finalI<3){
                        for(int j=1;j<5;j++){
                            Ticket ticket = basic.buyTicket("huzi",1, finalI +1,finalI +2+j);
                            if(ticket != null) {
                                System.out.println("Thread " + finalI + " get ticket " + ticket.tid + " from " + (finalI + 1) +
                                        " to " + (finalI + 2 + j) + " coach " + ticket.coach + " seat " + ticket.seat);
                                TicketToSold.add(ticket);
                            }
                            else{
                                System.out.println("Thread " + finalI + " buy ticket from " + (finalI+1) + " to " + (finalI + 2 + j)
                                        +" failed!");
                                System.out.println("No ticket form " + (finalI+1) + " to " + (finalI + 2 + j) + " have " +
                                        basic.inquiry(1,finalI+1,finalI + 2 + j)
                                        + " ticket left!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                            }
                        }
                    }
                    else{
                        for(int j=0;j<6;j++){
                            Ticket sold = TicketToSold.remove(rand.nextInt(TicketToSold.size()));
                            if(basic.refundTicket(sold)){
                                System.out.println("Thread " + finalI + " sold ticket " + sold.tid + " from " + sold.departure +
                                        " to " + sold.arrival + " coach " + sold.coach + " seat " + sold.seat);
                            }
                            else{
                                System.out.println("Error???????????????? sold " + sold.tid);
                            }
                        }
                    }
                }
            });
            bthread[i].start();
        }

        for (int i = 0; i < 4; i++) {
            bthread[i].join();
        }

        final int[] threadnums = {96,96,96,96,96};
        final int routenum = 20;
        final int coachnum = 15;
        final int seatnum = 100;
        final int stationnum = 10;

        final int testnum = 500000;
        final int refpc = 5;
        final int buypc = 20;
        final int inqpc = 100;

        //assert (false);

        System.out.println("Test: routenum: " + routenum +
                " coachnum: " + coachnum +
                " seatnum: " + seatnum +
                " stationnum: " + stationnum +
                " testnum: " + testnum + "/thread");

        int length = threadnums.length;
        for (int j = 0; j < length; j++) {
            int threadnum = threadnums[j];
            Thread[] threads = new Thread[threadnum];

            final TicketingDS tds = new TicketingDS(
                    routenum, coachnum, seatnum, stationnum, threadnum);

            long startTime = System.currentTimeMillis();

            for (int i = 0; i < threadnum; i++) {
                threads[i] = new Thread(new Runnable() {
                    public void run() {
                        Random rand = new Random();
                        Ticket ticket = new Ticket();
                        ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();

                        for (int i = 0; i < testnum; i++) {
                            int sel = rand.nextInt(inqpc);
                            if (0 <= sel && sel < refpc && soldTicket.size() > 0) {
                                int select = rand.nextInt(soldTicket.size());
                                ticket = soldTicket.remove(select);
                                if (ticket != null) {
                                    if (tds.refundTicket(ticket)) {
                                    }
                                    else {}
                                }
                                else {}
                            }
                            else if (refpc <= sel && sel < buypc) {
                                String passenger = "john";
                                int route = rand.nextInt(routenum) + 1;
                                int departure = rand.nextInt(stationnum - 1) + 1;
                                int arrival = departure
                                        + rand.nextInt(stationnum - departure) + 1;
                                ticket = tds.buyTicket(
                                        passenger, route, departure, arrival);
                                if (ticket != null) {
                                    soldTicket.add(ticket);
                                }
                                else {}
                            }
                            else if (buypc <= sel && sel < inqpc) {
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
            double timeused = (endTime - startTime) / 1000.0;
            System.out.format("CountOfThreads: %3d TimeUsed: %3.2fs Throughout: %.2f%n", threadnum, timeused, (threadnum * testnum / timeused));
        }

    }
}