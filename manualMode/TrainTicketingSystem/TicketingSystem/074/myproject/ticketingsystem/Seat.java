package ticketingsystem;

import java.util.concurrent.atomic.AtomicInteger;

public class Seat {
    AtomicInteger seatSituations=new AtomicInteger(0);//0代表所有车站之间该座位均可用
    //查询
    Boolean ifAvailable(int mask){
        if((mask&seatSituations.get())==0){//可用
            return true;
        }
        return false;
    }
    //买票
    Boolean ifAvailableAndSet(int mask){
        //乐观同步，非阻塞同步，无锁链表
        int temp=seatSituations.get();
        while((mask&temp)==0){
            if(seatSituations.compareAndSet(temp, mask|temp)){
                return true;//有空位并修改成功
            }
            temp=seatSituations.get();//重新尝试
        }
        return false;//失败，该座位没有空位
    }
    //退票
	public void refund(int mask) {
        int temp=seatSituations.get();
        while(!seatSituations.compareAndSet(temp, mask&temp)){
            temp=seatSituations.get();
        }
    }
}

