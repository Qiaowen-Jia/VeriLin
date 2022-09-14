package ticketingsystem;

import java.util.concurrent.atomic.AtomicLong;

public class Seat {
    private final int seatId;
    private AtomicLong freeSeats;

    public Seat(final int seatId) {
        this.seatId = seatId;
        this.freeSeats = new AtomicLong(0);
    }

    public int trySellTicket(final int departure, final int arrival) {
        long oldFreeSeat = 0;
        long newFreeSeat = 0;
        long temp = 0;

        for (int i = departure-1; i < arrival-1; i++) {
            long pow = 1;
            pow = pow << i;
            temp |= pow;
        }

        do {
            oldFreeSeat = this.freeSeats.get();//读取座位情况
            long result = temp & oldFreeSeat;
            if (result != 0) {
                return -1;
            }
            else {
                newFreeSeat = temp | oldFreeSeat;
            }
        } while (!this.freeSeats.compareAndSet(oldFreeSeat, newFreeSeat));//CAS写

        return this.seatId;

    }

    public int inquiryTicket(final int departure, final int arrival) {
        long oldFreeSeat = this.freeSeats.get();
        long temp = 0;
        long pow;

        for (int i = departure-1; i < arrival-1; i++) {
            pow = 1;
            pow = pow << i;
            temp |= pow;
        }
        long result = temp & oldFreeSeat;

        return (result == 0) ? 1 : 0;

    }

    public boolean tryRefundTicket(final int departure, final int arrival) {
        long oldFreeSeat = 0;
        long newFreeSeat = 0;
        long temp = 0;

        for (int i = departure-1; i < arrival-1; i++) {
            long pow = 1;
            pow = pow << i;
            temp |= pow;
        }
        temp = ~temp;
        do {
            oldFreeSeat = this.freeSeats.get();
            newFreeSeat = temp & oldFreeSeat;
        } while (!this.freeSeats.compareAndSet(oldFreeSeat, newFreeSeat));

        return true;
    }

}
