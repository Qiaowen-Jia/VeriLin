package ticketingsystem;

public class Coach {
	int coachid;
	Seat seats[];

	Coach (int coachid, int seatnum, int stationnum) {
		this.coachid = coachid;
		this.seats = new Seat[seatnum + 1];
		for (int i = 1; i <= seatnum; i++) {
			this.seats[i] = new Seat(i, stationnum);
		}
	}
}