package ticketingsystem;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author: fnbory
 * @Date: 4/1/2020 下午 8:52
 */
public class SeatNode {

    private final int seatId;
    private AtomicLong availableSeat;

    public SeatNode(final int seatId) {
        this.seatId = seatId;
        this.availableSeat = new AtomicLong(0);
    }

    public int trySealTic(final int departure, final int arrival) {
        long oldAvailSeat = 0;
        long newAvailSeat = 0;
        long temp = 0;
        // 利用数位来辨别座位是否可以买，比如要买出发站是5，到达站是9，111110000
        for (int i = departure-1; i < arrival-1; i++) {
            long pow = 1;
            pow = pow << i;
            temp |= pow;
        }
        // 利用CAS实现Lock free，不断循环如果有票的话总是可以买到的
        do {
            // 得到之前买过的票
            oldAvailSeat = this.availableSeat.get();//读
            // 比如当前可以买的座位是000001111，那么跟我们要买的区间不冲突，代表可以买。接着CAS更新
            // CAS成功的话退出do while代表买票成功，没有别的线程干扰；失败的话继续执行循环
            long result = temp & oldAvailSeat;
            //  result ！=0说明想买的票跟之前买过的票冲突了，直接返回-1代表失败
            if (result != 0) {
                return -1;
            }
            else {
                // 更新买过的票
                newAvailSeat = temp | oldAvailSeat;
            }
        } while (!this.availableSeat.compareAndSet(oldAvailSeat, newAvailSeat));//cas试图写

        return this.seatId;
    }

    public int inquiryTic(final int departure, final int arrival) {
        long oldAvailSeat = this.availableSeat.get();
        long temp = 0;
        long pow;
        // 构造要查的票的数位表示
        for (int i = departure-1; i < arrival-1; i++) {
            pow = 1;
            pow = pow << i;
            temp |= pow;
        }
        // 比如已售的票为000011111，要查的是000000001，此时result！=0，返回0代表卖光了，否则返回1
        long result = temp & oldAvailSeat;

        return (result == 0) ? 1 : 0;

    }

    public boolean tryRefundTic(final int departure, final int arrival) {
        long oldAvailSeat = 0;
        long newAvailSeat = 0;
        long temp = 0;

        for (int i = departure-1; i < arrival-1; i++) {
            long pow = 1;
            pow = pow << i;
            temp |= pow;
        }
        // 比如要退的票是000001111，取反111110000
        temp = ~temp;
        // 还是采用CAS实现Lock free
        do {
            // 得到已经卖出的所有票的数位表示，比如是000011111，我们退了刚那张票后，变成000010000
            oldAvailSeat = this.availableSeat.get();
            newAvailSeat = temp & oldAvailSeat;
        } while (!this.availableSeat.compareAndSet(oldAvailSeat, newAvailSeat));
        // 一直CAS循环退票总是可以成功的，最后返回true
        return true;
    }

}
