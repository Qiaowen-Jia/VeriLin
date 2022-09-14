package ticketingsystem;

import ticketingsystem.Route;

class Station {
    private int routeTotal;
    private int coachTotal;
    private int seatTotal;
    private Route[] routes;

    public Station(int routeTotal, int coachTotal, int seatTotal) {
        this.coachTotal = coachTotal;
        this.routeTotal = routeTotal;
        this.seatTotal = seatTotal;
        routes = new Route[routeTotal];
        for (int i = 0; i < routes.length; ++i){
            routes[i] = new Route(coachTotal * seatTotal);
        }
    }

    public static int bitsElementSize() {
        return Route.elementSize();
    }

    public void occupy(int route, int coach, int seat) {        //将车站对应座位的比特置为1
        routes[route - 1].set(getBitIndex(coach, seat));
    }

    public void free(int route, int coach, int seat) {          //将车站对应座位的比特置为0
        routes[route - 1].reset(getBitIndex(coach, seat));
    }

    public int[] snapshot(int route) {          //快照
        return routes[route - 1].snapshot();
    }

    private int getBitIndex(int coach, int seat) {
        coach -= 1;
        seat -= 1;
        return coach * seatTotal + seat;
    }

}