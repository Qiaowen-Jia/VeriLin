package ticketingsystem;

public class Seat {
    public int[] seatArray;//同一时间只能有一个购票操作修改座位的数组，每个座位对应一个数组
    public int stationNum;

    public Seat(int stationnum){
        stationNum = stationnum;
        seatArray = new int[stationNum*stationNum];
        for (int i = 0; i < seatArray.length; i++){
            seatArray[i] = 0;
        }
    }
    public boolean takeSeat(int departure, int arrival){
        int realDeparture = departure - 1;
        int realArrival = arrival - 1;
        //第一个站到departure站之间到达departure以后的站没有座位
        for (int i = 0; i <= realDeparture; i++){
            int temp = i * stationNum;
            for (int j = realDeparture + 1; j < stationNum; j++){
                seatArray[temp + j] += 1;
            }
        }
        //departure站和arrival之间的站作为起始站没有座位
        for (int i = realDeparture + 1; i < realArrival; i++){
            int temp = i * stationNum;
            for (int j = i + 1; j < stationNum; j++){
                seatArray[temp + j] += 1;
            }
        }
        return true;
    }
    public boolean isAvailable(int departure, int arrival){
        int realDeparture = departure - 1;
        int realArrival = arrival - 1;   
        return seatArray[realDeparture*stationNum + realArrival] == 0;
    }
    public boolean refundSeat(int departure, int arrival){
        int realDeparture = departure - 1;
        int realArrival = arrival - 1;
        //第一个站到departure站之间到达departure之后的站没有座位，退票时同样更新
        for (int i = 0; i <= realDeparture; i++){
            int temp = i * stationNum;
            for (int j = realDeparture + 1; j < stationNum; j++){
                seatArray[temp + j] += -1;
            }
        }
        //departure站和arrival之间的站作为起始站没有座位,退票时同样更新
        for (int i = realDeparture + 1; i < realArrival; i++){
            int temp = i * stationNum;
            for (int j = i + 1; j < stationNum; j++){
                seatArray[temp + j] += -1;
            }
        }
        return true;
    }
}
