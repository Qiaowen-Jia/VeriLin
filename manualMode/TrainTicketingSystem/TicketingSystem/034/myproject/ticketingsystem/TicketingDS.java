package ticketingsystem;

// import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

// import org.graalvm.compiler.nodes.PrefetchAllocateNode;

// import java.util.concurrent.atomic.AtomicInteger;

public class TicketingDS implements TicketingSystem {
    private int routenum;
    private int coachnum;
    private int seatnum;
    private int stationnum;
    private int threadnum;// 这个字段没有任何用
    private int max_one;// 记录一趟车的最大多票
    private Route[] data;

    static AtomicLong tid = new AtomicLong();
    // static int passenger_id = 0; // 乘客计数编码

    // 乘客名字字符串与id的映射
    // private ConcurrentHashMap<Integer, String> name2id;
    public TicketingDS() {
        this.coachnum = 8;
        this.routenum = 5;
        this.seatnum = 100;
        this.stationnum = 10;
        this.threadnum = 16;

        tid.set(0);
        init_db();
    }

    /**
     * @param routenum   车次总数 缺省 5个
     * @param coachnum   车厢总数 缺省 8个
     * @param seatnum    每节车厢座位总数 缺省 100个
     * @param stationnum 数组，长度为为车次数目 每个车次经停车站总数 缺省10个
     * @param threadnum  并发购票数量 缺省16个
     */
    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
        this.routenum = routenum;
        this.coachnum = coachnum;
        this.seatnum = seatnum;
        this.stationnum = stationnum;
        this.threadnum = threadnum;
        this.max_one = this.coachnum * this.seatnum;

        tid.set(0);
        init_db();
    }

    private Boolean init_db() {
        // this.db = new TicketDb(this.coachnum*this.seatnum, this.routenum,
        // this.stationnum);
        data = new Route[this.routenum];
        for (int i = 0; i < this.routenum; i++) {
            data[i] = new Route(this.coachnum * this.seatnum);
            // data[i].init(this.coachnum*this.seatnum, this.stationnum-1);
            // data[i].init(this.coachnum * this.seatnum);
        }
        return false;
    }

    /**
     * 是购票方法，即乘客passenger购买route车次从departure站到arrival站的车票1张 简化起见 车站编号从1开始
     * 
     * @param passenger 乘客名
     * @param route     哪个车次
     * @param departure 始发站 从1开始编号
     * @param arrival   终点站 1开始编号
     * @return 成功返回Ticket对象，失败返回NULL
     */
    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        // int pid = passenger.hashCode();
        // 传入的route是从1开始编码的所以减一
        // 这里pid可能为-1导致出错

        int seat = data[route - 1].purchase(departure, arrival);
        if (seat == -1) {
            return null;
        }
        Ticket tmp = new Ticket();
        tmp.arrival = arrival;
        tmp.departure = departure;
        tmp.route = route;
        tmp.passenger = passenger;
        tmp.tid = tid.incrementAndGet();
        // 注意返回的seat是一趟车的整体编号
        tmp.seat = seat % this.seatnum +1 ; //1开始编码 ，修正
        tmp.coach = seat / this.seatnum +1;//1开始编码 ，修正

        return tmp;
    }

    /**
     * 查询余票方法，即查询route车次从departure站到arrival站的余票数
     * 
     * @return 错误的话返回-1
     */
    @Override
    public int inquiry(int route, int departure, int arrival) {
        // return db.is_remain(route-1, departure, arrival);
        if (route > this.routenum || route < 0 || departure < 0 || arrival > this.stationnum) {
            return -1;// 出错
        }
        return data[route - 1].remain_query(departure, arrival);
    }

    /**
     * 退票方法
     * 
     * @param ticket 对象
     * @return 对有效的Ticket对象返回true，对错误或无效的Ticket对象返回false
     */
    @Override
    public boolean refundTicket(Ticket ticket) {
        // int seat = (ticket.coach -1) * this.coachnum + (ticket.seat-1);//1开始编码的修正  
        int seat = (ticket.coach -1) * this.seatnum + (ticket.seat-1);//1开始编码的修正
        // 去掉参数验证没必要
        // return db.refund(ticket.route-1, seat, ticket.departure, ticket.arrival);
        return data[ticket.route - 1].refund(seat, ticket.departure, ticket.arrival);
    }
}

