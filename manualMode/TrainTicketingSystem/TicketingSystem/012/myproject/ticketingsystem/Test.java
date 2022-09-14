package ticketingsystem;

import org.junit.jupiter.api.TestInfo;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class Test{
    static TicketingDS tds;//购票系统
    static int routenum=5,coachnum=10,seatnum=100,stationnum=10;//缺省的参数
    static double[] evaluation;//线程汇总执行时间、吞吐率用的数组
    static int[] invoketimes;
    static ReentrantLock evalock;//汇总时用的锁
    public static void main (String[] args) {
        int threadnum = 2;
        evaluation = new double[5];//查询、购票、退票方法平均时间、总吞吐率，已完成任务的线程数
        invoketimes = new int[5];
        Arrays.fill(evaluation, 0);
        Arrays.fill(invoketimes, 0);
        evalock = new ReentrantLock();

        for (int k = 0; k < 6; k++) {
            tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
            threadnum = threadnum << 1;
            Thread[] tArray = new Thread[threadnum];

            for (int i = 0; i < threadnum; i++) {
                tArray[i] = new Thread(new client(tds, threadnum, routenum, stationnum, evaluation, invoketimes, evalock));
                tArray[i].start();
            }
            for (int i = 0; i < threadnum; i++) {
                try {
                    tArray[i].join();//等待全部子线程任务结束
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("threadnum is " + threadnum);
            System.out.println("everage time: inqury " + String.format("%.2f", evaluation[0]) + "us,buy " + String.format("%.2f", evaluation[1]) + "us, refund "
                    + String.format("%.2f", evaluation[2]) + "us;\nThroughput rate: " + String.format("%.2f", evaluation[3]) + " tasks per second");
            System.out.println("invoketimes:inquiry " + invoketimes[0] + " ,total buy " + invoketimes[1] + " successly buy " + invoketimes[3]
                    + " ,total refund " + invoketimes[2] + " successly refund " + invoketimes[4]);
        }
    }
}
class client implements Runnable{
    TicketingDS tds;
    ArrayList<Ticket> mytickets;
    int threadsnum;
    int routenum,stationnum;
    ReentrantLock el;
    double[] evaluation;
    int[] invoketimes;//0查询次数，1购票次数，2退票次数，3购票成功次数，4退票成功次数
    public client(TicketingDS tds,int tnum,int routenum,int stationnum,double[] evaluation,int[] invoketimes,ReentrantLock evalock){
        this.invoketimes=invoketimes;
        this.tds=tds;
        this.evaluation=evaluation;
        this.el=evalock;
        mytickets= new ArrayList<>();
        this.threadsnum =tnum;this.routenum=routenum;this.stationnum=stationnum;
    }
    @Override
    public void run() {
        Random r1=new Random();

        double[] durations=new double[3];
        Arrays.fill(durations,0);
        int [] invokecounts=new int[5];
        for(int i=0;i<10000;i++){
            int nextAction=r1.nextInt(10);
            if(nextAction==0){
                if(mytickets.size()!=0){
                    //退票
                    Ticket tic=mytickets.remove(r1.nextInt(mytickets.size()));
                    long startTime = System.nanoTime()/1000;
                    if(tds.refundTicket(tic))
                        invokecounts[4]++;
                    else{
                        System.out.println("refund ticket failed!");
                    }
                    invokecounts[2]++;
                    durations[2]+=System.nanoTime()/1000-startTime;
                }

            }
            else{
                int r=r1.nextInt(this.routenum)+1;
                int d=r1.nextInt(this.stationnum-1)+1;
                int a=d+r1.nextInt(this.stationnum-d)+1;
                if(nextAction<7){
                    //查询
                    long startTime = System.nanoTime()/1000;
                    durations[0]+=System.nanoTime()/1000-startTime;
                    invokecounts[0]++;
                }
                else {
                    //买票
                    int pid=r1.nextInt(1000);
                    long startTime = System.nanoTime()/1000;
                    Ticket t = tds.buyTicket("passenger"+pid, r, d, a);
                    durations[1]+=System.nanoTime()/1000-startTime;
                    invokecounts[1]++;
                    if(t!=null){
                        mytickets.add(t);
                        invokecounts[3]++;
                        //System.out.println(this.name + " got ticket:" + t.tid);
                    }
                }
            }
        }

        double tp=(invokecounts[0]+invokecounts[1]+invokecounts[2])*1000*1000/(durations[0]+durations[1]+durations[2]);
        for(int i=0;i<3;i++){
            durations[i]=durations[i]/invokecounts[i];//以ns秒计时
        }
        el.lock();
        evaluation[0]+=durations[0]/this.threadsnum;
        evaluation[1]+=durations[1]/this.threadsnum;
        evaluation[2]+=durations[2]/this.threadsnum;
        evaluation[4]+=1;
        evaluation[3]+=tp;
        invoketimes[0]+=invokecounts[0];
        invoketimes[1]+=invokecounts[1];
        invoketimes[2]+=invokecounts[2];
        invoketimes[3]+=invokecounts[3];
        invoketimes[4]+=invokecounts[4];
        el.unlock();
    }
}
