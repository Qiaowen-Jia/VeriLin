package ticketingsystem;

import java.util.*;
import java.util.concurrent.atomic.*;

// bit map for each seat in a route
public class SeatMap {
  private int coachNum;
  private int seatNum;
  private int stationNum;
  private int totalBits;
  private int length; // how many long words in this bitmap
  private AtomicLongArray[] map;
  private int reserveBits;
  public static HashMap<Long, Integer> highestBit = new HashMap<Long, Integer>();

  public SeatMap(int coachNum, int seatNum, int stationNum) {
    this.coachNum = coachNum;
    this.seatNum = seatNum;
    this.stationNum = stationNum;
    totalBits = coachNum * seatNum;
    length = (totalBits + Long.SIZE - 1) / Long.SIZE;
    reserveBits = length * Long.SIZE - totalBits;
    map = new AtomicLongArray[stationNum - 1];
    for (int i = 0; i < stationNum - 1; i++) {
      map[i] = new AtomicLongArray(length);
      // set all reserve bits to 1
      // map[i].set(length - 1, ((long)-1) << (Long.SIZE - reserveBits));
      map[i].set(length - 1, ~(((long)-1) >>> reserveBits));
    }

    // 000...001 -> 0
    // 000...011 -> 1
    // 111...111 -> Long.SIZE-1
    for (int i = 0; i < Long.SIZE; i++)
      highestBit.put(((long)~0) >>> (Long.SIZE - 1 - i), i);
  }

  public long setBit(int i) {
    return (long)1 << i;
  }

  // public long resetBit(int i) {
  //   return ~(1 << i);
  // }

  public long setBits(int b, int e) {
    return ~((-1 << b) ^ (~(-1 << e)));
  }

  public int seat2Bit(int c, int s) {
    return (c * seatNum + s);
  }

  public void setSeat(int c, int s, int begin, int end) {
    int n = seat2Bit(c, s);
    int idx = n / Long.SIZE;
    int off = n - idx * Long.SIZE;
    for (int i = begin; i <= end; i++)
      map[i].getAndUpdate(idx, (x) -> (x | setBit(off)));
  }

  public void clearSeat(int c, int s, int begin, int end) {
    int n = seat2Bit(c, s);
    int idx = n / Long.SIZE;
    int off = n - idx * Long.SIZE;
    for (int i = begin; i <= end; i++)
      map[i].getAndUpdate(idx, (x) -> (x & ~setBit(off)));
  }

  private long[] snapshot(int intv) {
    long[] ss = new long[length];
    for (int i = 0; i < length; i++)
      ss[i] = map[intv].get(i);
    return ss;
  }

  // count how many 1 in a long type
  public int count1(long x) {
    int count = 0;
    while (x != 0) {
      x = x & (x - 1);
      count++;
    }
    return count;
  }

  public long[] getAvailableBitMap(int begin, int end) {
    long[] avail = snapshot(begin); // bit-or to find available seats
    long[] bm = new long[length];
    for (int intv = begin + 1; intv <= end; intv++) {
      bm = snapshot(intv);
      for (int i = 0; i < length; i++)
        avail[i] |= bm[i];
    }
    
    return avail;
  }

  public int findSeatNum(int begin, int end) {
    long[] avail = getAvailableBitMap(begin, end);

    // find how many 0 (i.e. available seats) in avail
    int count = 0;
    for (int i = 0; i < length; i++)
      count += count1(~avail[i]);
    return count;
  }

  public SeatInfo findASeat(int begin, int end) {
    long[] avail = getAvailableBitMap(begin, end); // find 0 in avail
    for (int i = 0; i < length; i++) {
      long l = ~avail[i]; // find 1 in l
      if (l != 0) {
        int off = highestBit.get(l ^ (l - 1));
        int n = i * Long.SIZE + off;
        int c = n / seatNum;
        int s = n - c * seatNum;
        return new SeatInfo(c + 1, s + 1);
      }
    }

    return null;
  }
}