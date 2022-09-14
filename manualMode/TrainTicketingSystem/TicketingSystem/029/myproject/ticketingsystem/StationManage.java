package ticketingsystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StationManage {
    private Station[] stations;
    private int seatNum;

    public StationManage(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
        this.seatNum = seatNum;
        stations = new Station[stationNum - 1];
        for (int i = 0; i < stations.length; ++i)
            stations[i] = new Station(routeNum, coachNum, seatNum);
    }

    public void occupy(MyTicket it) {
        int s = it.departure - 1;
        int e = it.arrival - 1;
        for (int i = s; i < e; ++i)
            stations[i].occupy(it.route, it.coach, it.seat);    //将每个车站对应座位的比特置为1
    }

    public void free(MyTicket it) {
        int s = it.departure - 1;
        int e = it.arrival - 1;
        for (int i = s; i < e; ++i)
            stations[i].free(it.route, it.coach, it.seat);      //将每个车站对应座位的比特置为0
    }

    private int[] getOrBits(int route, int departure, int arrival) {
        int s = departure;
        int e = arrival - 1;
        int[] bits = stations[s - 1].snapshot(route);         //遍历从s-1处开始
        for (int i = s; i < e; ++i) {
            int[] bm = stations[i].snapshot(route);
            for (int j = 0; j < bits.length; ++j)
                bits[j] |= bm[j];                             //或操作，压缩同一车次不同站点的信息
        }
        return bits;
    }

    private MyTicket toInsideTicket(int index, int route, int departure, int arrival) {
        int seat = index % seatNum + 1;
        int coach = index / seatNum + 1;
        return new MyTicket(route, coach, seat, departure, arrival);
    }

    //以数组的形式返回车票信息
    public List<MyTicket> locateAvailables(int route, int departure, int arrival) {
        ArrayList<MyTicket> location = new ArrayList<>();
        int[] bits = getOrBits(route, departure, arrival);
        int size = Station.bitsElementSize();
        for (int i = 0; i < bits.length; ++i) {
            List<Integer> l = BitManage.locateZeros(bits[i]);     //座位在当前车次的Index数组
            for (int index : l)
                location.add(toInsideTicket(index + i * size, route, departure, arrival));
        }
        Collections.shuffle(location);      //打乱数组，减少并发冲突
        return location;
    }
    //以数值的形式返回剩余票数
    public int countAvailables(int route, int departure, int arrival) {
        int count = 0;
        int[] bitMap = getOrBits(route, departure, arrival);
        for (int i = 0; i < bitMap.length; ++i){
            count += BitManage.countZeros(bitMap[i]);
        }
        return count;
    }
}