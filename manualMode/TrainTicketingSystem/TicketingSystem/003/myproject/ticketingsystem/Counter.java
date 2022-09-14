package ticketingsystem;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.LongAdder;

public class Counter {
    private LongAdder counter;
    private AtomicIntegerArray slot;
    private static int range;
    final static int EMPTY = 0, INCREASE = 1, DECREASE = 2, BUSY = 3;
    Counter(int value, int range1) {
        range = range1;
        counter = new LongAdder();
        counter.add(value);
        this.slot = new AtomicIntegerArray(range);
    }

    public int get(){
        return this.counter.intValue();
    }
    
    public boolean increment(int nanos){
        long timeBound = System.nanoTime() + nanos;
        int nth = ThreadLocalRandom.current().nextInt(range);
        while (true) {
            if (System.nanoTime() > timeBound){
                this.counter.increment();
                return true;
            }
            int status = slot.get(nth);
            switch (status) {
                case EMPTY:
                    if (slot.compareAndSet(nth, status, INCREASE)) {
                        while (System.nanoTime() < timeBound) {
                            status = slot.get(nth);
                            if (status == BUSY) {
                                slot.set(nth, EMPTY);
                                return true;
                            }
                        }
                        if (slot.compareAndSet(nth, INCREASE, EMPTY)) {
                            this.counter.increment();
                            return true;
                        } else {
                            slot.set(nth, EMPTY);
                            return false;
                        }
                    }
                    break;
                case INCREASE:
                    nth = ThreadLocalRandom.current().nextInt(range);
                    break;
                case DECREASE:
                    this.slot.compareAndSet(nth, DECREASE, BUSY);
                    return false;
            }
        }
    }

    public boolean decrement(int nanos) {
        long timeBound = System.nanoTime() + nanos;
        int nth = ThreadLocalRandom.current().nextInt(range);
        while (true) {
            if (System.nanoTime() > timeBound) {
                this.counter.decrement();
                return true;
            }
            int status = slot.get(nth);
            switch (status) {
                case EMPTY:
                    if (slot.compareAndSet(nth, status, DECREASE)) {
                        while (System.nanoTime() < timeBound) {
                            status = slot.get(nth);
                            if (status == BUSY) {
                                slot.set(nth, EMPTY);
                                return true;
                            }
                        }
                        if (slot.compareAndSet(nth, DECREASE, EMPTY)) {
                            this.counter.decrement();
                            return false;
                        } else {
                            slot.set(nth, EMPTY);
                            return true;
                        }
                    } else {
                        nth = ThreadLocalRandom.current().nextInt(range);
                    }
                    break;
                case INCREASE:
                    this.slot.compareAndSet(nth, INCREASE, BUSY);
                    return false;
                case DECREASE:
                    nth = ThreadLocalRandom.current().nextInt(range);
                    break;
            }
        }
    }
}
