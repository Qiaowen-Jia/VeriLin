package ticketingsystem;

import java.util.concurrent.atomic.AtomicLong;

public class Train {
    public AtomicLong[] coaches;
    public final int seatNum;
    public final int coachNum;
    public final int stationNum;

    public Train(final int coachnum, final int seatnum, final int stationnum) {
        seatNum = (coachnum * seatnum);
        coachNum = coachnum;
        stationNum = stationnum;
        coaches = new AtomicLong[seatNum];
        for(int i = 0; i < seatNum; i++){
            coaches[i] = new AtomicLong(0);
        }
    }

    public int lockSeat(final int departure, final int arrival){
        long mask = (0x01 << (arrival - departure)) - 1;
        mask = mask << departure;
        for (int i = 0; i < seatNum; i++) {
            long tmp = coaches[i].get();
            while((mask & tmp) == 0){
                if(coaches[i].compareAndSet(tmp, (tmp | mask))){
                    return (i);
                }
                tmp = coaches[i].get();
            }
        }
        return -1;
    }

    public boolean unlockSeat(final int seatnum, final int departure, final int arrival){
        long mask = (0x01 << (arrival - departure)) - 1;
        mask = (mask << departure);
        while(true) {
            long tmp = coaches[seatnum].get();
            if (coaches[seatnum].compareAndSet(tmp, (tmp & ~mask))) {
            	//System.out.println(mask);
                return true;
            }
        }
    }

    public int querySeat(final int departure, final int arrival){
        int remainNum = 0;
        long mask = (0x01 << (arrival - departure)) - 1;
        mask = (mask << departure);
        for(int i = 0; i < seatNum; i++){
            if((mask & coaches[i].get()) == 0){
                ++remainNum;
            }
            //System.out.println(mask+ " " + (coaches[i].get()) + " " + (mask & ~coaches[i].get()) + " " + (2&-1));
        }
        return remainNum;
    }
    /*
     
    public Train(final int coachnum, final int seatnum, final int stationnum) {
        seatNum = (coachnum * seatnum) << 3;
        coachNum = coachnum;
        stationNum = stationnum;
        coaches = new AtomicLong[seatNum];
        for(int i = 0; i < seatNum; i+=8){
            coaches[i] = new AtomicLong(0);
        }
    }

    public int lockSeat(final int departure, final int arrival){
        long mask = (0x01 << (arrival - departure)) - 1;
        mask = mask << departure;
        for (int i = 0; i < seatNum; i+=8) {
            long tmp = coaches[i].get();
            while((mask & tmp) == 0){
                if(coaches[i].compareAndSet(tmp, (tmp | mask))){
                    return (i >> 3);
                }
                tmp = coaches[i].get();
            }
        }
        return -1;
    }

    public boolean unlockSeat(final int seatnum, final int departure, final int arrival){
        long mask = (0x01 << (arrival - departure)) - 1;
        mask = (mask << departure);
        while(true) {
            long tmp = coaches[seatnum << 3].get();
            if (coaches[seatnum << 3].compareAndSet(tmp, (tmp & ~mask))) {
                return true;
            }
        }
    }

    public int querySeat(final int departure, final int arrival){
        int remainNum = 0;
        long mask = (0x01 << (arrival - departure)) - 1;
        mask = (mask << departure);
        for(int i = 0; i < seatNum; i+=8){
            if((mask & ~coaches[i].get()) == 0){
                ++remainNum;
            }
        }
        return remainNum;
    }
     */
}