class Route {
    // 每个车次单独保存
    // private boolean locked;//为了实现线性一致的长期锁
    private int max_seat;
    // 二维数组 第一维seat
    // private AtomicInteger[] table;
    private AtomicLong[] table;
/**
 * 构造方法
 * @param mm 最大座位数量 
 */
    public Route(int mm) {
        // this.locked = false;//开始不上锁
        this.max_seat = mm;
        // this.station = ss;
        this.table = new AtomicLong[mm];
        for (int i = 0; i < mm; i++) {
            this.table[i] = new AtomicLong();
            this.table[i].set(0);// 默认0
        }
        // return 0;
    }

    /**
     * 返回可以购票的最小座位号
     * 
     * @param departure 起始站，车站从1开始编码的
     * @param arrival   到达站
     * @return 该趟车从departure到arrival站余票数
     */
    int remain_query(int departure, int arrival) {
        // this.locked = true;
        long count = 0;
        long mask = getMask(departure, arrival);
        long curr_mask;
        // 一种错误情况是在遍历时某个已经检验的座位被占了？
        // 这只能整个加锁了啊，
        for (int i = 0; i < this.max_seat; i++) {
            curr_mask = this.table[i].get();
            if ((curr_mask & mask) == 0) {
                count++;
            }
        }
        // this.locked = false;

        return (int)count;
    }

    /**
     * 购票
     * 
     * @param departure 起始站
     * @param arrival   大道站
     * @return 座位号正确，-1错误
     */
    int purchase(int departure, int arrival) {
        // while(this.locked); //空转等待
        long mask = getMask(departure, arrival);
        long curr, result;
        for (int i = 0; i < this.max_seat; i++) {
            do {
                curr = table[i].get();
                //检查是否购票冲突
                if ((curr & mask) != 0) {
                    break;
                }
                result = mask | curr;
                if (table[i].compareAndSet(curr,result )) {// 短路机制
                    // System.out.printf("%d->%d %d\n",departure,arrival,mask);
                    return i;
                } 
            } while (true);
            // 循环继续条件是本来可以写入但中途table[i]被改写了，抢占了那么重新测试
            // 如果测试没法写入了退出循环
        }

        return -1; // 错误情况
    }

    /**
     * 退票是
     * 
     * @param seat      座位号
     * @param departure 起始站
     * @param arrival   出发站
     * @return 是否退票成功
     */
    Boolean refund(int seat, int departure, int arrival) {
        // while(this.locked); //空转等待
        // if(arrival > station || departure <=0)
        // return false;
        long mask = getMask(departure, arrival);
        // 将departure到arrival清理 异或运算
        long curr,result;// = table[seat].get();
        do
        {
            curr = table[seat].get();
            if ((curr | mask) != curr) {
                return false;
            }
            result  = curr ^ mask;
            if(table[seat].compareAndSet(curr,result))
            {
                return true;
            }      
            //重新循环表示有人在退票重新检查     
            // curr = mask ^ table[seat].get();
            // mask = table[seat].get();
            // if (table[seat].compareAndSet(mask, curr)) {
            //     return true;
            // }
        }while(true);

    }

    private final long getMask(int start, int end) // 变成了inline方法
    {
        //编码修正
        start--;
        end -= 1;
        // 第start到第end位的二进制置为1，其他0
        // 0开始编码的 ，，
        // return ((1<<end) - (1<<start) ) | 1<<(start-1);
        return ((1 << end) - (1 << start));
    }
}
