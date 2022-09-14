package ticketingsystem;

public class Seat {
    int stationNum;
    boolean[] occupied;

    Seat(int stationNum) {
        this.stationNum = stationNum;
        this.occupied = new boolean[stationNum];
        for (int i = 0; i < stationNum; ++i) {
            this.occupied[i] = false;
        }
    }

    boolean queryVacant(int departure, int arrival) {
        // caveat: range is not exclusive on both sides!
        for (int k = departure - 1; k < arrival - 1; ++k) {
            if (occupied[k]) {
                return false;
            }
        }

        return true;
    }

    boolean queryOccupied(int departure, int arrival) {
        for (int k = departure - 1; k < arrival - 1; ++k) {
            if (!occupied[k]) {
                return false;
            }
        }
        return true;
    }

    boolean occupy(int departure, int arrival) {
        // double check
        if (queryOccupied(departure, arrival)) {
            for (int k = departure - 1; k < arrival - 1; ++k) {
                occupied[k] = true;
            }
        } else {
            return false;
        }
        return true;
    }

    boolean vacate(int departure, int arrival) {
        // double check
        if (queryVacant(departure, arrival)) {
            for (int k = departure - 1; k < arrival - 1; ++k) {
                occupied[k] = false;
            }
        } else {
            return false;
        }
        return true;
    }
}