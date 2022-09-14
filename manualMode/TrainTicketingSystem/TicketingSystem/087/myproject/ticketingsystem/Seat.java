

package ticketingsystem;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class Seat {
    private final int seatId;
    private AtomicInteger seat_condition;
    
    public Seat(final int seatId) {
        
        this.seatId = seatId;
        // If there are more than 33 stations need to be operated,
        // we will used "stateOfMuchPeace" instead "seat_condition",
        // instead of operations to "seat_condition" are faster.
        this.seat_condition = new AtomicInteger(0);
    }
    
    public boolean checkState(final int departure, final int arrival) {
        int expect = this.seat_condition.get();
        int temp = 0;
        for (int i = departure - 1; i < arrival - 1; i++) {
            int pow = 1;
            pow = pow << i;
            temp += pow;
        }
        int result = temp & expect;
        
        return (result != 0) ? false : true;
    }
    
    public int trySealTick(final int departure, final int arrival) {
        
        int expect = 0;
        int update = 0;
        int temp = 0;
        for (int i = departure - 1; i < arrival - 1; i++) {
            int pow = 1;
            pow = pow << i;
            temp += pow;
        }
        do {
            expect = this.seat_condition.get();
            int result = temp & expect;
            if (result != 0) {
                return -1;
            } else {
                update = temp | expect;
            }
        } while (!this.seat_condition.compareAndSet(expect, update));
        
        return this.seatId;
    }
    
    public boolean tryRefundTick(final int departure, final int arrival) {
        
        int expect = 0;
        int update = 0;
        int temp = 0;
        for (int i = departure - 1; i < arrival - 1; i++) {
            int pow = 1;
            pow = pow << i;
            temp += pow;
        }
        do {
            expect = this.seat_condition.get();
            update = (~temp) & expect;
        } while (!this.seat_condition.compareAndSet(expect, update));
        
        return true;
    }
}
    