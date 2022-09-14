package ticketingsystem;

import java.util.concurrent.atomic.AtomicReference;

public class Seat {
    int coach;
    int seat;
    AtomicReference<SeatOccupy> seatOccupy;

    Seat(int coach, int seat) {
        this.coach = coach;
        this.seat = seat;
    }

    Seat(int coach, int seat, int stationNum) {
        this.coach = coach;
        this.seat = seat;
        seatOccupy = new AtomicReference<>(new SeatOccupy(stationNum));
    }
}
