package ticketingsystem;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class Test2 {
    public AtomicLong[] coaches;
    public final int seatNum;
    public final int coachNum;
    public final int stationNum;
    

    public Test2(final int coachnum, final int seatnum, final int stationnum) {
    	System.out.println("coachnum:" + coachnum);
    	System.out.println("seatnum:" + seatnum);
        seatNum = (coachnum * seatnum) << 3;
    	System.out.println("seatNum:" + seatNum);
        coachNum = coachnum;
        stationNum = stationnum;
        coaches = new AtomicLong[seatNum];
        for(int i = 0; i < seatNum; i+=8){
        	System.out.println("i:" + i);
            coaches[i] = new AtomicLong(0);
        }
    }
    
	public static void main(String[] args) throws InterruptedException {
		Test2 T = new Test2(5,100,10);
	}
}
