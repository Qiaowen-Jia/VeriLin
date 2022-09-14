package ticketingsystem.tools;

class Section {
    private int routeTotal;
    private int coachTotal;
    private int seatTotal;
    private Seat[] sectionSeats;


    public Section(int routeTotal, int coachTotal, int seatTotal) {
        this.coachTotal = coachTotal;
        this.routeTotal = routeTotal;
        this.seatTotal = seatTotal;
        sectionSeats = new Seat[routeTotal * coachTotal * seatTotal];
        for (int i = 0; i < sectionSeats.length; ++i)
            sectionSeats[i] = new Seat();
    }

    public void lock(int route, int coach, int seat) {
        int index = this.getSeatIndex(route, coach, seat);
        sectionSeats[index].lock();
    }

    public void unlock(int route, int coach, int seat) {
        int index = this.getSeatIndex(route, coach, seat);
        sectionSeats[index].unlock();
    }

    public void occupy(int route, int coach, int seat) throws IllegalStateException {
        int index = this.getSeatIndex(route, coach, seat);
        sectionSeats[index].occupy();

    }

    public void free(int route, int coach, int seat) throws IllegalStateException {
        int index = this.getSeatIndex(route, coach, seat);
        sectionSeats[index].free();

    }

    public boolean isAvailable(int route, int coach, int seat) {
        return sectionSeats[this.getSeatIndex(route, coach, seat)].isAvailable();
    }



    private int getSeatIndex(int route, int coach, int seat) {
        route -= 1;
        coach -= 1;
        seat -= 1;
        return route * coachTotal * seatTotal + coach * seatTotal + seat;
    }


}