package ticketingsystem;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TicketingDS implements TicketingSystem {
    private Route[] routes;
    //用于记录某个线程购买的车票
    public static final ThreadLocal<Set<Long>> tickets = ThreadLocal.withInitial(HashSet::new);
    //用于记录某个线程的购票次数
    private static final ThreadLocal<Integer> cnt = ThreadLocal.withInitial(() -> 1);

    TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
        routes = new Route[routenum + 1];
        for (int i = 1; i <= routenum; i++) {
            routes[i] = new Route(1, stationnum, i, coachnum, seatnum);
        }
    }

    @Override
    public Ticket buyTicket(String passenger, int routeID, int departure, int arrival) {
        Route route = routes[routeID];
        State[][] states = route.states;
        //错开买的车厢, 提高吞吐量
        int hash = Thread.currentThread().hashCode();
        int begin = hash % route.coachNum, cur = begin;
        //购票的目标区间, 在循环占座的过程中不会变
        State targetInterval = states[departure][arrival];
        do {
            ConcurrentHashMap<Integer, Boolean> map = targetInterval.coach[cur];
            //从当前车厢开始尝试所有车厢内的座位
            for (Map.Entry<Integer, Boolean> entry : map.entrySet()) {
                int seat = entry.getKey();
                if (map.get(seat) == null)
                    //在foreach循环中, entrySet是一份拷贝
                    //也就是说当前循环的键值对可能已经不在map真正的entrySet里
                    //所以这里要先判断一下是否在当前map里, 如果不在就不要浪费时间去循环了
                    continue;
                /* 从各个区间跟目标区间有交集的区间的map中占领该座位
                 * 比如想占领3->6的1号座位, 一共有8个站
                 * 那么
                 * 1->4, 1->5, 1->6, 1->7, 1->8
                 * 2->4, 2->5, 2->6, 2->7, 2->8
                 * 3->4, 3->5, 3->6, 3->7, 3->8
                 * 4->5, 4->6, 4->7, 4->8
                 * 5->6, 5->7, 5->8
                 * 以上这些区间的1号座位都要尝试去占领
                 */
                boolean flag = true;
                Try: for (int i = 1; i < arrival; i++) {
                    for (int j = Math.max(departure, i) + 1; j <= route.stationNum; j++) {
                        State s = states[i][j];
                        ConcurrentHashMap<Integer, Boolean> m = s.coach[cur];
                        if (m.remove(seat) == null) {
                            //目标座位已经被其他线程从这个map中删除了, 占座失败
                            flag = false;
                            //复原之前区间的状态
                            recovery(routeID, departure, arrival, cur , seat, i, j);
                            break Try;  //换一个座位重新尝试
                        } else {
                            //占座成功
                            int prev = s.left.getAndDecrement();
                            if (prev < 0) {
                                Singleton.getInstance().errorMsg(
                                        "\n*******************************" +
                                        "\nseat less than 0: " +
                                        "\nrouteID: " + routeID +
                                        "\ncount: " + prev +
                                        "\nfrom: " + i + " to: " + j);
                            }
                        }
                    }
                }
                if (flag) {
                    return ticketGenerator(passenger, routeID, departure, arrival, cur, seat);
                }
            }
            //这节车厢的所有座位都尝试过了, 换下一节车厢试试
            cur = (cur + 1) % route.coachNum;
        } while (cur != begin);
        //所有车厢遍历完毕, 买不到座位
        return null;
    }

    /**
     * 复原从departure到arrival之间所有区间目标座位的状态
     * @param routeID 列车ID
     * @param departure 始发站
     * @param arrival 终点站
     * @param targetSeat 目标座位
     * @param failedStart 如果是占座调用的, 则表示占座失败区间的始发站; 如果是退票调用的则为-1
     * @param failedEnd 如果是占座调用的, 则表示占座失败区间的终点站; 如果是退票调用的则为-1
     */
    private void recovery(int routeID, int departure, int arrival, int targetCoach,
                          int targetSeat, int failedStart, int failedEnd) {
        Route route = routes[routeID];
        State[][] states = route.states;
        for (int i = 1; i < arrival; i++) {
            for (int j = Math.max(departure, i) + 1; j <= route.stationNum; j++) {
                State s = states[i][j];
                ConcurrentHashMap<Integer, Boolean> map = s.coach[targetCoach];
                if (i == failedStart && j == failedEnd)
                    //占座调用的recovery, 失败区间之前的区间全部复原完毕, 直接退出
                    return;
                Boolean flag = map.put(targetSeat, true);
                if (flag != null) {
                    //不知道为啥被删除的座位会出现这个map里
                    Singleton.getInstance().errorMsg(
                            "\n*******************************" +
                            "\nseat should not appear: " +
                            "\nrouteID: " + routeID +
                            "\ncoach: "+ (targetCoach+1) +
                            "\nseat: " + targetSeat +
                            "\nfrom: " + i + " to: " + j +
                            "\nstate: " + flag);
                }
                int prev = s.left.getAndIncrement();
                if (prev > route.total_seat) {
                    Singleton.getInstance().errorMsg(
                            "\n*******************************" +
                            "\nseat greater than seatNum: " +
                            "\nrouteID: " + routeID +
                            "\ncount: " + prev + ", limit: " + route.total_seat +
                            "\nfrom: " + i + " to: " + j);
                }
            }
        }
    }

    @Override
    public int inquiry(int routeID, int departure, int arrival) {
        Route route = routes[routeID];
        State s = route.states[departure][arrival];
        return s.left.get();
    }

    @Override
    public boolean refundTicket(Ticket ticket) {
        if (ticket == null) {
            return false;
        } else {
            //判断是否重复退同一张票
            Set<Long> set = tickets.get();
            boolean isSuccessful = set.remove(ticket.tid);
            if (isSuccessful) {
                //恢复相应区间的座位空闲情况, 剩余座位数
                //票的coach从1开始计数, 代码中是从0开始计数, 所以要-1
                recovery(ticket.route, ticket.departure, ticket.arrival,
                        ticket.coach - 1, ticket.seat, -1, -1);
            }
            return isSuccessful;
        }
    }

    //生成车票
    private Ticket ticketGenerator(String passenger, int routeID, int departure,
                                   int arrival, int coach, int seat) {
        //将当前线程的购票次数和线程ID拼接在一起当tid
        int prev = cnt.get();
        cnt.set(prev + 1);
        long tid = getTid(prev);
        Ticket ticket = new Ticket();
        ticket.tid = tid;
        ticket.passenger = passenger;
        ticket.route = routeID;
        ticket.coach = coach + 1; //代码中coach从0开始计数, 所以要+1
        ticket.seat = seat;
        ticket.departure = departure;
        ticket.arrival = arrival;
        Set<Long> set = tickets.get();
        set.add(tid);
        return ticket;
    }

    private long getTid(int n) {
        //例如: threadID = 14, n = 27, 那么返回结果就位1427
        long tid = Thread.currentThread().getId();
        int tmp = n;
        while (tmp >= 1) {
            tmp /= 10;
            tid *= 10;
        }
        return tid + n;
    }
}