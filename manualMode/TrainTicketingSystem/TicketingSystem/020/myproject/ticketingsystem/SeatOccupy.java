package ticketingsystem;

import java.util.Arrays;

public class SeatOccupy {
    // TODO: 2019/12/22 单元测试
    private final static long MASK = 0xFFFFFFFFFFFFFFFFL;
    private long[] state;

    private SeatOccupy() {
    }

    SeatOccupy(int stationNum) {
        state = new long[(int) Math.ceil(stationNum / 64.0)];  // 终点站没人买
        Arrays.fill(state, 0);
    }

    SeatOccupy copy() {
        SeatOccupy seatOccupy = new SeatOccupy();
        seatOccupy.state = Arrays.copyOf(this.state, this.state.length);    // 不会修改链表上的内容，因此复制不需要加锁
        return seatOccupy;
    }

    boolean notOccupied(int start, int end) {
        int startIndex = start >> 6;
        int endIndex = end >> 6;
        if (endIndex == startIndex) {
            return getInternalValue(state[startIndex], start & 0x3F, end & 0x3F) == 0L;
        }
        if (getInternalValue(state[startIndex], start & 0x3F, 63) != 0L) {
            return false;
        }
        for (int i = startIndex + 1; i <= endIndex - 1; i++) {
            if (state[i] != 0L) {
                return false;
            }
        }
        return getInternalValue(state[endIndex], 0, end & 0x3F) == 0L;
    }

    void occupy(int start, int end) {
        int startIndex = start >> 6;
        int endIndex = end >> 6;
        if (endIndex == startIndex) {
            state[startIndex] = setInternalValue(state[startIndex], start & 0x3F, end & 0x3F);
            return;
        }
        state[startIndex] = setInternalValue(state[startIndex], start & 0x3F, 63);
        for (int i = startIndex + 1; i <= endIndex - 1; i++) {
            state[i] = MASK;
        }
        state[endIndex] = setInternalValue(state[endIndex], 0, end & 0x3F);
    }

    void free(int start, int end) {
        int startIndex = start >> 6;
        int endIndex = end >> 6;
        if (endIndex == startIndex) {
            state[startIndex] = clearInternalValue(state[startIndex], start & 0x3F, end & 0x3F);
            return;
        }
        state[startIndex] = clearInternalValue(state[startIndex], start & 0x3F, 63);
        for (int i = startIndex + 1; i <= endIndex - 1; i++) {
            state[i] = 0L;
        }
        state[endIndex] = clearInternalValue(state[endIndex], 0, end & 0x3F);
    }

    int get(int i) {
        return (int) ((state[i >> 6] >> (i & 0x3F)) & 1);
    }

    private long getInternalValue(long value, int lowBit, int highBit) {
        return (value >> lowBit) & (~((MASK << (highBit - lowBit + 1))));
    }

    private long setInternalValue(long value, int lowBit, int highBit) {
        return value | ((~(MASK << (highBit - lowBit + 1))) << lowBit);
    }

    private long clearInternalValue(long value, int lowBit, int highBit) {
        return value & (~((~(MASK << (highBit - lowBit + 1))) << lowBit));
    }

    void internalValueTest() {
        long x = 0xF123456789ABCDEFL;
        System.out.printf("0x%X\n", clearInternalValue(x, 20, 51));
        System.out.printf("0x%X\n", setInternalValue(x, 20, 51));
        System.out.printf("0x%X\n", getInternalValue(x, 20, 51));
    }

    public static void main(String[] args) {
        long x = 0xF123456789ABCDEFL;
        SeatOccupy seatOccupy = new SeatOccupy(64);
        seatOccupy.internalValueTest();
    }
}
