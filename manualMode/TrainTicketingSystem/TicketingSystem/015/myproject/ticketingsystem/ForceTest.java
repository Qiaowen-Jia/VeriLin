package ticketingsystem;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class ForceTest{
    // 参数配置
    private final static int ROUTE_NUM = 20;
    private final static int COACH_NUM = 10;
    private final static int SEAT_NUM = 100;
    private final static int STATION_NUM = 16;

    public static void main(String[] args) {
        fireTest();
    }

    static void fireTest(){
        int seatAmount = SEAT_NUM * COACH_NUM;
        TicketingDS ds = new TicketingDS(ROUTE_NUM, COACH_NUM, SEAT_NUM, STATION_NUM, seatAmount);
        BuyTest buyTest = new BuyTest(COACH_NUM, SEAT_NUM, STATION_NUM);
        boolean success = true;
        int successCount = 0, count = 0;
        buyTest.fire(ds, seatAmount/2);
        success = buyTest.verify(ds, seatAmount/2);
        if(!success){
            return;
        }
        int[] attackThread = {10, 25, 50, 100, 200, 400, 800, 1024, 2048, 4096};
        outer:for(int i=0; i < attackThread.length; i++){
            for(int j=0; j < 3; j++) {
                count++;
                InquiryModifyOverlapTest inqModTest = new InquiryModifyOverlapTest(COACH_NUM, SEAT_NUM, STATION_NUM);
                try {
                    success = inqModTest.fire(ds, attackThread[i], 10000);
                } catch (OutOfMemoryError e) {
                    break;
                }
                if (!success) {
                    break outer;
                }
                successCount++;
            }
        }
        if(success) {
            System.out.println("[Passed] Your implementation has a high probability of being linearizable");
        }
    }
}
// 用和一辆车所有座位数一样多的线程数并发购买，所有线程都应该成功
class BuyTest{
    public Ticket ticket[];
    private int coachnum;
    private int seatnum;
    private int stationnum;
    private int seatAmount;
    BuyTest(int coachnum, int seatnum, int stationnum){
        this.coachnum = coachnum;
        this.seatnum = seatnum;
        this.stationnum = stationnum;
        this.seatAmount = coachnum * seatnum;
        this.ticket = new Ticket[this.seatAmount];
    }

    public void fire(TicketingDS ds, int testAmount){
        if(testAmount > seatAmount){
            System.out.println("[Error] testAmount is greater then seatAmount");
        }
        Thread t[] = new Thread[testAmount];
        for(int i=0; i < testAmount; i++){
            t[i] = new BuyThread(i, ds);
        }
        for(int i=0; i < testAmount; i++){
            t[i].start();
        }
        for(int i=0; i < testAmount; i++){
            try{
                t[i].join();
            } catch (InterruptedException e){}
        }
    }

    public boolean verify(TicketingDS ds, int testAmount){
        boolean occupiedCheck[] = new boolean[seatAmount];
        for(int i=0; i < seatAmount; i++){
            occupiedCheck[i] = false;
        }
        for(int i=0; i < testAmount; i++){
            if(ticket[i] == null){
                System.out.println("[Failed] Some threads didn't get a ticket");
                return false;
            }
            int seatIndex = (ticket[i].coach - 1) * seatnum + (ticket[i].seat - 1);
            if(occupiedCheck[seatIndex]){
                System.out.println("[Failed] Duplicate seat");
                return false;
            }
            occupiedCheck[seatIndex]=true;
        }
        if(ds.inquiry(1, 1, stationnum) != (seatAmount - testAmount)){
            System.out.println(ds.inquiry(1, 1, stationnum));
            System.out.println("[Failed] Wrong remain ticket amount");
            return false;
        }
        System.out.println("[Passed] Force buy test passed");
        return true;
    }

    class BuyThread extends Thread{
        private int index;
        private TicketingDS ds;
        BuyThread(int index, TicketingDS ds){
            super();
            this.index = index;
            this.ds = ds;
        }

        @Override
        public void run() {
            ticket[index] = ds.buyTicket(Integer.valueOf(index).toString(), 1, 1, stationnum);
        }
    }
}

// 修改查询重叠测试
class InquiryModifyOverlapTest {
    public Ticket ticket;
    private int coachnum;
    private int seatnum;
    private int stationnum;
    private int seatAmount;

    final static int BUY_THREAD = 50000;
    final static int REFUND_THREAD = 50000;

    private int counterboard[][];

    InquiryModifyOverlapTest(int coachnum, int seatnum, int stationnum){
        this.coachnum = coachnum;
        this.seatnum = seatnum;
        this.stationnum = stationnum;
        this.seatAmount = coachnum * seatnum;
        this.counterboard = new int[stationnum+1][stationnum+1];
    }

    synchronized void buy(TicketingDS ds){
        // 买最长程
        if(ticket == null){
            ticket = ds.buyTicket("whatever",1,1, stationnum);
        }
    }

    synchronized void refund(TicketingDS ds){
        // 退最长程
        if(ticket != null){
            ds.refundTicket(ticket);
            ticket = null;
        }
    }

    public boolean fire(TicketingDS ds, int attackThreadNr, int attackNr){

        AtomicBoolean success = new AtomicBoolean(true);
        for(int departure=1; departure <= stationnum; departure++){
            for(int arrival=departure+1; arrival <= stationnum; arrival++){
                this.counterboard[departure][arrival] = ds.inquiry(1, departure, arrival);
            }
        }
        if(this.counterboard[1][stationnum] < 100){
            System.out.println("[Error] Keep at least 10 tickets for test");
            return false;
        }
        // 创建attackThreadNr个攻击线程
        Thread buyThreads[] = new Thread[attackThreadNr];
        Thread refundThreads[] = new Thread[attackThreadNr];

        for(int i=0; i<attackThreadNr; i++){
            buyThreads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    for(int i=0; i<attackNr; i++)
                        buy(ds);
                }
            });
            buyThreads[i].start();
            refundThreads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    for(int i=0; i<attackNr; i++)
                        refund(ds);
                }
            });
            refundThreads[i].start();
        }

        Random rand = new Random();
        // 创建一个查询线程
        Thread inquiryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i=0; i < attackNr << 4; i++){
                    int departure = 1;
                    int arrival = stationnum/2;
                    int result = ds.inquiry(1, departure, arrival);
                    if(result == counterboard[departure][arrival] || result == counterboard[departure][arrival]-1){
                        continue;
                    } else {
                        System.out.printf("[Failed] Inquiry result is unlinearizable (expect:%d/%d result:%d)\n",
                                counterboard[departure][arrival], counterboard[departure][arrival]-1, result);
                        success.set(false);
                    }
                }
            }
        });
        inquiryThread.start();
        for(int i=0; i<attackThreadNr; i++){
            try{
                buyThreads[i].join();
                refundThreads[i].join();
            } catch (InterruptedException e){}
        }
        try{
            inquiryThread.join();
        }catch (InterruptedException e){}
        if(success.get()){
            System.out.printf("[Passed] %d times overlap test passed\n",attackThreadNr * 2);
            return true;
        } else {
            System.out.printf("[Failed] %d times overlap test failed\n",attackThreadNr * 2);
            return false;
        }
    }
}
