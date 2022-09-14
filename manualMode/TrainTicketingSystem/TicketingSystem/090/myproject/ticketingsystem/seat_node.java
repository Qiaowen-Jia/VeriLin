package ticketingsystem;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

class seat_node {
  private final int seat_id;
  private AtomicLong have_seat;

  public seat_node(final int seat_id) {
    this.seat_id = seat_id;
    this.have_seat = new AtomicLong(0);
  }

  public int inquiryTic(final int departure, final int arrival) {
    long old = this.have_seat.get();
    long temp = 0;

    for (int i = departure-1; i < arrival-1; i++) {
      temp |= (1 << i);
    } 
    long result = temp & old;

    return (result == 0) ? 1 : 0;
  }

  public int trySealTic(final int departure, final int arrival) {
    long old = 0;
    long newseat = 0;
    long temp = 0;

    for (int i = departure-1; i < arrival-1; i++) {
      temp |= (1 << i);
    } 

    do {
      old = this.have_seat.get();
      long result = temp & old;
      if (result != 0) return -1;
      else newseat = temp | old;
    } while (!this.have_seat.compareAndSet(old, newseat));

    return this.seat_id;
  }

  
  public boolean tryRefundTic(final int departure, final int arrival) {
    long old = 0;
    long newseat = 0;
    long temp = 0;

    for (int i = departure-1; i < arrival-1; i++) {
      temp |= (1 << i);
    } 
    temp = ~temp;
    do {
      old = this.have_seat.get();
      newseat = temp & old;
    } while (!this.have_seat.compareAndSet(old, newseat));

    return true;
  }
}