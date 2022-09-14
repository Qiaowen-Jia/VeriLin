package ticketingsystem;

public class Seat {
    public int coach_id;
    public int seat_id;
    public Seat next;
    public Seat pre;
    public int value;
    public boolean inlist = true;
    public int station_num;

    public Label[][] labels;
    public int [][] checks;
    Seat()
    {
        coach_id = 0;
        seat_id = 0;
        next = null;
        pre = null;
        value = 0;
        coach_id = 0;
        seat_id = 0;
        station_num = 0;
        labels = null;
        checks = null;
    }
    Seat(int index, Seat next, Seat pre, int station_num, int seat_num)
    {
        value = 0;
        value |= (int)(Math.pow(2,station_num)-1);
        coach_id = index / seat_num + 1;
        seat_id = index % seat_num + 1;
        this.next = next;
        this.pre = pre;
        this.station_num = station_num;
        labels = new Label[station_num][station_num];
        checks = new int[station_num][station_num];
        for(int i=0;i<station_num;i++)
        {
            for(int j=0;j<station_num;j++)
            {
                if(j>i)
                {
                    labels[i][j] = new Label();
                    checks[i][j] = 0;
                    for(int k=i;k<j;k++)
                        checks[i][j] += (1<<k);
                    checks[i][j] += (1<<(station_num-1));
                }
                else
                {
                    checks[i][j] = 0;
                    labels[i][j] = null;
                }
            }
        }
    }

    public void AddStations(int departure,int arrival)
    {
        for(int i=departure;i<arrival;i++)
        {
            value |= (1<<(i-1));
        }
        for(int i=0;i<station_num;i++)
        {
            for(int j=i+1;j<station_num;j++)
            {
                int tmp = value | checks[i][j];
                if(tmp == value)//退票后此区间的座位被补充
                    labels[i][j].remain = true;
            }
        }
    }

    public void RemoveStations(int departure,int arrival)
    {
        for(int i=departure;i<arrival;i++)
        {
            value ^= (1<<(i-1));
        }
        for(int i=0;i<station_num;i++)
        {
            for(int j=i+1;j<station_num;j++)
            {
                int tmp = value | checks[i][j];
                if(tmp != value)//此区间的座位收到影响
                    labels[i][j].remain = false;
            }
        }
    }

    public boolean isContains(int departure,int arrival)
    {
        for(int i=departure;i<arrival;i++)
        {
            int tmp = value & (1<<(i-1));
            if(tmp == 0)
                return false;
        }
        return true;
    }

    public boolean canRemove()
    {
        return value == (1<<(station_num-1));
    }

}
