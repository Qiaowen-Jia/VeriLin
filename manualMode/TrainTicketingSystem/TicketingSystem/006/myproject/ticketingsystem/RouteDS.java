package ticketingsystem;

import java.util.BitSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicStampedReference;

class RouteDS
{

    private int stationNum, coachNum, seatNum, routeID, threadNum;
    private StationDS [] station;
    private SeatDS [] seat;
    private ConcurrentHashMap soldTicket;
    private static final AtomicLong nextTicketId = new AtomicLong(1);
    private AtomicStampedReference<BitSet> [] cache;
    public RouteDS(int routeID, int stationNum, int coachNum, int seatNum, int threadNum)
    {
        this.routeID = routeID;
        this.stationNum = stationNum;
        this.coachNum = coachNum;
        this.seatNum = seatNum;
        this.threadNum = threadNum;
        this.station = new StationDS [stationNum];
        this.seat = new SeatDS[coachNum * seatNum];

        for (int i = 0; i < stationNum; i++)
        {
            station[i] = new StationDS(coachNum, seatNum);
        }
        for (int i = 0; i < coachNum * seatNum; i++)
        {
            seat[i] = new SeatDS(stationNum);
        }
        /*int intervalNum = (stationNum - 1) * (stationNum - 2) / 2 + stationNum - 1;
        cache = new AtomicStampedReference [intervalNum];
        for (int i = 0; i < intervalNum; i++)
        {
            cache[i] = new AtomicStampedReference<BitSet>(null, 0);
        }*/

    }
    public int inquiry(int departureID, int arrivalID) throws IllegalArgsException
    {

        if (departureID >= arrivalID || arrivalID > stationNum)
        {
            throw new IllegalArgsException();
        }
        BitSet bs = (BitSet) station[departureID - 1].seatBitMap.clone();
        for (int i = departureID; i < arrivalID - 1; i++)
        {
            bs.or(station[i].seatBitMap);
        }

        int ret = coachNum * seatNum - bs.cardinality();
        int j = bs.nextClearBit(0);
        return ret;
    }

    public Ticket buy(String passengerName, int departureID, int arrivalID) throws NoTicketsException, IllegalArgsException
    {

        if (departureID >= arrivalID || arrivalID > stationNum)
        {
            throw new IllegalArgsException();
        }
        station[departureID - 1].lock.lock();
        BitSet bs = (BitSet) station[departureID - 1].seatBitMap.clone();
        for (int i = departureID; i < arrivalID - 1; i++)
        {
            station[i].lock.lock();
            bs.or(station[i].seatBitMap);
        }
        int index = bs.nextClearBit(0);

        if (index == coachNum * seatNum)
        {
            // no available seat in this interval
            for (int i = departureID - 1; i < arrivalID - 1; i++)
            {
                station[i].lock.unlock();
            }
            throw new NoTicketsException();
        }
        for (int i = departureID - 1; i < arrivalID - 1; i++)
        {
            station[i].seatBitMap.set(index);
            station[i].lock.unlock();
        }
        Ticket retTicket = new Ticket();
        int res [] = indexToCoachAndSeatID(index);
        retTicket.coach = res[0];
        retTicket.seat = res[1];
        retTicket.tid = nextTicketId.getAndIncrement();
        retTicket.departure = departureID;
        retTicket.arrival = arrivalID;
        retTicket.route = this.routeID;
        retTicket.passenger = passengerName;

        seat[index].buy(passengerName, retTicket.tid, departureID, arrivalID);


        return retTicket;
    }
    public void refund(Ticket ticket) throws InvalidTicketException
    {
        int index;
        index = CoachAndSeatIDToIndex(ticket.coach, ticket.seat);

        seat[index].refund(ticket.passenger, ticket.tid, ticket.departure, ticket.arrival);


        for (int i = ticket.departure - 1;  i < ticket.arrival - 1; i++)
        {
            station[i].lock.lock();
            station[i].seatBitMap.clear(index);
            station[i].lock.unlock();
        }
    }
    private int [] indexToCoachAndSeatID(int index)
    {
        int coachID = index / seatNum + 1;
        int seatID = index % seatNum + 1;
        // won't jvm JIT compiler optimizes this?
        int [] ret = new int [2];
        ret[0] = coachID;
        ret[1] = seatID;
        return ret;
    }
    private int CoachAndSeatIDToIndex(int coachID, int seatID) throws IllegalArgsException
    {
        if (coachID > coachNum || seatID > seatNum)
        {
            throw new IllegalArgsException();
        }
        int index = (coachID - 1) * seatNum + seatID - 1;
        return index;
    }

}




