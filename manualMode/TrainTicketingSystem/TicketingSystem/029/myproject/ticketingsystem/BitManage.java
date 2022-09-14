package ticketingsystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
//这里借鉴了关于快速定位0，1比特的算法
public class BitManage {
    private static final int intSize = Integer.SIZE;
    private static int[] bitList;
    private static HashMap<Integer, Integer> bitIndexMap;
    static {
        bitIndexMap = new HashMap<>(intSize);
        bitList = new int[intSize];
        for (int i = 0; i < intSize; ++i) {
            bitList[i] = (int) (1 << i);
            bitIndexMap.put(bitList[i], i);
        }
    }

    public static int setOne(int d, int i) {
        return d | bitList[i];
    }

    public static int setZero(int d, int i) {
        return d & (~bitList[i]);
    }

    public static int setRange(int d, int s, int e) {
        for (int i = s; i < e; ++i)
            d = setOne(d, i);
        return d;
    }

    public static int resetRange(int d, int s, int e){
        for (int i = s; i < e; i++)
            d = setZero(d, i);
        return d;
    }

    public static int countOnes(int d) {
        int count = 0;
        while (d != 0) {
            d = d & (d - 1);
            ++count;
        }
        return count;
    }

    public static List<Integer> locateOnes(int d) {
        ArrayList<Integer> location = new ArrayList<>(intSize);
        while (d != 0) {
            int tmp = d & (d - 1);
            location.add(bitIndexMap.get(d ^ tmp));
            d = tmp;
        }
        return location;
    }

    public static List<Integer> locateZeros(int d) {
        return locateOnes(~d);
    }

    public static int countZeros(int d) {
        return countOnes(~d);
    }


    public static boolean isBitZero(int d, int i){
        d = d & bitList[i];
        if (d == 0){
            return true;
        }
        return false;
    }

    public static boolean isRangeZero(int d, int s, int e){
        for (int i = s; i < e; i++) {
            if(!isBitZero(d, i))
                return false;
        }
        return true;
    }
}