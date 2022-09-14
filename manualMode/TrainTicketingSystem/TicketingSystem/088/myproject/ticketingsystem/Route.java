 
package ticketingsystem;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Route {
    private final int routeId;
    private final int countOfSeat;
    private final int countOfCoach;
    
    private List<Coach> allCoach;
    private AtomicInteger countOfSoldTicket;
    private Queue<Integer> queueOfSoldTicket;
    
    public Route(final int routeId,
        final int countOfStation,
        final int countOfCoach,
        final int countOfSeat) {
        
        this.routeId = routeId;
        this.countOfSeat = countOfSeat;
        this.countOfCoach = countOfCoach;
        
        this.countOfSoldTicket = new AtomicInteger(0);
        this.queueOfSoldTicket = 
            new ConcurrentLinkedQueue<Integer>();
        
        this.allCoach = new ArrayList<Coach>();
        for (int i = 0; i < countOfCoach; i++) {
            this.allCoach.add(new Coach(i + 1, countOfSeat));
        }
    }
    
    public int checkFreeSeat(final int departure, 
        final int arrival) {
        
        int freeCount = 0;
        for (int i = 0; i < countOfCoach; i++) {
            freeCount += this.allCoach.get(i)
                .checkFreeSeat(departure, arrival);
        }
        return freeCount;
    }
    
    public Ticket trySeal(final String name, 
        final int departure, final int arrival) {
        
        Ticket ticket = null;
        CoachIdAndSeatId result = null;
        
        int i = 0;
        int j = ThreadLocalRandom.current()
            .nextInt(this.countOfCoach);
            
        while (i < this.countOfCoach) {
            result = this.allCoach.get(j).trySeal(departure, arrival);
                
            if (result != null) {
                // Create a ticket.
                ticket = new Ticket();
                
                ticket.tid = this.routeId * 1000000
                    + this.countOfSoldTicket.getAndIncrement();
                ticket.passenger = name;
                ticket.route = this.routeId;
                ticket.coach = result.coachId;
                ticket.seat = result.seatId;
                ticket.departure = departure;
                ticket.arrival = arrival;
                
                this.queueOfSoldTicket.add(
                    new Integer(ticket.hashCode()));
                
                break;
            }
            
            i++;
            j = (j + 1) % this.countOfCoach;
        }
                
        return ticket;
    }
    
    public boolean tryRefund(final Ticket ticket) {
        
        Integer hashCodeOfTicket = new Integer(ticket.hashCode());
        if (!this.queueOfSoldTicket.contains(hashCodeOfTicket)) {
            return false;
        } else {
            this.queueOfSoldTicket.remove(hashCodeOfTicket);
            final int coachId = ticket.coach;
            final int seatId = ticket.seat;
            final int departure = ticket.departure;
            final int arrival = ticket.arrival;
            
            return this.allCoach.get(coachId - 1).
                tryRefund(departure, arrival, seatId);
        }
    }
}