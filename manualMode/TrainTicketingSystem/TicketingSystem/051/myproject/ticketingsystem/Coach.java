package ticketingsystem;

/**
 * one train has several coaches depends on the {@code}coachnum.
 * every Coach has many seats, and every seat has different status at different station.
 * in coaches, all integer number start at 0.
 * 
 * if the seat use one class to define, the data will be too many. will try if time is enough.
 * 
 * @author Firzen
 *
 */
public final class Coach {

	public static int seatSum;//how many seats there are.
	
	private static final int LOOP_SIZE = 8;
	
	/**
	 * one seat has one BitSet, and one BitSet keeps the seat's status at every station. 
	 */
	public final Seat[] seatsMap;
	
	
	public Coach (int seatSum) {
		
		seatsMap = new Seat[seatSum];
		
		for(int i = 0; i < seatSum; i++) {
			seatsMap[i] = new Seat();
		}
		
	}
	
	/**
	 * calculate how many empty seats from departure to arrival there are.
	 * @param departure
	 * @param arrival
	 * @return
	 */
	public final int sumAllEmptySeats(int departure, int arrival) {
		
		int emptySeatsSum = 0;
		long seatMask = Seat.generateSeatMask(departure, arrival);
		
		int reminder = seatSum % LOOP_SIZE;//loop_size = 8.

		for (int i = 0; i < seatSum - reminder; i+=LOOP_SIZE) {
			if(seatsMap[i].sects(seatMask)) {
				emptySeatsSum++;
			}
			if(seatsMap[i+1].sects(seatMask)) {
				emptySeatsSum++;
			}
			if(seatsMap[i+2].sects(seatMask)) {
				emptySeatsSum++;
			}
			if(seatsMap[i+3].sects(seatMask)) {
				emptySeatsSum++;
			}
			if(seatsMap[i+4].sects(seatMask)) {
				emptySeatsSum++;
			}
			if(seatsMap[i+5].sects(seatMask)) {
				emptySeatsSum++;
			}
			if(seatsMap[i+6].sects(seatMask)) {
				emptySeatsSum++;
			}
			if(seatsMap[i+7].sects(seatMask)) {
				emptySeatsSum++;
			}
		}
		
		for(int i = seatSum - reminder; i < seatSum; i++) {
			if(seatsMap[i].sects(seatMask)) {
				emptySeatsSum++;
			}
		}
		return emptySeatsSum;
	}
	
	/**
	 * look for one empty seat.
	 * @param departure
	 * @param arrival
	 * @return return -1 means no empty seat.
	 */
	public final int queryAllSeatsForOne(long seatMask) {
		
		int reminder = seatSum % LOOP_SIZE;//loop_size = 8.
		for (int i = 0; i < seatSum - reminder; i+=LOOP_SIZE) {
			if(seatsMap[i].sects(seatMask)) {
				return i;
			}
			if(seatsMap[i+1].sects(seatMask)) {
				return i + 1;
			}
			if(seatsMap[i+2].sects(seatMask)) {
				return i + 2;
			}
			if(seatsMap[i+3].sects(seatMask)) {
				return i + 3;
			}
			if(seatsMap[i+4].sects(seatMask)) {
				return i + 4;
			}
			if(seatsMap[i+5].sects(seatMask)) {
				return i + 5;
			}
			if(seatsMap[i+6].sects(seatMask)) {
				return i + 6;
			}
			if(seatsMap[i+7].sects(seatMask)) {
				return i + 7;
			}
		}
		for(int i = seatSum - reminder; i < seatSum; i++) {
			if(seatsMap[i].sects(seatMask)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * get seat.
	 * @param seatnum
	 * @param departure
	 * @param arrival
	 * @return return -1 means no empty seat.
	 */
	public final int getSeat(int departure, int arrival) {
		
		for (long seatMask = Seat.generateSeatMask(departure, arrival); ; ) {
			
			int targetSeat = queryAllSeatsForOne(seatMask);

			if (targetSeat < 0) { // there are no empty seats.
				return -1;
			} 
			else {
				if(seatsMap[targetSeat].set(seatMask)) {
					return targetSeat;
				} 
				else{//both seat not empty and CAS failed need continue.
					continue;
				}
			}
		}
		
	}
	
	/**
	 * reset the special seat identified by seatNum, from departure to arrival.
	 * @param seatNum
	 * @param departure
	 * @param arrival
	 * @return nothing.
	 */
	public final void resetSeat(int seatNum, int departure, int arrival) {

		seatsMap[seatNum].clear(Seat.generateSeatMask(departure, arrival));
	}
}
