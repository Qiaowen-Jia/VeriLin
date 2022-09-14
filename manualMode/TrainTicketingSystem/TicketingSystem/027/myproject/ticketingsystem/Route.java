package ticketingsystem;


/**
 * @author 965087276@qq.com
 * @date 2019/12/8 16:12
 */
public class Route {
    /**
     * 车次号
     */
    private int rid;
    /**
     * 车厢数目
     */
    private int coachNum = 8;
    /**
     * 每节车厢座位数
     */
    private int seatNum = 100;
    /**
     * 经停站数目
     */
    private int stationNum = 10;
    /**
     * 车站
     */
    private Station[] stations;

    public Route (int rid, int coachNum, int seatNum, int stationNum) {
        this.rid = rid;
        this.coachNum = coachNum;
        this.seatNum = seatNum;
        this.stationNum = stationNum;
        this.stations = new Station[stationNum];
        for (int i = 0; i < stationNum; i++) {
            this.stations[i] = new Station(i, coachNum * seatNum);
        }
    }

    public int inquiry(int departure, int arrival) {
        --departure; --arrival;
        return this.countEmptySeats(departure, arrival);
    }

    public Ticket buyTicket(int departure, int arrival) {
        --departure; --arrival;
        AtomicBitSet bitSet = this.or(departure, arrival);
        Ticket ticket = null;
        int sid = 0;
        while((sid = bitSet.nextZeroBit(sid)) != -1) {
            if (this.buyTicket(sid, departure, arrival)) {
                ticket = new Ticket();
                ticket.setCoach(sid / this.seatNum + 1);
                ticket.setSeat(sid % this.seatNum + 1);
                break;
            }
            sid++;
        }
        return ticket;
    }


    public boolean refundTicket(int seatId, int departure, int arrival) {
        departure--; arrival--;
        try {
            for (int i = departure; i < arrival; i++) {
                stations[i].lock(seatId);
                stations[i].updateSeatStatus(seatId, false);
                stations[i].updateSeat(seatId, true);
            }

//            for (int i = departure; i < arrival; i++) {
//                if (stations[i].isEmpty(seatId)) {
//                    return false;
//                }
//            }
//            // 修改座位状态
//            for (int i = departure; i < arrival; i++) {
//                stations[i].updateSeat(seatId, true);
//                stations[i].updateSeatStatus(seatId, false);
//            }
            return true;
        } finally {
            for (int i = departure; i < arrival; i++) {
                stations[i].unlock(seatId);
            }
        }
    }

    private AtomicBitSet or(int departure, int arrival) {
        AtomicBitSet bitSet = new AtomicBitSet(this.coachNum * this.seatNum);
        for (int i = departure; i < arrival; i++) {
            bitSet.or(stations[i].getSeatStatus());
        }
        return bitSet;
    }

    private int countEmptySeats(int departure, int arrival) {
        AtomicBitSet bitSet = new AtomicBitSet(this.seatNum * this.coachNum);
        for (int i = departure; i < arrival; i++) {
            bitSet.or(this.stations[i].getSeatStatus());
        }
        return bitSet.cardinality();
    }

    private boolean buyTicket(int seatId, int departure, int arrival) {
//        for (int i = departure; i < arrival; i++) {
//            if (!stations[i].isEmpty(seatId)) {
//                return false;
//            }
//        }
        int end = departure - 1;
        try {
            // 锁住各站上的seatId座位
            for (int i = departure; i < arrival; i++) {
                // 座位已经被占用, return false
                if (!stations[i].isEmpty(seatId) || !stations[i].tryLock(seatId)) {
                    return false;
                }
                ++end;
                // 座位已经被占用，return false
                if (!stations[i].isEmpty(seatId)) return false;
            }
//            for (int i = departure; i < arrival; i++) {
//                // 若座位已经被占，返回
//                if (!stations[i].isEmpty(seatId)) {
//                    return false;
//                }
//            }
            // 修改座位状态
            for (int i = departure; i < arrival; i++) {
                stations[i].updateSeat(seatId, false);
                stations[i].updateSeatStatus(seatId, true);
            }
            return true;

        } finally {
            for (int i = departure; i <= end; i++) {
                stations[i].unlock(seatId);
            }
        }
    }



}
