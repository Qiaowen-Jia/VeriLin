package ticketingsystem;

public class Route {
    private int routeId;
    private int coachNum;
    private int seatNum;
    private int stationNum;

    public Seat[] seatArray;

    public int[] leftSeat;

    public Route(int routeid, int coachnum, int seatnum, int stationnum){
        routeId = routeid;
        coachNum = coachnum;
        seatNum = seatnum;
        stationNum = stationnum;
        seatArray = new Seat[coachNum*seatNum];
        leftSeat = new int[stationNum*stationNum];
        for (int i = 0; i < stationNum; i++){
            int temp = i * stationNum;
            for (int j = 0; j < stationNum; j++){
                leftSeat[temp + j] = coachNum*seatNum;
            }
        }
        for (int i = 0; i < coachNum; i++){
            int temp = i * seatNum;
            for (int j = 0; j < seatNum; j++){
                seatArray[temp + j] = new Seat(stationNum);
            }
        }
    }
    synchronized public int[] buyTicket(int departure, int arrival){
        if (leftSeat[(departure - 1) * stationNum + arrival - 1] == 0)
            return null;
        int coach = 0;
        int seat = 0;
        int[] couple;
        int[] oldSeatArray = new int[stationNum * stationNum];

            for (int i = 0; i < coachNum; i++){
                int temp = i * seatNum;
                for (int j = 0; j < seatNum; j++){
                    if (seatArray[temp + j].isAvailable(departure, arrival)){
                        for (int m = 0; m < stationNum; m++){
                            int temp_beta = m * stationNum;
                            for (int n = 0; n < stationNum; n++){
                                oldSeatArray[temp_beta + n] = seatArray[temp + j].seatArray[temp_beta + n];
                            }
                        }
                        if (seatArray[temp + j].takeSeat(departure, arrival)){
                            coach = i+1;
                            seat = j+1;
                            break;
                        }
                    }
                }
                if (coach != 0 && seat != 0){
                    break;
                }
            }
            if (coach != 0 && seat != 0){
                updateLeftSeat(coach, seat, oldSeatArray);
                couple = new int[]{coach, seat};
                return couple;
            }
            else {
                return null;
            }
    }
    public int inquiryTicket(int departure, int arrival){
        return leftSeat[(departure - 1) * stationNum + arrival - 1];
    }   
    synchronized public boolean refundTicket(Ticket ticket){
        int departure = ticket.departure;
        int arrival = ticket.arrival;
        int coach = ticket.coach;
        int seat = ticket.seat;
        int[] oldSeatArray = new int[stationNum * stationNum];
        int temp_beta = (coach - 1) * seatNum;
        for (int i = 0; i < stationNum; i++){
            int temp = i * stationNum;
            for (int j = 0; j < stationNum; j++){
                oldSeatArray[temp + j] = seatArray[temp_beta + seat - 1].seatArray[temp + j];
            }
        }
        if (seatArray[temp_beta+ seat-1].refundSeat(departure, arrival)){
            updateLeftSeat(coach, seat, oldSeatArray);
            return true;
        }
        return false;
    } 

    private void updateLeftSeat(int coach, int seat, int[] oldseatarray){
        int temp_beta = (coach - 1) * seatNum;
        for (int i = 0; i < stationNum; i++){
            int temp = i * stationNum;
            for (int j = i + 1; j < stationNum; j++){
                int delt = seatArray[temp_beta+ seat - 1].seatArray[temp + j] - oldseatarray[temp + j];
                if (delt > 0 && oldseatarray[temp + j] == 0)
                    leftSeat[temp + j] += -1;
                else if (delt < 0 && seatArray[temp_beta + seat - 1].seatArray[temp + j] == 0)
                    leftSeat[temp + j] += 1;
                else 
                    continue;    
            }
        }
    }
    /*
    private int countLeftSeat(int departure, int arrival){
        int count = 0;
        for (int i = 0; i < stationNum; i++){
            for (int j = 0; j < stationNum; j++){
                if (seatArray[i][j].isAvailable(departure, arrival)){
                    count++;
                }
            }
        }
        return count;
    }
    */
}
