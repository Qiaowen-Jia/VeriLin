package ticketingsystem;

public class Route {
    public int coachNum,seatNum,stationNum,tid;
    public Seat[] seats;
    public int[] buffer;
    public Object LockObj;
    
    public Route(int coachnum, int seatnum, int stationnum) {
        this.coachNum = coachnum;
        this.seatNum = seatnum;
        this.stationNum = stationnum;
        tid = 0;
        this.seats = new Seat[coachnum * seatnum+1];
        for (int i = 1; i <= coachnum * seatnum; i++) {
            this.seats[i] = new Seat(stationnum);
        }
        LockObj = new Object();
        int range=stationnum * (stationnum-1)+1;
        this.buffer = new int[range];
        for (int i =0; i<range; i++) {
            buffer[i] =-1;
        }
    }

    public int inquiry(int departure, int arrival) {
        int pos = (departure-1) * this.stationNum + arrival;
        if (this.buffer[pos] != -1) {
            return this.buffer[pos];
        }
        int Remain_count = 0;
        for (int i=1; i < this.seats.length; i++) {
        	boolean Remain=this.seats[i].Empty(departure, arrival);
        	if (Remain) {
        		Remain_count++;
            }
        }
        return Remain_count;
    }
    
    public int[] buyTicket(int departure, int arrival) {
        int[] coach_Seat=new int[2];
    	int pos=this.stationNum*(departure-1)+arrival;
    	int count = 0,tNum = -1,i = 1;
        int range=this.stationNum * this.stationNum;
        boolean[] oldsta=new boolean[range], newsta=new boolean[range];   
        synchronized (LockObj) {
            if (this.buffer[pos] == 0){
                return null;
          }
            for (; i < this.seats.length; i++) {
                if (this.seats[i].Empty(departure, arrival)) {
                    for (int k = 0; k < this.seats[i].buffer.length; k++) {
                        oldsta[k] = this.seats[i].buffer[k];
                    }
                    boolean operate=this.seats[i].occupy(departure, arrival);
                    if (operate) {
                        tNum = tid++;
                        int coachN = i / this.seatNum;
                        int seatN= i % this.seatNum;
                        if (seatN != 0) {
                        	int []temp={coachN+1,seatN};
                        	coach_Seat =temp;
                        } else {
                        	int []temp={coachN,this.seatNum};
                        	coach_Seat =temp;
                        }
                        break;
                    }
                }
            }
            if (tNum == -1) {
                return null;
            }
            if (this.buffer[pos] != -1) {
                count = this.buffer[pos]-1;
            } else {
                for (int j = i+1; j < this.seats.length; j++) {
                    if (this.seats[j].Empty(departure, arrival)) {
                        count++;
                    }
                }
            }
            updatebuffer(oldsta, newsta, this.seats[i], false);
            this.buffer[pos] = count;
        }
        return new int[]{tNum, coach_Seat[0], coach_Seat[1]};
    }


    public boolean refund(Ticket ticket, int departure, int arrival) {
        int freeNum = this.coachNum*(ticket.coach - 1)+ ticket.seat;
        int range=this.stationNum * this.stationNum;
        boolean[] oldsta = new boolean[range], newsta = new boolean[range];
        synchronized (LockObj) {
            for (int i = 0; i < this.seats[freeNum].buffer.length; i++) {
                oldsta[i] = this.seats[freeNum].buffer[i];
            }
            if (this.seats[freeNum].free(departure, arrival)) {
                updatebuffer(oldsta, newsta, this.seats[freeNum], true);
                return true;
            }
        }
        return false;
    }


    private void updatebuffer(boolean[] oldsta, boolean[] newsta, Seat seat, boolean flag) {
        for (int i=0; i<seat.buffer.length; i++) {
            newsta[i] = seat.buffer[i];
        }
        for (int i=1; i<this.stationNum; i++) {
            for (int j = i+1; j <= this.stationNum; j++) {
                int pos = this.stationNum*(i-1)+j;
                if (oldsta[pos] != newsta[pos]) {
                    if (this.buffer[pos]!= -1) {
                     if (flag)   
                    	this.buffer[pos] =this.buffer[pos]+ 1;
                    }
                }
            }
        }
    }
}