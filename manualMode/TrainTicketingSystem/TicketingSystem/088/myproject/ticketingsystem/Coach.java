 

package ticketingsystem;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

class CoachIdAndSeatId {
    public int coachId;
    public int seatId;
    
    public CoachIdAndSeatId(int _caochId, int _seatId) {
        
        this.coachId = _caochId;
        this.seatId = _seatId;
    }
}

public class Coach {
    private final int coachId;
    private final int countOfSeat;
    private Seat[] allSeat;
    
    public Coach(final int coachId, final int countOfSeat) {
        
        // If there are more than 33 stations need to be operated,
        // we will used "Seat.stateOfMuchPeace" instead "Seat.stateOfPeace",
        // instead of operations to "Seat.stateOfPeace" are faster.
        this.coachId = coachId;
        this.countOfSeat = countOfSeat;
        this.allSeat = new Seat[countOfSeat];
        
        for (int i = 0; i < this.countOfSeat; i++) {
            this.allSeat[i] = new Seat(i + 1);
        }
    }
    
    public int checkFreeSeat(final int departure, final int arrival) {
        
        int freeCount = 0;
        for (int i = 0; i < this.countOfSeat; i++) {
            if (this.allSeat[i].checkState(departure, arrival)) {
                freeCount++;
            }
        }
        return freeCount;
    }
    
    public CoachIdAndSeatId trySeal(final int departure,
        final int arrival) {
        
        int _seatId = -1;
        CoachIdAndSeatId result = null;   

        int i = 0;
        int j = ThreadLocalRandom.current().nextInt(this.countOfSeat);
        
        while (i < this.countOfSeat) {
            _seatId = this.allSeat[j].trySealTick(departure, arrival);
            if (_seatId > 0) {
                result = new CoachIdAndSeatId(this.coachId, _seatId);
                break;
            }
            
            i++;
            j = (j + 1) % this.countOfSeat;
        }
        
        return result;
    }
    
    public boolean tryRefund(final int departure,
        final int arrival, final int seatId) {
            
        return this.allSeat[seatId - 1]
            .tryRefundTick(departure, arrival);
    }
}