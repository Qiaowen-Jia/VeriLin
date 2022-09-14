package ticketingsystem;

public class TicketingDS implements TicketingSystem {

	private TrainTicketingDS[] trains;
	private int routenum;
	TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum){
		this.routenum = routenum;
		this.trains = new TrainTicketingDS[routenum];
		for(int trainNr = 1; trainNr <= routenum; trainNr++){
			this.trains[trainNr - 1] = new AdptGraFCStampedTrainTicketingDS(trainNr, coachnum, seatnum,stationnum,threadnum);
		}
	}

	@Override
	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
		if(route <= 0 || route > this.routenum){
			return null;
		}
		return this.trains[route - 1].buyTicket(passenger, departure, arrival);
	}

	@Override
	public int inquiry(int route, int departure, int arrival) {
		if(route <= 0 || route > this.routenum){
			return 0;
		}
		return this.trains[route - 1].inquiry(departure, arrival);
	}

	@Override
	public boolean refundTicket(Ticket ticket) {
		if(ticket == null || ticket.route <= 0 || ticket.route > this.routenum){
			return false;
		}
		return this.trains[ticket.route - 1].refundTicket(ticket);
	}
}
