package ticketingsystem;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class TicketingDS implements TicketingSystem {

    private int routenum = 5; /*车次总数，缺省为5个*/
    private int coachnum = 8; /*列车的车厢数目，缺省为8个*/
    private int seatnum = 100; /*每节车厢的座位数，缺省为100个*/
    private int stationnum = 10; /*每个车次经停站的数量，缺省为10个，含始发站和终点站*/
    private int threadnum = 16; /*并发购票的线程数，缺省为16个*/

    private static AtomicLong tid = new AtomicLong(1); /*车票编号，从1开始不重复*/
    private Train[] trains = null;
    private static ConcurrentHashMap<Long, String> soldTicket = new ConcurrentHashMap<Long, String>(); /*(tid, information string)*/

    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum){
        this.routenum = routenum;
        this.coachnum = coachnum;
        this.seatnum = seatnum;
        this.stationnum = stationnum;
        this.threadnum = threadnum;

        //init
        trains = new Train[routenum + 1]; //each route for train from 1 to routenum
        for(int i = 1; i <= routenum; i++){ //init each train
            Train train = new Train();
            train.setTrainID(i);
            Coach[] coaches = new Coach[coachnum + 1]; //each train has coaches from 1 to coachnum
            for(int j = 1; j <= coachnum; j++){ //init coach
                Coach coach = new Coach();
                coach.setCoachID(j);
                Seat[] seats = new Seat[seatnum + 1]; //each coaches has seats from 1 to seatnum
                for(int k = 1; k <= seatnum; k++){ //init seat
                    Seat seat = new Seat();
                    seat.setSeatID(k);
                    seat.setLock(new ReentrantLock()); //each seat can only modify by one thread
                    int[] sections = new int[stationnum + 1];
                    for(int a = 1; a <= stationnum; a++){ //flag for each seat at each section
                        sections[a] = 1;
                    }
                    seat.setSections(sections);
                    seats[k] = seat; //finish seat
                }
                coach.setSeats(seats);
                coaches[j] = coach; //finish coach
            }
            train.setCoaches(coaches); //finish coach
            int[] sections = new int[stationnum];
            for(int k = 1; k < stationnum; k++){
                sections[k] = coachnum * seatnum;
            }
            train.setSections(sections); //finish section
            train.setLock(new ReentrantLock());
            trains[i] = train; //finish train
        }
    }

    /*
     * 购票方法，即乘客passenger购买route车次从departure站到arrival站的车票1张：
     * 若购票成功，返回有效的Ticket对象；
     * 若失败（即无余票），返回无效的Ticket对象（即return null）。
     * */
    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        if(inquiry(route, departure, arrival) <= 0){ //check rest ticket
            return null;
        }

        Train train = trains[route];
        Coach[] coaches = train.getCoaches();

        for(int i = 1; i<= coachnum; i++){
            Seat[] seats = coaches[i].getSeats();
            for(int j = 1; j <= seatnum; j++){ //check each seat
                boolean hasTicket = true; // default has ticket
                for(int k = departure; k < arrival; k++){
                    if(seats[j].getSections()[k] <= 0){
                        hasTicket = false;
                        break;
                    }
                }

                if(!hasTicket){
                    continue;
                }

                try{
                    seats[j].getLock().lock();
                    int[] sections = seats[j].getSections();
                    for(int k = departure; k < arrival; k++){
                        sections[k]--;
                    }
                }
                finally {
                    seats[j].getLock().unlock();
                }

                try {
                    train.getLock().lock();
                    int[] sections = train.getSections();
                    for(int k = departure; k < arrival; k++){
                        sections[k]--;
                    }
                }
                finally {
                    train.getLock().unlock();
                }

                Ticket ticket = new Ticket();
                ticket.tid = tid.getAndIncrement();
                ticket.passenger = passenger;
                ticket.route = train.getTrainID();
                ticket.coach = coaches[i].getCoachID();
                ticket.seat = seats[j].getSeatID();
                ticket.departure = departure;
                ticket.arrival = arrival;
                soldTicket.put(ticket.tid, ticket.passenger + ticket.route + ticket.coach + ticket.seat + ticket.departure + ticket.arrival);
                return ticket;
            }
        }
        return null;
    }


    /*
     * 查询余票方法，即查询route车次从departure站到arrival站的余票数：
     * 返回从departure站到arrival站最小的余票数，可能为0
     * */
    @Override
    public int inquiry(int route, int departure, int arrival) {
        try {
            int[] sections = trains[route].getSections();
            int min = sections[departure];
            for(int i = departure; i < arrival; i++){
                if(min > sections[i]){
                    min = sections[i];
                }
            }
            return min;
        }
        catch (Exception e){
            return 0;
        }
    }

    /*
     * 退票方法：
     * 对有效的Ticket对象返回true；
     * 对错误或无效的Ticket对象返回false。
     * */
    @Override
    public boolean refundTicket(Ticket ticket) {
        if( ticket == null || !soldTicket.get(ticket.tid).equals(ticket.passenger + ticket.route + ticket.coach + ticket.seat + ticket.departure + ticket.arrival)){ //only sold ticket can be refund
            return false;
        }

        try {
            soldTicket.remove(ticket.tid); /*refund ticket*/
        }
        catch (Exception e){
            return false;
        }

        Train train = trains[ticket.route];
        Seat seat = train.getCoaches()[ticket.coach].getSeats()[ticket.seat];
        int departure = ticket.departure;
        int arrival = ticket.arrival;

        int[] sections = seat.getSections();
        for(int i = departure; i < arrival; i++){
            sections[i]++;
        }

        sections = train.getSections();
        for(int i = departure; i < arrival; i++){
            sections[i]++;
        }

        return true;
    }
}

class Seat {
    private int seatID = -1; /*座位号*/
    private int[] sections = null; /*座位标识*/
    private ReentrantLock lock = null; /*座位标识锁*/

    public int getSeatID(){
        return seatID;
    }

    public void setSeatID(int seatID){
        this.seatID = seatID;
    }

    public int[] getSections() {
        return sections;
    }

    public void setSections(int[] sections) {
        this.sections = sections;
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public void setLock(ReentrantLock lock) {
        this.lock = lock;
    }
}

class Coach {
    private int coachID = -1; /*车厢号*/
    private Seat[] seats = null; /*座位*/

    public int getCoachID() {
        return coachID;
    }

    public void setCoachID(int coachID) {
        this.coachID = coachID;
    }

    public Seat[] getSeats() {
        return seats;
    }

    public void setSeats(Seat[] seats) {
        this.seats = seats;
    }
}

class Train {
    private int trainID = -1; /*列车号*/
    private Coach[] coaches = null; /*车厢*/
    private int[] sections = null; /*乘车区间*/
    private ReentrantLock lock = null; /*乘车区间锁*/

    public int getTrainID() {
        return trainID;
    }

    public void setTrainID(int trainID) {
        this.trainID = trainID;
    }

    public Coach[] getCoaches() {
        return coaches;
    }

    public void setCoaches(Coach[] coaches) {
        this.coaches = coaches;
    }

    public int[] getSections() {
        return sections;
    }

    public void setSections(int[] sections) {
        this.sections = sections;
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public void setLock(ReentrantLock lock) {
        this.lock = lock;
    }
}
