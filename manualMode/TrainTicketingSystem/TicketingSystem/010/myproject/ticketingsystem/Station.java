package ticketingsystem;

import java.util.Arrays;

/**
 *
 * @author Jianyong Feng
 **/
public class Station {

    private final int seatNum;

    // 初始的总座位数
    private final int totalSeatNum;

    private final BitSet[] seatStatus;
    private final Object[] objects;

    public Station(int seatNum, int totalSeatNum, BitSet[] seatStatus, Object[] objects) {
        this.seatNum = seatNum;
        this.totalSeatNum = totalSeatNum;

//        this.seatStatus = bitSetArrayCopy(seatStatus);
        this.seatStatus = seatStatus;
        this.objects = objects;

        // not deep copy
//        this.seatStatus = Arrays.copyOf(seatStatus, seatStatus.length);
    }

    /**
     * 释放一张票
     * @param departure 出发站
     * @param arrival 到达站
     */
    public boolean freeOneTicket(int departure, int arrival, int coach, int seat) {
        int seatIndex = (coach - 1) * seatNum + seat;
        for (int stationIndex = departure; stationIndex < arrival; stationIndex++) {
            if (!seatStatus[stationIndex].get(seatIndex)) {
                return false;
            }
            seatStatus[stationIndex].clear(seatIndex);
        }
        return true;
    }

    public boolean freeOneSeat(int departure, int arrival, int seatIndex) {
        int wordIndex = BitSet.getWordIndex(seatIndex);
//        for (int stationIndex = departure; stationIndex < arrival; stationIndex++) {
//            if (!seatStatus[stationIndex].get(seatIndex)) {
//                return false;
//            }
//        }
        for (int stationIndex = departure; stationIndex < arrival; stationIndex++) {
            seatStatus[stationIndex].clear(seatIndex);
        }
        return true;
    }

    public boolean seatAccess(int departure, int arrival, int seatIndex) {
        for (int stationIndex = departure; stationIndex < arrival; stationIndex++) {
            if (seatStatus[stationIndex].get(seatIndex)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 分配一张票，座号为出发站至到达站车票数目的最小值
     *
     * @param departure 出发站
     * @param arrival 到达站
     * @return 分配的座位，如果票已售完，则返回-1号
     */
    public int[] allocateOneTicket(int departure, int arrival) {
        BitSet seatCopy = seatStatus[departure].clone();
        for (int i = departure + 1; i < arrival; i++) {
            seatCopy.or(seatStatus[i]);
        }
        int seatIndex = seatCopy.nextClearBit(1);
        if (seatIndex > totalSeatNum)
            return new int[]{-1, -1};
        for (int stationIndex = departure; stationIndex < arrival; stationIndex++) {
            if (seatStatus[stationIndex].get(seatIndex)) {
                return new int[]{-1, -1};
            }
            seatStatus[stationIndex].set(seatIndex);
        }
        return new int[]{(seatIndex - 1) / seatNum + 1, (seatIndex - 1) % seatNum + 1};
    }

    public boolean occupyOneSeat(int departure, int arrival, int seatIndex) {
        int wordIndex = BitSet.getWordIndex(seatIndex);
        for (int stationIndex = departure; stationIndex < arrival; stationIndex++) {
            if (seatStatus[stationIndex].get(seatIndex)) {
                return false;
            }
        }
        for (int stationIndex = departure; stationIndex < arrival; stationIndex++) {
            seatStatus[stationIndex].set(seatIndex);
        }
        return true;
    }

    public int getSeatIndex(int departure, int arrival) {
        BitSet seatCopy = seatStatus[departure].clone();
        for (int i = departure + 1; i < arrival; i++) {
            seatCopy.or(seatStatus[i]);
        }
        int seatIndex = seatCopy.nextClearBit(1);
        if (seatIndex > totalSeatNum)
            return -1;
        return seatIndex;
    }

    public int inquiryLeftTicket(int departure, int arrival) {
        BitSet seatCopy = seatStatus[departure].clone();
        for (int i = departure + 1; i < arrival; i++) {
            seatCopy.or(seatStatus[i]);
        }
        return totalSeatNum - seatCopy.cardinality();
    }

    public BitSet[] getSeatStatus() {
        return bitSetArrayCopy(seatStatus);
    }

    public BitSet[] getSeatStatus(int departure, int arrival) {
        return bitSetArrayCopy(seatStatus, departure, arrival);
    }

    public static BitSet[] bitSetArrayCopy(BitSet[] original,int start, int end) {
        BitSet[] copy = Arrays.copyOf(original, original.length);
        for (int i = start; i < end; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }

    public static BitSet[] bitSetArrayCopy(BitSet[] original) {
        BitSet[] copy = new BitSet[original.length];
        for (int i = 1; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }
}
