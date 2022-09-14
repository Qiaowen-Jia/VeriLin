package ticketingsystem;


public class Seat {
	private final int seatId;
	private long stationList;
	private final long[] maps = { 0x0, 0x1, 0x3, 0x7, 0xF, 0x1F, 0x3F, 0x7F, 0xFF, 0x1FF, 0x3FF, 0x7FF, 0xFFF, 0x1FFF,
			0x3FFF, 0x7FFF, 0xFFFF, 0x1FFFF, 0x3FFFF, 0x7FFFF, 0xFFFFF, 0x1FFFFF, 0x3FFFFF, 0x7FFFFF, 0xFFFFFF,
			0x1FFFFFF, 0x3FFFFFF, 0x7FFFFFF, 0xFFFFFFF, 0x1FFFFFFF, 0x3FFFFFFF, 0x7FFFFFFF, 0xFFFFFFFF, };

	public Seat(final int seatId) {
		this.seatId = seatId;
		this.stationList = 0;
	}

	public int tryBuyTicket(final int departure, final int arrival) {
		long tryBuyNum = (~maps[arrival - 1]) | maps[departure - 1];
		if ((tryBuyNum & this.stationList) != 0)
			return -1;
		synchronized (this) {
			if ((tryBuyNum & this.stationList) != 0) return -1;
			this.stationList = tryBuyNum | this.stationList;
		}
		return this.seatId;
	}

	public int tryInqueryTicket(final int departure, final int arrival) {
		long tryInqueryNum;
		long currentNum;
		tryInqueryNum = (~maps[arrival - 1]) | maps[departure - 1];
		currentNum = this.stationList;
		long test = tryInqueryNum & currentNum;
		if (test == 0) {
			return 1;
		} else {
			return 0;
		}
	}

	public boolean tryRefundTicket(final int departure, final int arrival) {
		long tryInqueryNum = (~maps[arrival - 1]) | maps[departure - 1];
		long newSeatNum;
		long oldSeatNum;
		tryInqueryNum = ~tryInqueryNum;
		synchronized (this) {
			oldSeatNum = this.stationList;
			newSeatNum = oldSeatNum & tryInqueryNum;
			this.stationList = newSeatNum;
		}
		return true;
	}
}
