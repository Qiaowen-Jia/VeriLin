package ticketingsystem;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

class Seat 
{
  private final int seat;
  private AtomicLong seat_available;

  public Seat(final int seat) 
  {
    this.seat = seat;
    this.seat_available = new AtomicLong(0);
  }

  private long station_bitset(int departure, int arrival)
  {
    long bitset = 0;
    long position = 1;
    for (int i = departure-1; i < arrival-1; i++) 
    {
      bitset |= position << i;
    }
    return bitset;
  }

  public int buyt(final int departure, final int arrival) 
  {
    long history_seat_available = 0;
    long current_seat_available = 0;
    long bitset = station_bitset(departure, arrival);
    long result;
    do 
    {
      history_seat_available = this.seat_available.get();
      result = bitset & history_seat_available;
      if (result != 0) //有相同,买不了
      {
        return -1;
      } 
      else 
      {
        current_seat_available = bitset | history_seat_available;
      }
    } 
    while (!this.seat_available.compareAndSet(history_seat_available, current_seat_available));
    // 成功
    return this.seat;
  }

  public int inquiryt(final int departure, final int arrival) 
  {
    long history_seat_available = this.seat_available.get();
    long bitinq = station_bitset(departure, arrival);
    long result = bitinq & history_seat_available;
    return (result == 0) ? 1 : 0;
  }

  public boolean refundt(final int departure, final int arrival) 
  {
    long history_seat_available = 0;
    long current_seat_available = 0;
    long bitset = station_bitset(departure, arrival);
    long result;
    do 
    {
      history_seat_available = this.seat_available.get();
      result = bitset & history_seat_available;
      if(result != bitset) //有没买的
      {
        System.out.println("history_seat_available__error:"+history_seat_available+" bitset:"+bitset);
        System.out.flush();
        return false;
      }
      else
      {
        current_seat_available = (~bitset) & history_seat_available;
      }
    } 
    while (!this.seat_available.compareAndSet(history_seat_available, current_seat_available));
    return true;
  }

}