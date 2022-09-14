package ticketingsystem;

import java.util.concurrent.atomic.AtomicInteger;

public class Seat {
	int stationNum;
	AtomicInteger stationState = new AtomicInteger(0);
	int code;
	
	public Seat(int stationnum) {
		stationNum = stationnum;
		code = (1<<stationNum)-1;
	}

	boolean canWrite(int departure, int arrival) {
		int seatState = stationState.get();
		//左移ｉ位等价模2^(32-i),右移i等价除2^i
		int targetPartState = seatState<<(32-(arrival-1))>>>(32-(arrival-1)+(departure-1));
		int diff = code<<(32-(arrival-1))>>>(32-(arrival-1)+(departure-1))<<(departure-1);
//		int targetPartState = seatState%(int)Math.pow(2, arrival-1)/(int)Math.pow(2, departure-1);
//		int diff = 1023%(int)Math.pow(2, arrival-1)/(int)Math.pow(2, departure-1)*(int)Math.pow(2, departure-1);
		int	newState = seatState+diff;
		
		while(targetPartState==0) {
			if(stationState.compareAndSet(seatState, newState)) {
				return true;
			}else {
				seatState = stationState.get();
				targetPartState = seatState<<(32-(arrival-1))>>>(32-(arrival-1)+(departure-1));
				//targetPartState = seatState%(int)Math.pow(2, arrival-1)/(int)Math.pow(2, departure-1);
				newState = seatState+diff;
			}
		}		
		return false;
	}

	boolean releaseSeat(int departure, int arrival) {
		int seatState = stationState.get();
		int diff = code<<(32-(arrival-1))>>>(32-(arrival-1)+(departure-1))<<(departure-1);
		int newState = seatState-diff;
		//int diff = -1*(1023%(int)Math.pow(2, ticket.arrival-1)/(int)Math.pow(2, ticket.departure-1)*(int)Math.pow(2, ticket.departure-1));
		
		while(!stationState.compareAndSet(seatState, newState)) {
			seatState = stationState.get();
			newState = seatState-diff;
		}
		return true;
	}

	boolean canRead(int departure, int arrival) {
		int seatState = stationState.get();
		int targetPartState = seatState<<(32-(arrival-1))>>>(32-(arrival-1)+(departure-1));
//		int targetPartState = seatState%(int)Math.pow(2, arrival-1)/(int)Math.pow(2, departure-1);
		
		if(targetPartState==0) return true;
		return false;
	}
}
