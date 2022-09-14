package ticketingsystem;

import java.util.concurrent.atomic.AtomicInteger;

public class Seat {
    /*
       section用于表示列车站点区间是否被占用，
       0表示未占用，非0表示占用，
       当某座位卖出时，卖出区间及其相关区间值加1，退票时，退票区间及相关区间减1，
       相应区间在数组中的位置为(((stationnum - 1) + (stationnum - (departure - 1))) * (departure - 1) / 2) + (arrival - departure) -1,
       区间(1，2)在section[0]，区间(stationnum - 1, stationnum)在section[stationnum * (stationnum - 1) / 2 - 1]
     */
    public AtomicInteger[] section;
//    public int[] section;

    public Seat(int stationnum) {
        int capacity = stationnum * (stationnum - 1) / 2;
        section = new AtomicInteger[capacity];
//        section = new int[capacity];
        for (int i = 0; i < capacity; i++) {
            section[i] = new AtomicInteger(0);
//            section[i] = 0;
        }
    }
}
