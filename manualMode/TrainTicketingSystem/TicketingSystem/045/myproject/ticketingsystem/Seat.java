package ticketingsystem;

public class Seat {

	private int size;
	private int stationnum;
	private boolean[] isFree;
	private boolean[] hold;
	
	public boolean[] getIsFree() {
		boolean[] res=new boolean[size];
		for(int i=0;i<size;i++) {
			res[i]=isFree[i];
		}
		return res;
	}

	public Seat(int stationnum) {
		int i;
		size=(stationnum-1)*stationnum+1;
		isFree=new boolean[size];
		for(i=1;i<size;i++) {
			isFree[i]=true;
		}
		this.stationnum=stationnum;
		hold=new boolean[stationnum+1];
		for(i=0;i<=stationnum;i++) {
			hold[i]=false;
		}
	}
	
	public boolean isFree(int departure,int arrival) {
		for(int i=departure;i<arrival;i++) {
			if(hold[i])
				return false;
		}
		return true;
	}
	
	public void updateIsFree(int departure,int arrival) {
		int i,j;
		for(i=1;i<arrival;i++) {
			j=i<departure?departure+1:i+1;
			for(;j<=stationnum;j++) {
				isFree[(i-1)*stationnum+j]=isFree(i, j);
			}
		}
	}
	
	public boolean bought(int departure,int arrival) {
		if(isFree[(departure-1)*stationnum+arrival]) {
			for(int i=departure;i<arrival;i++) {
				hold[i]=true;
			}
			updateIsFree(departure,arrival);
			return true;
		}
		return false;
	}
	
	public void refunded(int departure,int arrival) {
		for(int i=departure;i<arrival;i++) {
			hold[i]=false;
		}
		updateIsFree(departure,arrival);
	}
	
}
