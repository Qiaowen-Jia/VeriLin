package ticketingsystem;

import java.util.concurrent.atomic.AtomicLong;


public class Seat {

    private final int SeatId; //座位id
    private AtomicLong BitMap; //用位图保存该座位对应的车站信息

    public Seat(final int seatId) {
        this.SeatId = seatId;
        this.BitMap = new AtomicLong(0);
    }

    public void setBitMap(AtomicLong BitMap){
        this.BitMap = BitMap;
    }

    public AtomicLong getBitMap(){
        return this.BitMap;
    }

    //查票
    public int inquiry(final int departure, final int arrival) {
        long preBitMap = this.BitMap.get();
        long newBitMap = 0;
        long base;
        long i = departure-1;


        do{
            base = 1;
            base = base << i;
            newBitMap = newBitMap | base;
            i++;
        }while(i<arrival-2);


        long result = newBitMap & preBitMap;

        if(result==0){
            return 1;
        }else{
            return 0;
        }
    }

    //购票,首先判断是否有重复区间,然后再合并位图
    public int buyTicket(final int departure, final int arrival) {
        long preBitMap = 0;
        long curBitMap = 0;
        long newBitMap = 0;
        long base;
        long i = departure-1;

        do{
            base = 1;
            base = base << i;
            newBitMap = newBitMap | base;
            i++;
        }while(i<arrival-2);


        do {
            preBitMap = this.BitMap.get();

            /*将newBitMap与preBitMap进行与操作,如果结果不为0则说明两者至少有一位为1
              这意味着至少有一个车站的当前座位与该方法提供的路线冲突,此时返回-1;否则更新位图。
             */
            long result = newBitMap & preBitMap;
            if (result != 0) {
                return -1;
            } else {
                curBitMap = newBitMap | preBitMap;   //更新位图
            }
        } while (!this.BitMap.compareAndSet(preBitMap, curBitMap));

        return this.SeatId;
    }



    //退票
    public boolean refundTicket(final int departure, final int arrival) {
        long preBitMap = 0; //旧位图
        long curBitMap = 0; //合并之后当前的位图
        long newBitMap = 0; //仅含有退票相关的车站信息的位图
        long base;
        long i = departure - 1;

        do{
            base = 1;
            base = base << i;
            newBitMap = newBitMap | base;
            i++;
        }while(i<arrival-2);

        newBitMap = ~newBitMap;

        do {
            preBitMap = this.BitMap.get();
            curBitMap = newBitMap & preBitMap;
        } while (!this.BitMap.compareAndSet(preBitMap, curBitMap));

        return true;
    }

}
