package ticketingsystem;

import ticketingsystem.BitManage;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicIntegerArray;

class Route {
    private AtomicIntegerArray array;
    private static final int intSize = Integer.SIZE;
    private int size;

    public Route(int size) {
        this.size = size;
        int arraySize = (size + intSize - 1) / intSize;
        array = new AtomicIntegerArray(arraySize);
        int remainSize = arraySize * intSize - size;
        int begin = intSize - remainSize;
        int old = array.get(array.length() - 1);
        array.set(array.length() - 1, BitManage.setRange(old, begin, intSize));
    }

    public static int elementSize() {
        return intSize;
    }

    public void set(int index) {
        int arrayIndex = index / intSize;
        int i = index % intSize;
        while (true) {
            int oldValue = array.get(arrayIndex);
            int newValue = BitManage.setOne(oldValue, i);
            if (array.compareAndSet(arrayIndex, oldValue, newValue))
                break;
        }
    }

    public void reset(int index) {
        int arrayIndex = index / intSize;
        int i = index % intSize;
        while (true) {
            int oldValue = array.get(arrayIndex);
            int newValue = BitManage.setZero(oldValue, i);
            if (array.compareAndSet(arrayIndex, oldValue, newValue))
                break;
        }
    }

    public int[] snapshot() {       //快照
        int[] res = new int[array.length()];
        for (int i = 0; i < res.length; ++i)
            res[i] = array.get(i);
        return res;
    }

}