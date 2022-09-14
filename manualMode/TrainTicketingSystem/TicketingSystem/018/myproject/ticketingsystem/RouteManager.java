package ticketingsystem;

import java.util.concurrent.locks.StampedLock;

public class RouteManager {
    Route route;
    private StampedLock lock;
    private int station_num;
    private int[][] RemainTicket;
    private int total;
    private int coach_num;
    private int seat_num;

    RouteManager(int route_id,int coach_num,int seat_num,int station_num)
    {
        this.station_num = station_num;
        this.coach_num = coach_num;
        this.seat_num = seat_num;
        route = new Route(route_id,coach_num,seat_num,station_num);
        RemainTicket = new int[station_num][station_num];
        lock = new StampedLock();
        total = coach_num*seat_num;
        for(int i=0;i<station_num;i++)
        {
            for(int j=0;j<station_num;j++)
            {
                if(j>i)
                    RemainTicket[i][j] = total;
                else
                    RemainTicket[i][j] = 0;
            }
        }
    }

    public Ticket buyTicket(String passenger, int departure, int arrival)
    {
        long stamp = -1;
        Ticket ticket = route.buyTicket(passenger,departure,arrival);
        if(ticket == null)
            return null;
        else
        {
            int index = (ticket.coach-1)*seat_num+ticket.seat-1;
            Seat seat = route.seats[index];
            try{
                stamp = lock.writeLock();
                for(int i=0;i<station_num;i++)
                {
                    for(int j=i+1;j<station_num;j++)
                    {
                        if(!seat.labels[i][j].remain && !seat.labels[i][j].update) {
                            RemainTicket[i][j]--;
                            seat.labels[i][j].update = true;
                        }
                    }
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }finally {
                lock.unlock(stamp);

            }
            return ticket;
        }
    }

    public int inquiry(int departure, int arrival) {
        long stamp = -1;
        stamp = lock.tryOptimisticRead();
        int remain_ticket = RemainTicket[departure-1][arrival-1];
        if(!lock.validate(stamp))
        {
            try{
                stamp = lock.readLock();
                remain_ticket = RemainTicket[departure-1][arrival-1];
            }catch (Exception e)
            {
                e.printStackTrace();
            }finally {
                lock.unlockRead(stamp);
            }
        }
        return remain_ticket;
    }

    public void refundTicket(Ticket ticket){
        route.refundTicket(ticket);
        {
            boolean ret = false;
            int index = (ticket.coach-1)*seat_num+ticket.seat-1;
            Seat seat = route.seats[index];
            long stamp = -1;
            try{
                stamp = lock.writeLock();
                for(int i=0;i<station_num;i++)
                {
                    for(int j=i+1;j<station_num;j++)
                    {
                        if(seat.labels[i][j].remain && seat.labels[i][j].update)
                        {
                            seat.labels[i][j].update = false;
                            RemainTicket[i][j]++;
                        }
                    }
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }finally {
                lock.unlock(stamp);

            }
        }
    }
}
