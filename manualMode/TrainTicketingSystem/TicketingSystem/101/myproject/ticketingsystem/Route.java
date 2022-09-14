package ticketingsystem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class Route {
    private int coachNum;
    private int seatNum;
    private int allSeats;
    private ArrayList<LinkedList<Integer>> seatStations;
    private int lastIndex;

    static private final int MASK = 0xffff;
    static private final int HALF_WORD = 16;

    static int getCombinedStationValue(int departure, int arrival) {
        return (departure << HALF_WORD) | arrival;
    }

    public Route(int coachnum, int seatnum) {
        this.coachNum = coachnum;
        this.seatNum = seatnum;
        this.seatStations = new ArrayList<LinkedList<Integer>>(coachnum * seatnum);
        for (int i = 0; i < coachnum * seatnum; ++i) {
            seatStations.add(i, new LinkedList<Integer>());
        }
        this.allSeats = coachnum * seatnum;
        lastIndex = 0;
    }

    // return coachNumber and seatNumber.
    // departure and arrrival starts from 0
    public int[] getAvaliableSeat(int departure, int arrival) {
        boolean found = false;
        int i = lastIndex;
        for (; i < allSeats; ++i) {
            LinkedList<Integer> intervals = seatStations.get(i);
            if (isSeatAvailable(intervals, departure+1, arrival+1)) {
                int combinedValue  = getCombinedStationValue(departure+1, arrival+1);
                intervals.add(combinedValue);
                found = true;
                break;
            }
        }
        if (!found) {
            for (i = 0; i < lastIndex; ++i) {
                LinkedList<Integer> intervals = seatStations.get(i);
                if (isSeatAvailable(intervals, departure+1, arrival+1)) {
                    int combinedValue  = getCombinedStationValue(departure+1, arrival+1);
                    intervals.add(combinedValue);
                    found = true;
                    break;
                }
            }
        }
        if (found) {
            int[] res = new int[2];
            res[0] = i / seatNum;
            res[1] = i % seatNum;
            lastIndex = i;
            return res;
        }
        return null;
    }

    public void setSeatAvailable(int coach, int seat, int departure, int arrival) {
        int index = coach * seatNum + seat;
        LinkedList<Integer> intervals = seatStations.get(index);
        Integer combinedValue = getCombinedStationValue(departure+1, arrival+1);
        intervals.remove(combinedValue);
        lastIndex = index;
    }

    public int getRemainingTickets(int departure, int arrival) {
        int cnt = 0;
        for (int i = 0; i < allSeats; ++i) {
            if (!isSeatAvailable(seatStations.get(i), departure+1, arrival+1)) {
                ++cnt;
            }
        }
        return allSeats - cnt;
    }

    private boolean isSeatAvailable(LinkedList<Integer> intervals, int departure, int arrival) {
        if (intervals == null) {
            return true;
        }

        for (Integer value: intervals) {
            int combinedValue = value;
            int existedDeparture = combinedValue >>> HALF_WORD;
            int existedArrival = combinedValue & MASK;
            if (!(existedArrival <= departure || arrival <= existedDeparture)) {
                return false;
            }
        }

        return true;
    }


}
