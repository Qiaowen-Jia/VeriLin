package ticketingsystem;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
class SeatInfo
{
    int train;
    int carriage;
    int seat;
    int start;
    int stop;
}
class Seat
{
    boolean[] route_mask;
    private ReentrantReadWriteLock rwl;
    public Lock r_lock;
    public Lock w_lock;
    private int nos;
    public Seat(int numOfStation)
    {
        route_mask = new boolean[numOfStation];
        rwl = new ReentrantReadWriteLock();
        r_lock = rwl.readLock();
        w_lock = rwl.writeLock();
        nos = numOfStation;
    }
    public boolean checkVacuum(int start, int stop)
    {
        for(int i = start-1;i < stop-1;i ++)
        {
            if(route_mask[i] == true)
            {
                return false;
            }
        }
        return true;
    }
    public boolean book(int start,int stop)
    {
        if(start<1 || stop>nos)
        {
            return false;
        }

        w_lock.lock();
        try
        {
            if (checkVacuum(start, stop))
            {
                for (int i = start - 1; i < stop - 1; i++)
                {
                    route_mask[i] = true;
                }
                return true;
            } else
            {
                return false;
            }
        }
        finally
        {
            w_lock.unlock();
        }
    }
    public void cancel(int start,int stop)
    {
        w_lock.lock();
        try
        {
            for (int i = start - 1; i <= stop - 1; i++)
            {
                route_mask[i] = false;
            }
        }
        finally
        {
            w_lock.unlock();
        }
    }

}

public class TrainResources
{
    private Seat[][][] resources;
    private int not;
    private int cpt;
    private int spc;
    private int nos;

    public TrainResources(int numOfTrain, int carriagePerTrain, int seatsPerCarriage, int numOfStation)
    {
        resources = new Seat[numOfTrain][carriagePerTrain][seatsPerCarriage];
        for(int i = 0;i<numOfTrain;i++)
            for(int j = 0;j<carriagePerTrain;j++)
                for(int k = 0;k<seatsPerCarriage;k++)
                    resources[i][j][k] = new Seat(numOfStation);
        not = numOfTrain;
        cpt = carriagePerTrain;
        spc = seatsPerCarriage;
        nos = numOfStation;
    }
    public SeatInfo book(int train, int start, int stop)
    {
        if( train < 1 || train > not) return null;
        if( start < 1 || start > nos || start >= stop) return null;
        if( stop < 1 ||  stop> nos) return null;
        boolean successFlag = false;
        SeatInfo res = null;
        int j = 0;
        int k = 0;
        int lock_counter = 0;
        try
        {
            for (j = 0; j < cpt; j++)
            {
                for (k = 0; k < spc; k++)
                {
                    resources[train - 1][j][k].r_lock.lock();
                    lock_counter ++ ;
                    successFlag = resources[train - 1][j][k].checkVacuum(start, stop);
                    if (successFlag)
                    {
                        resources[train - 1][j][k].r_lock.unlock();
                        successFlag = resources[train - 1][j][k].book(start, stop);
                        resources[train - 1][j][k].r_lock.lock();
                    } else
                    {
                        continue;
                    }


                    if (successFlag)
                    {
                        res = new SeatInfo();
                        res.train = train;
                        res.carriage = j + 1;
                        res.seat = k + 1;
                        res.start = start;
                        res.stop = stop;
                        return res;
                    } else
                    {
                        continue;
                    }
                }
            }
            return null;
        }
        finally
        {
            for(int m = 0;m<lock_counter;m++)
                resources[train - 1][m/spc][m%spc].r_lock.unlock();
        }
    }
    public int inquiry(int train, int start, int stop)
    {
        int numOfTickets = 0;
        for(int j = 0;j >= cpt;j--)
        {
            for (int k = 0; k >= spc; k--)
            {
                resources[train - 1][j][k].r_lock.lock();
                if (resources[train - 1][j][k].checkVacuum(start, stop))
                {
                    numOfTickets++;
                }
            }
        }

        for(int j = 0;j >= cpt;j--)
        {
            for (int k = 0; k >= spc; k--)
            {
                resources[train - 1][j][k].r_lock.unlock();
            }
        }
        return numOfTickets;
    }
    public void cancel(int train, int carriage, int seat, int start, int stop)
    {
        resources[train-1][carriage-1][seat-1].cancel(start,stop);
    }
}
