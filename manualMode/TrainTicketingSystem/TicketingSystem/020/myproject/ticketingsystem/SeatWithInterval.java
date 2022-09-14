package ticketingsystem;

public class SeatWithInterval extends Seat implements Comparable<SeatWithInterval> {
    int start, end;

    private SeatWithInterval(int coach, int seat) {
        super(coach, seat);
    }

    SeatWithInterval(int coach, int seat, int stationNum, int start, int end) {
        super(coach, seat, stationNum);
        this.start = start;
        this.end = end;
    }

    SeatWithInterval copy(int start, int end) {
        SeatWithInterval res = new SeatWithInterval(coach, seat);
        res.seatOccupy = seatOccupy;
        res.start = start;
        res.end = end;
        return res;
    }

    @Override
    public int compareTo(SeatWithInterval o) {
        if (this.start == o.start) {
            return Integer.compare(this.end, o.end);
        }
        return Integer.compare(this.start, o.start);
    }
}
