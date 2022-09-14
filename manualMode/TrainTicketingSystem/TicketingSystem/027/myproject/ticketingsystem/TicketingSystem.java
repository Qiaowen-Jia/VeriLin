package ticketingsystem;

/**
 * 售票系统接口
 * @author 965087276@qq.com
 * @date 2019/12/5 10:25
 */
public interface TicketingSystem {
    /**
     * 购票
     * @param passenger 乘客名
     * @param route 车次
     * @param departure 上车站
     * @param arrival 下车站
     * @return 有票返回车票，无票返回null
     */
    Ticket buyTicket (String passenger, int route, int departure, int arrival);

    /**
     * 退票
     * @param ticket 车票
     * @return 车票合法时应返回true，其他条件返回false
     */
    boolean refundTicket (Ticket ticket);

    /**
     * 查询余票数量
     * @param route 车次
     * @param departure 上车站
     * @param arrival 下车站
     * @return 返回余票数
     */
    int inquiry(int route, int departure, int arrival);
}
