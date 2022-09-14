package ticketingsystem;

import java.util.Arrays;

public class Seat {
    protected boolean[] isNotOccupyStation;
    protected boolean[] isValidStaionToStaion;
    protected int stationNum;
    public Seat(int staionNum) {
        this.stationNum = staionNum;
        isNotOccupyStation = new boolean[staionNum]; //occupy占有的是k -> k + 1
        isValidStaionToStaion = new boolean[staionNum * (staionNum -1) / 2];
        Arrays.fill(isNotOccupyStation, true);
        Arrays.fill(isValidStaionToStaion, true);
    }
    /*private int computeIndex(int departure, int arrival) {
        return (((departure - 1) * ((stationNum << 1) - departure)) >> 1) + arrival - departure - 1;
    }*/
    public boolean occupy(int departure, int arrival) {
        for (int i = departure; i < arrival; ++i) {
            //System.out.println("座位所占区间："+i);
            isNotOccupyStation[i] = false;
        }
        return updateVaildStatus();
    }

    public boolean unoccupy(int departure, int arrival) {
        for (int i = departure; i < arrival; ++i) {
            isNotOccupyStation[i] = true;
        }
        return updateVaildStatus();
    }
    public boolean isValid(int departure, int arrival) {
        //int index = computeIndex(departure, arrival);
        int index = StationMapping.stationToStaion[departure][arrival];
        return isValidStaionToStaion[index];
    }
    public boolean isNotOccupy(int departure, int arrival) {
        for (int i = departure; i < arrival; ++i) {
            if (!isNotOccupyStation[i]) {
                return false;
            }
        }
        return true;
    }
    public boolean updateVaildStatus() {

        for (int i = 1; i < stationNum; ++i) {
            for (int j = i + 1; j <= stationNum; ++j) {
                //int index = computeIndex(i ,j);
                int index = StationMapping.stationToStaion[i][j];
                if (isNotOccupy(i, j)) {
                    isValidStaionToStaion[index] = true;
                } else {
                    while (j <= stationNum) {
                        index = StationMapping.stationToStaion[i][j];
                        isValidStaionToStaion[index] = false;
                        ++j;
                    }

                }
            }
        }
        return true;
    }


}
