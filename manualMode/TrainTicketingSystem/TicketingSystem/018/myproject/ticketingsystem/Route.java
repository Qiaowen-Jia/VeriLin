package ticketingsystem;

import java.util.concurrent.locks.ReentrantLock;

public class Route {
    public int route_id;
    public int coach_num;
    public int seat_num;
    public Seat head;
    public Seat[]seats;
    private ReentrantLock seat_lock;
    private int size;

    Route(int route_id,int coach_num,int seat_num,int station_num)
    {
        this.route_id = route_id;
        this.coach_num = coach_num;
        this.seat_num = seat_num;
        int total = coach_num * seat_num;
        size = total;
        head = new Seat();
        head.pre = null;
        seats = new Seat[total];
        for(int i=0;i<total;i++)
        {
            if(i == 0 && i != total-1)
                seats[i] = new Seat(i,seats[1],head,station_num,seat_num);
            else if(i == total-1)
                seats[i] = new Seat(i,null,seats[i-1],station_num,seat_num);
            else
                seats[i] = new Seat(i,seats[i+1],seats[i-1],station_num,seat_num);
        }
        head.next = seats[0];
        for(int i=0;i<total-1;i++)
        {
            seats[i].next = seats[i+1];
        }
        seat_lock = new ReentrantLock();
    }

    public Ticket buyTicket(String passenger, int departure, int arrival)
    {
        Ticket ticket;
        try{
            seat_lock.lock();
            Seat tmp = head.next;
            while(tmp != null)
            {
                if(tmp.isContains(departure,arrival))
                {
                    int seat_index = (tmp.coach_id-1)*seat_num+tmp.seat_id-1;
                    ticket = new Ticket(passenger,route_id,tmp.coach_id,tmp.seat_id,departure,arrival);
                    tmp.RemoveStations(departure,arrival);
                    if(tmp.canRemove())
                        RemoveSeat(seat_index);
                    return ticket;
                }
                tmp = tmp.next;
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }finally {
            seat_lock.unlock();
        }
        return null;
    }

    public void refundTicket(Ticket ticket) {
        try {
            seat_lock.lock();
            int seat_index = (ticket.coach-1)*seat_num+ticket.seat-1;
            if(!seats[seat_index].inlist)
            {
                AddSeat(seat_index);
            }
            seats[seat_index].AddStations(ticket.departure,ticket.arrival);
        }catch (Exception e)
        {
            e.printStackTrace();
        }finally {
            seat_lock.unlock();
        }
    }


    private void RemoveSeat(int seat_index)
    {
        seats[seat_index].inlist = false;
        Seat pre = seats[seat_index].pre;
        Seat next = seats[seat_index].next;
        if(seats[seat_index].next == null)
        {
            pre.next = null;
        }
        else
        {
            pre.next = next;
            next.pre = pre;
        }
        seats[seat_index].pre = null;
        seats[seat_index].next = null;
    }

    private void AddSeat(int seat_index)
    {
        seats[seat_index].inlist = true;
        if(head.next == null)
        {
            head.next = seats[seat_index];
            seats[seat_index].pre = head;
        }
        else {
            Seat next = head.next;
            head.next = seats[seat_index];
            seats[seat_index].pre = head;
            next.pre = seats[seat_index];
            seats[seat_index].next = next;
        }
    }

}
