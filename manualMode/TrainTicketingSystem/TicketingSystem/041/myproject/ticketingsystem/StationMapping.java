package ticketingsystem;

public class StationMapping {
    protected static int [][]stationToStaion;
    private int stationNum;
    private int computeIndex(int departure, int arrival) {
        return (((departure - 1) * ((stationNum << 1) - departure)) >> 1) + arrival - departure - 1;
    }
    public StationMapping(int stationNum) {
        this.stationNum = stationNum;
        stationToStaion = new int[stationNum + 1][stationNum + 1];
        for (int i = 1; i < stationNum; ++i) {
            for (int j = i + 1; j <= stationNum; ++j) {
                stationToStaion[i][j] = computeIndex(i, j);
                //System.out.print(stationToStaion[i][j]+ " ");
            }
            //System.out.println();
        }
    }
}
