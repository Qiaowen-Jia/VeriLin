package ticketingsystem;

public class Seat {
    public boolean[] status,buffer;
    public int stationNum;

    public Seat(int stationNum) {
        this.stationNum = stationNum;
        status = new boolean[stationNum];
        int range=this.stationNum * this.stationNum;
        buffer = new boolean[range];
        for (int i =0; i<stationNum; i++) {
            status[i] =true;
        }
        for (int i =0; i<range; i++) {
            buffer[i] =true;
        }
    }
 
    public boolean Empty(int departure, int arrival) {
        return this.buffer[(departure - 1) * this.stationNum + arrival];
    }
    
    public boolean occupy(int departure, int arrival) {
        for (int i = departure; i < arrival; i++) {
            status[i] = false;
        }	
        return updatebuffer();
    }

    public boolean free(int departure, int arrival) {
        for (int i = departure; i < arrival; i++) {
            status[i] = true;
        }
        return updatebuffer();
    }


    public boolean updatebuffer() {
        for (int i = 1; i < this.stationNum; i++) {
            for (int j = i + 1; j < this.stationNum+1; j++) {
                boolean check=false;
                for (int k=i; k<j; k++) {
                    if (!status[k]) {
                    	check=true;
                    	break;
                    }
                }              
                int n = j;
                if (check){
                    while (j<=this.stationNum){
                        buffer[(i-1)* this.stationNum+j] = false;
                        j++;
                    }
                    i = n;
                }else {
                    buffer[(i-1)* this.stationNum+n] = true;
                }
            }
        }
        return true;
    }

}
