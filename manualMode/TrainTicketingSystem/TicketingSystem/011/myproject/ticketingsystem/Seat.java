package ticketingsystem;

public class Seat {

    protected long occupiedBitmap;
    protected int stationnum;
    static protected int[][] maskMap;

    public boolean isRangeOccupied(int departure, int arrival) {
        return ((this.maskMap[departure][arrival] & this.occupiedBitmap)  != 0);
    }

    public void occupyRange(int departure, int arrival) {
        this.occupiedBitmap |= this.maskMap[departure][arrival];
    }

    public void releaseRange(int departure, int arrival){
        this.occupiedBitmap &= ~this.maskMap[departure][arrival];
    }

    Seat(int stationnum) {
        this.occupiedBitmap = 0;
        this.stationnum = stationnum;
        // 把 maskMap 预先生成
        this.maskMap = new int[stationnum+1][stationnum+1];
        for(int departure = 1; departure <= stationnum; departure++){
            for(int arrival = departure; arrival <= stationnum; arrival++){
                int mask = 0;
                for (int i = 0; i < stationnum; i++) {
                    if (i >= departure - 1 && i < arrival - 1) {
                        mask += 1;
                    }
                    mask = mask << 1;
                }
                maskMap[departure][arrival] = mask;
            }
        }
    }
}