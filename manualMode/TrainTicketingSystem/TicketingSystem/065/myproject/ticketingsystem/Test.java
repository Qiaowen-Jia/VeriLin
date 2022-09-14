package ticketingsystem;
import java.util.*;
public class Test 
{
    ////*
    final static int routenum = 20; // route is designed from 1 to 5
    final static int coachnum = 15; // coach is arranged from 1 to 8
    final static int seatnum = 100; // seat is allocated from 1 to 100
    final static int stationnum = 10; // station is designed from 1 to 4

    final static int testnum = 500000;
    final static int retpc = 5; // return ticket operation is 10% percent
    final static int buypc = 20; // buy ticket operation is 30% percent
    final static int inqpc = 100; //inquiry ticket operation is 60% percent
    ///*/
    /*
    final static int routenum = 5; // route is designed from 1 to 5
    final static int coachnum = 8; // coach is arranged from 1 to 8
    final static int seatnum = 100; // seat is allocated from 1 to 100
    final static int stationnum = 10; // station is designed from 1 to 4

    final static int testnum = 500000;
    final static int retpc = 5; // return ticket operation is 10% percent
    final static int buypc = 20; // buy ticket operation is 30% percent
    final static int inqpc = 100; //inquiry ticket operation is 60% percent
    */
    static String passengerName() 
    {
        Random rand = new Random();
        long uid = rand.nextInt(testnum);
        return "passenger" + uid; 
    }

    public static void main(String[] args) throws InterruptedException 
    {
        //打印参数
        System.out.println("********************");
        System.out.println("车次:"+routenum);
        System.out.println("车厢数:"+coachnum);
        System.out.println("每节车厢座位数量:"+seatnum);
        System.out.println("车站数量:"+stationnum);
        System.out.println("每个线程执行次数:"+testnum);
        System.out.println("查询、购票和退票比例:  "+(inqpc-buypc)+":"+(buypc-retpc)+":"+retpc);
        System.out.println("********************");
        System.out.flush();  

        final int[] threadnumbers={4,8,16,32,64,96};
        for(int turn=0;turn<6;turn++)
        {
            int threadnum = threadnumbers[turn];
            Thread[] threads = new Thread[threadnum];
            final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
            //开始一次计时
            double start_time = System.currentTimeMillis();
            //operation
            for (int i = 0; i< threadnum; i++) 
            {
                threads[i] = new Thread(new Runnable() 
                {
                    public void run() 
                    {
                        Random rand = new Random();
                        Ticket ticket = new Ticket();
                        ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();
                        
                        //System.out.println(ThreadId.get());
                        for (int i = 0; i < testnum; i++) 
                        {
                            int sel = rand.nextInt(inqpc);
                            // return ticket
                            if (0 <= sel && sel < retpc && soldTicket.size() > 0) 
                            {
                                int select = rand.nextInt(soldTicket.size());
                                if ((ticket = soldTicket.remove(select)) != null) 
                                {
                                    if (tds.refundTicket(ticket)) 
                                    {
                                        //System.out.println("TicketRefund" + " " + ticket.tid + " " + ticket.passenger + " " + ticket.route + " " + ticket.coach  + " " + ticket.departure + " " + ticket.arrival + " " + ticket.seat);
                                        System.out.flush();
                                    } 
                                    else 
                                    {
                                        System.out.println("ErrOfRefund");
                                        System.out.flush();
                                    }
                                } 
                                else 
                                {
                                    System.out.println("ErrOfRefund");
                                    System.out.flush();
                                }
                            }
                            // buy ticket 
                            else if (retpc <= sel && sel < buypc) 
                            {
                                String passenger = passengerName();
                                int route = rand.nextInt(routenum) + 1;
                                int departure = rand.nextInt(stationnum - 1) + 1;
                                int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
                                if ((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null) 
                                {
                                    soldTicket.add(ticket);
                                    //System.out.println("TicketBought" + " " + ticket.tid + " " + ticket.passenger + " " + ticket.route + " " + ticket.coach + " " + ticket.departure + " " + ticket.arrival + " " + ticket.seat);
                                    System.out.flush();
                                } 
                                else 
                                {
                                    //System.out.println("TicketSoldOut" + " " + route+ " " + departure+ " " + arrival);
                                    System.out.flush();
                                }
                            }
                            // inquiry ticket 
                            else if (buypc <= sel && sel < inqpc) 
                            {
                                int route = rand.nextInt(routenum) + 1;
                                int departure = rand.nextInt(stationnum - 1) + 1;
                                int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
                                int leftTicket = tds.inquiry(route, departure, arrival);
                                //System.out.println("RemainTicket" + " " + leftTicket + " " + route+ " " + departure+ " " + arrival);
                                System.out.flush();              
                            }
                        }
                    }
                });
                threads[i].start();
            }
            //join threads
            for (int i = 0; i< threadnum; i++) 
            {
                threads[i].join();
            }
            //结束一次计时
            double end_time = System.currentTimeMillis();
            //calculate
            System.out.println("-----------------------------------------------");
            System.out.println("线程数："+threadnum);
            double meantime = (end_time-start_time)/testnum/threadnum ;
            System.out.println("每种方法调用的平均执行时间："+meantime);
            double throughput = 1/meantime ;
            System.out.println("系统的总吞吐率(单位时间内完成的方法调用总数)："+throughput);
            System.out.flush();
            //next threadnum
        }
    }
}
