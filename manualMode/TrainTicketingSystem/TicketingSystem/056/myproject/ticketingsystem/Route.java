package ticketingsystem;

public class Route {
	int routeid;
	Coach coaches[];

	Route (int routeid, int coachnum, int seatnum, int stationnum) {
		this.routeid = routeid;
		this.coaches = new Coach[coachnum + 1];
		for (int i = 1; i <= coachnum; i++) {
			this.coaches[i] = new Coach(i, seatnum, stationnum);
		}
	}
}