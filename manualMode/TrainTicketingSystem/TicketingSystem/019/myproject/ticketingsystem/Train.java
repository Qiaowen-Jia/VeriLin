package ticketingsystem;

import java.util.concurrent.atomic.AtomicInteger;

public class Train {
    int seat_num;  //每列车厢的车座数量
    int coach_num;  //车厢数量
    int station_num;  //经过的站点数量
    int total_seat_num;  //每列车的车座总数  =seat_num*coach_num
    AtomicInteger [][]seat_state_in_station;  //每个车座在某站的状态 (i，j)表示从i->i+1站的第j个座已经被预定
    AtomicInteger [][]seat_reamin_between_station;  //（a,b）表示该车从a站->b站的余票数量
    AtomicInteger [][][]domain_affected_by_seat;  // （i,a,b）>0，表示车座i对a->b站有影响，造成余票减少
    
    public Train(int seat_num,int coach_num,int station_num){
        this.seat_num = seat_num;
        this.coach_num = coach_num;
        this.station_num = station_num;
        total_seat_num = seat_num*coach_num;
        seat_state_in_station = new AtomicInteger[station_num][total_seat_num];
        seat_reamin_between_station = new AtomicInteger[station_num][station_num];
        domain_affected_by_seat = new AtomicInteger[total_seat_num][station_num][station_num];
        for(int i=0;i<station_num;i++){
            for(int j=0;j<total_seat_num;j++){
                seat_state_in_station[i][j]= new AtomicInteger(0);
            }
            for(int j=0;j<station_num;j++){
                seat_reamin_between_station[i][j]= new AtomicInteger(total_seat_num);
            }
        }
        for(int i=0;i<total_seat_num;i++){
            for(int j=0;j<station_num;j++){
                for(int k=0;k<station_num;k++){
                    domain_affected_by_seat[i][j][k]= new AtomicInteger(0);
                }
            }
        }
    }

    public int trySellSeat(int i,int departure,int arrival){
        int end = arrival;
        if(seat_state_in_station[departure][i].get()==0){
            for(int j=departure+1;j<=arrival;j++){
                if(seat_state_in_station[j][i].get()==1){
                    end = j-1;
                    break;
                }
            }
        }else return departure-1;
        return end;
    }

    public int sellSeat(int departure,int arrival){
        for(int i=0;i<total_seat_num;i++){
            int end = trySellSeat(i,departure,arrival);
           // System.out.println(end+" "+arrival);
            if(end==arrival){
                for(int j=departure;j<=arrival;j++){
                    seat_state_in_station[j][i].compareAndSet(0, 1);
                }
                for(int j=0;j<departure;j++)
                    for(int k=departure;k<station_num;k++)
                        if(domain_affected_by_seat[i][j][k].getAndIncrement()==0){
                            seat_reamin_between_station[j][k].getAndDecrement();
                        }
                for(int j=departure;j<=arrival;j++)
                    for(int k=j;k<station_num;k++)
                        if(domain_affected_by_seat[i][j][k].getAndIncrement()==0){
                            seat_reamin_between_station[j][k].getAndDecrement();
                        }
                return i;
            }
        }
        return -1;
    }

    public int findSeat(int departure,int arrival){
        return seat_reamin_between_station[departure][arrival].get();
    }


    public boolean spareSeat(Ticket t){
        int departure = t.departure-1;
        int arrival = t.arrival-2;
        int seat_id = t.seat-1 + seat_num*(t.coach-1); 
        for(int j=0;j<departure;j++)
            for(int k=departure;k<station_num;k++)
                if(domain_affected_by_seat[seat_id][j][k].decrementAndGet()==0){
                    seat_reamin_between_station[j][k].getAndIncrement();
                }
        for(int j=departure;j<=arrival;j++)
            for(int k=j;k<station_num;k++)
                if(domain_affected_by_seat[seat_id][j][k].decrementAndGet()==0){
                    seat_reamin_between_station[j][k].getAndIncrement();
                }
        for(int i=departure;i<=arrival;i++){
            if(!seat_state_in_station[i][seat_id].compareAndSet(1, 0)){
                return false;
            }
        } 
        return true;
    }
}