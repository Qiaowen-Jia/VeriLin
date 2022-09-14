package ticketingsystem;

import java.util.concurrent.atomic.AtomicLong;


/**
 * one seat keeps all station status, using an AtomicLong stationMap.
 * in seats, all integer number start at 0.
 * @author Firzen
 *
 */
public final class Seat {
	final AtomicLong stationMap = new AtomicLong(0);
	long l1, l2, l3, l4, l5, l6;//byte filling to avoid false sharing.
	
	public Seat() {
	}
	
	/**
	 * make a mask for the AtomicLong stationMap, 
	 * bits from departure(included) to arrival(not included) will be set to 1,
	 * and other bits will be 0.
	 * @param departure
	 * @param arrival
	 * @return a 64 bits mask like 000011110000
	 */
	public final static long generateSeatMask(int departure, int arrival) {
		long seatMask = 0x8000000000000000l;
		seatMask >>= (arrival - departure - 1);
		seatMask >>>= departure;
		return seatMask;
	}
	
	/**
	 * to set the special station map bits to 1.
	 * @param seatMask
	 * @return true means got seat, false means CAS operation failed or bits are not all 0.
	 */
	public final boolean set(long seatMask) {
		long currentMap = stationMap.get();
		if((currentMap & seatMask) == 0) {//if the seat is empty.
			return (stationMap.compareAndSet(currentMap, currentMap | seatMask));
		}
		else {
			return false;//false because seat is not empty.
		}
	}
	
	/**
	 * to clear the special station map bits to 0.
	 * @param seatMask
	 * @return nothing.
	 */
	public final void clear(long seatMask) {
		long currentMap;
		while(true) {
			currentMap = stationMap.get();
			//it seems impossible that (currentMap & seatMask)!=seatMask, so no need to verify.
			if(stationMap.compareAndSet(currentMap, currentMap & (~seatMask))) {
				return;
			}
		}
	}
	
	/**
	 * to measure if the bits in the seatMask is 0. 
	 * after jdk9 can use stationMap.getPlain() to avoid cache invalidation.
	 * @param seatMask
	 * @return true if there are no masked bits both are true in seatMask and stationMap. 
	 */
	public final boolean sects(long seatMask) {
		return ((stationMap.get() & seatMask) == 0);
	}

}
