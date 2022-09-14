package ticketingsystem;


import java.util.concurrent.atomic.AtomicStampedReference;

// this is for checking whether a refund is valid
public class SeatDS
{
    private int stationNum;
    private AtomicStampedReference <OwnerInfo> [] ownerInfoReference;
    public SeatDS(int stationNum)
    {
        this.stationNum = stationNum;
        int maxInfoNum = (stationNum - 1) * (stationNum - 2) / 2 + stationNum - 1;
        ownerInfoReference = (AtomicStampedReference<OwnerInfo> [])new AtomicStampedReference<?> [maxInfoNum];
        for (int i = 0; i < maxInfoNum; i++)
        {
            ownerInfoReference[i] = new AtomicStampedReference<OwnerInfo>(null, 0);
        }
    }

    private int departureAndArrivalIDToIndex(int departureID, int arrivalID) throws IllegalArgsException
    {
        if (departureID > stationNum || arrivalID > stationNum || departureID >= arrivalID || arrivalID <= 0 || departureID <= 0)
        {
            throw new IllegalArgsException();
        }
        return (arrivalID - 1) * (arrivalID - 2) / 2 + departureID - 1;
    }

    public void refund(String passengerName, long ticketID, int departureID, int arrivalID ) throws InvalidTicketException
    {
        try
        {
            int intervalID = departureAndArrivalIDToIndex(departureID, arrivalID);
            int [] stampHolder = new int [1];
            OwnerInfo ownerInfo = ownerInfoReference[intervalID].get(stampHolder);
            if (ownerInfo == null)
            {
                //System.out.printf("%s\tseat not sold, refund failed\n", Thread.currentThread().getName());
                throw new InvalidTicketException ();
            }
            else
            {
                //System.out.printf("%s\tseat owner: %s\trefunder: (%s, %d)\n", Thread.currentThread().getName(), ownerInfo.toString(), passengerName, ticketID);
                if (ownerInfo.name.equals(passengerName) && ownerInfo.tid == ticketID)
                {
                    // if everything is matched at this point
                    // try CAS, if failed, that means someone has refund that ticket at least once
                    // then just announce the failure of this refund, it's linearized
                    boolean CASSucceed = ownerInfoReference[intervalID].compareAndSet(ownerInfo, null, stampHolder[0], stampHolder[0] + 1);
                    if (!CASSucceed)
                    {
                        throw new InvalidTicketException();
                    }
                }
                else
                {
                    throw new InvalidTicketException();
                }
            }

        }
        catch (IllegalArgsException iae)
        {
            throw new InvalidTicketException ();
        }
    }
    // this function should meet no inconsistency
    public void buy(String passengerName, long ticketID, int departureID, int arrivalID )
    {
        int intervalID = departureAndArrivalIDToIndex(departureID, arrivalID);
        int stamp = ownerInfoReference[intervalID].getStamp();
        OwnerInfo passengerInfo = new OwnerInfo(passengerName, ticketID);
        ownerInfoReference[intervalID].set(passengerInfo, stamp + 1);
        /*int [] stampHolder = new int [1];
        OwnerInfo ownerInfo = ownerInfoReference[intervalID].get(stampHolder);
        assert(ownerInfo == null);
        OwnerInfo passengerInfo = new OwnerInfo(passengerName, ticketID);

        boolean CASSucceed = ownerInfoReference[intervalID].compareAndSet(null, passengerInfo, stampHolder[0], stampHolder[0] + 1);
        assert(CASSucceed);*/

    }
}

class OwnerInfo
{
    public String name;
    public long tid;
    public OwnerInfo(String name, long ticketID)
    {
        this.name = name;
        this.tid = ticketID;
    }

    @Override
    public String toString() {
        return "OwnerInfo{" +
                "name='" + name + '\'' +
                ", tid=" + tid +
                '}';
    }
}
