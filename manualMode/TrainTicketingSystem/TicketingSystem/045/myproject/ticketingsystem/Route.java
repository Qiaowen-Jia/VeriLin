package ticketingsystem;

public class Route {

	private int stationnum;
	private int seatsSize;
	private Seat[] seats;
	private int[] tickets;//for inquiry
	
	public Route(int stationnum,int coachnum,int seatnum){
		int i,size,value;
		this.stationnum=stationnum;
		size=(stationnum-1)*stationnum+1;
		value=coachnum*seatnum;
		tickets=new int[size];
		for(i=1;i<size;i++) {
			tickets[i]=value;
		}
		seatsSize=value+1;
		seats=new Seat[seatsSize];
		for(i=1;i<seatsSize;i++) {
			seats[i]=new Seat(stationnum);
		}
	}
	
	public int inqury(int departure,int arrival) {
		int count=-1;
		synchronized (tickets) {
			count=tickets[(departure-1)*stationnum+arrival];
		}
		return count;
	}
	
	public int buy(int departure,int arrival) {
		int i;
		if(inqury(departure, arrival)>0) {
			for(i=1;i<seatsSize;i++) {
				synchronized (seats[i]) {
					boolean[] oldIsFree=seats[i].getIsFree();
					if(seats[i].bought(departure, arrival)) {
						boolean[] newIsFree=seats[i].getIsFree();
						updateTickets(departure, arrival,oldIsFree,newIsFree,-1);
						return i;
					}	
				}
			}
		}
		return -1;
	}
	
	public void refund(int seat,int departure,int arrival) {
		synchronized (seats[seat]) {
			boolean[] oldIsFree=seats[seat].getIsFree();
			seats[seat].refunded(departure, arrival);
			boolean[] newIsFree=seats[seat].getIsFree();
			updateTickets(departure, arrival,oldIsFree,newIsFree,1);
		}
	}
	
	public void updateTickets(int departure,int arrival,boolean[] oldIsFree,boolean[] newIsFree,int value) {
		synchronized (tickets) {
			int i,j,index;
			for(i=1;i<arrival;i++) {
				j=i<departure?departure+1:i+1;
				for(;j<=stationnum;j++) {
					index=(i-1)*stationnum+j;
					if(oldIsFree[index]!=newIsFree[index]) {
						tickets[index]+=value;
					}
				}
			}
		}
	}
}
