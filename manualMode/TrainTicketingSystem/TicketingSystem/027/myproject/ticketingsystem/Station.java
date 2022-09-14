package ticketingsystem;

/**
 * @author 965087276@qq.com
 * @date 2019/12/8 16:15
 */
public class Station {
    /**
     * 车站编号
     */
    private int sid;
    /**
     * 座位数量
     */
    private int seatNum;
    /**
     * 座位状态
     */
    private AtomicBitSet seatStatus;
    /**
     * 座位
     */
    private Seat[] seats;

    public boolean tryLock(int seatId) {
        return seats[seatId].tryLock();
    }

    public void lock(int seatId) {
        seats[seatId].lock();
    }

    public void unlock(int seatId) {
        seats[seatId].unlock();
    }

    public boolean isEmpty(int seatId) {
        return seats[seatId].isEmpty();
    }

    public void updateSeat(int seatId, boolean value) {
        seats[seatId].setEmpty(value);
    }

    public void updateSeatStatus(int seatId, boolean value) {
        seatStatus.set(seatId, value);
    }

    public Station(int sid, int seatNum) {
        this.sid = sid;
        this.seatNum = seatNum;
        this.seatStatus = new AtomicBitSet(seatNum);
        this.seats = new Seat[this.seatNum];
        for (int i = 0; i < this.seatNum; i++) {
            this.seats[i] = new Seat();
        }
    }

    public AtomicBitSet getSeatStatus() {
        return seatStatus;
    }

}
