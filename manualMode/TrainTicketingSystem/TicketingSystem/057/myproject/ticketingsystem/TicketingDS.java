package ticketingsystem;
import java.util.concurrent.locks.ReentrantLock;

public class TicketingDS implements TicketingSystem {
	private Train[] train;              //列车成员；
	public Train[] getTrain() {
		return train;
	}

	public void setTrain(Train[] train) {
		this.train = train;
	}

	private int routenum;				//列车总数；
	private int coachnum;               //每列列车的车厢数量；
	private int seatnum;                //每个车厢的座位数量；
	private int stationnum;             //每列火车的车站数量；
	private int ticket_next_id;         //每张票的id，独一无二；
	private ReentrantLock tds_lock;     //可重入锁；
//    Ticket[]  ticket;
	public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum){
		this.routenum = routenum;
		this.coachnum = coachnum;
		this.seatnum = seatnum;
		this.stationnum = stationnum;
		this.setTicket_next_id(1);
		this.tds_lock = new ReentrantLock();
		train = new Train[routenum];
		for(int i = 0; i < routenum;i++) {
			train[i] = new Train(coachnum,seatnum,stationnum);
		}
//		ticket = new Ticket[routenum * coachnum * seatnum * stationnum];
		
	}
	
	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
		
//		System.out.println("buyTicket in ");
		int route_order   = route - 1;
		int dep_sta_order = departure - 1;
		int arr_sta_order = arrival - 1;
//		check arg		
		
		if(route > this.routenum) {
			System.out.println("route out range");
			return null;
		}

		Coach[] temp_coach = train[route_order].getCoach();
		int    next_coach_choice = train[route_order].getNext_coach_choice();    //先查找利用率最小的车厢；
		Seat[] seat = temp_coach[next_coach_choice].getLocal_seat();
		
		{
			int i = 0;
			for( i = 0 ; i < this.seatnum; i++) {
				int re = seat[i].buy_seat(dep_sta_order, arr_sta_order);
				if(1 ==  re ) {
					 tds_lock.lock();
					 Ticket ticket  = register_ticket(ticket_next_id, passenger,route, next_coach_choice + 1, i + 1, departure, arrival);
				     ticket_next_id++;
				     long temp_occup_rate = temp_coach[next_coach_choice].getCoach_occupancy_rate();
				     temp_coach[next_coach_choice].setCoach_occupancy_rate(temp_occup_rate + arr_sta_order - dep_sta_order);
				     tds_lock.unlock();
				     return   ticket;
				}
				else if(-1 == re){
					System.out.println(" arr error");
					return  null;
				}
				else{
                    //此座位不满足购买者的要求；
				}
			}
			if(i == this.seatnum) {              //在最佳候选车厢中无法找到合适的座位时，从第一个车厢开始，找合适的位置；
				int new_coachnum = 0;
				while(new_coachnum < this.coachnum) {
					if(new_coachnum == next_coach_choice) {
						new_coachnum++;
						continue;
					}
					Seat[] new_seat = temp_coach[new_coachnum].getLocal_seat();
					int j = 0;
					for( j = 0 ; j < this.seatnum; j++) {
						int re = new_seat[j].buy_seat(dep_sta_order, arr_sta_order);
						if(1 ==  re ) {	
							 tds_lock.lock();
						     Ticket ticket  = register_ticket(ticket_next_id, passenger,route, new_coachnum + 1, j + 1, departure, arrival);
						     ticket_next_id++;
						     long temp_occup_rate = temp_coach[new_coachnum].getCoach_occupancy_rate();
						     temp_coach[new_coachnum].setCoach_occupancy_rate(temp_occup_rate + arr_sta_order - dep_sta_order);
						     tds_lock.unlock();
						     return ticket;
						}
						else if(-1 == re){
							System.out.println(" buy arg error");
							return null;
						}
						else
						{
							
						}
					}
					new_coachnum++;
				}
			}
		}
//		System.out.println("buyTicket end ");
		return null;
		
	}
	
	
	
	public int inquiry(int route, int departure, int arrival) {
		
//		System.out.println("inquiry in ");
		int route_order   = route - 1;
		int dep_sta_order = departure - 1;
		int arr_sta_order = arrival - 1;
		int remand_ticket_num = 0;
//		check arg		
		if(route > this.routenum) {
			System.out.println("route out range");
			return -1;
		}

		Coach[] temp_coach ;
//		int    next_coach_choice ;
		Seat[] seat ;
		
		
		temp_coach = train[route_order].getCoach();
		for(int k = 0; k < train[route_order].getCoachnum(); k++) {
			seat = temp_coach[k].getLocal_seat();
			{
				int i;
				for( i = 0 ; i < this.seatnum; i++) {
					int re = seat[i].ifcanbuy_seat(dep_sta_order, arr_sta_order);
					if(1 ==  re ) {
						remand_ticket_num++;
					}
					else if(re < 0){
						continue;
					}
				}
			}	
		}
//		next_coach_choice = train[route_order].getNext_coach_choice();
//		seat = temp_coach[k].getLocal_seat();
//		{
//			int i;
//			for( i = 0 ; i < this.seatnum; i++) {
//				int re = seat[i].ifcanbuy_seat(dep_sta_order, arr_sta_order);
//				if(1 ==  re ) {
//					return 1;
//				}
//				else if(re < 0){
//					return re;
//				}
//			}
//			if(i == this.seatnum) {
//				i = 0;
//				seat = temp_coach[i].getLocal_seat();
//				while(i < this.coachnum) {
//					if(i == next_coach_choice) {
//						i++;
//						continue;
//					}
//					int j = 0;
//					for( j = 0 ; j < this.seatnum; j++) {
//						int re = seat[j].ifcanbuy_seat(dep_sta_order, arr_sta_order);
//						if(1 == re  ) {
//							return 1;
//						}
//						else if(re <  0) {
//							return re;
//						}
//					}
//					i++;
//				}
//			}
//		}
//		System.out.println("inquiry end ");
		return remand_ticket_num;
	}
	
	
	
	public boolean refundTicket(Ticket ticket) {
		
//		System.out.println("refundTicket in ");
		int route_order   = ticket.route - 1;
		int dep_sta_order = ticket.departure - 1;
		int arr_sta_order = ticket.arrival - 1;
//		check arg		
		if(ticket.route > this.routenum) {
			System.out.println("route out range");
			return false;
		}

		Coach[] temp_coach = train[route_order].getCoach();
		Seat[] seat = temp_coach[ticket.coach-1].getLocal_seat();
		{
			int i = 0;
			for(i = dep_sta_order; i < arr_sta_order; i++) {
				if(1 != seat[ticket.seat-1].getTicket_id()[i]) {
				    System.out.println("refund ticket info error");
					return false;
				}
			}	
			if(i == arr_sta_order) {
				if(1 == seat[ticket.seat-1].seat_refund(dep_sta_order,arr_sta_order)) {
					return true;
				}
//				for(i = dep_sta_order; i < arr_sta_order; i++) {
//					seat[ticket.seat].seat_refund(i);
//
//				}
			}

		}

		
//		System.out.println("refundTicket end ");
		return false;
		
	}

	public int getRoutenum() {
		return routenum;
	}

	public void setRoutenum(int routenum) {
		this.routenum = routenum;
	}

	public int getCoachnum() {
		return coachnum;
	}

	public void setCoachnum(int coachnum) {
		this.coachnum = coachnum;
	}

	public int getSeatnum() {
		return seatnum;
	}

	public void setSeatnum(int seatnum) {
		this.seatnum = seatnum;
	}

	public int getStationnum() {
		return stationnum;
	}

	public void setStationnum(int stationnum) {
		this.stationnum = stationnum;
	}
	
	public void show_info() {
		for(int i = 0; i < this.routenum; i++) {
			System.out.println(" train : " + i + "  ");
			train[i].show_train_info();		
			System.out.println("train " + i +" end" + "   <<<<<<<<<<<<<<<<<<<<<<<<<<<< ");
		}
	}

	public int getTicket_next_id() {
		return ticket_next_id;
	}

	public void setTicket_next_id(int ticket_next_id) {
		this.ticket_next_id = ticket_next_id;
	}
	

	private Ticket register_ticket(long tid, String passenger,int route, int coach, int seat, int departure, int arrival) {
		Ticket ticket = new Ticket();
		ticket.tid = tid;
		ticket.passenger = passenger;
		ticket.route = route;
		ticket.coach = coach;
		ticket.seat = seat;
		ticket.departure = departure;
		ticket.arrival = arrival;
		
		return ticket;
	}

	public ReentrantLock getTds_lock() {
		return tds_lock;
	}

	public void setTds_lock(ReentrantLock tds_lock) {
		this.tds_lock = tds_lock;
	}

}


class Seat{
	private long[] ticket_id;  //该座位的占用情况；-1表示座位在index站未利用，1表示座位在index站已经利用；
	private int remind_sta;    //该座位可以乘坐的剩余总站数；
	private int total_sta;     //总站数；
    public int getTotal_sta() {
		return total_sta;
	}



	private ReentrantLock lock;//可重入锁；
	


	public ReentrantLock getLock() {
		return lock;
	}

	public void setLock(ReentrantLock lock) {
		this.lock = lock;
	}

	public Seat(int station_num) {
		this.ticket_id = new long[station_num];
		this.remind_sta = station_num;
		this.total_sta = station_num;
		this.lock = new ReentrantLock();
		for(int i = 0; i < station_num; i++) {
			this.ticket_id[i] = -1;
		}
	}

	public void show_seat_info() {
		System.out.print("can buy station : " );
		for(int i = 0; i < this.remind_sta; i++) {
			if(-1 == ticket_id[i]  ) {
				System.out.print(i + " ");
			}

		}
		System.out.println("");

	}
	
	public int getRemind_sta() {
		return remind_sta;
	}
	public void setRemind_sta(int remind_sta) {
		this.remind_sta = remind_sta;
	}
	public long[] getTicket_id() {
		return ticket_id;
	}
	public void setTicket_id(long[] ticket_id) {
		this.ticket_id = ticket_id;
	}
	
	public int ifcanbuy_seat(int start_sta, int end_sta) {
		int rec = 0;
        //arg check;
		if( ( ((start_sta >= 0) && (start_sta < (total_sta-1))) && ((end_sta >= 1) && (end_sta < total_sta)) ) && (end_sta > start_sta) ){
			//arg right;
		}
		else
		{
			System.out.println("ifcanbuy_seat arr error: " + "start_sta" + start_sta + "  " + "end_sta" + end_sta + "total:" + total_sta);
			return -1;// arg error;
		}
		
		if(remind_sta < (end_sta - start_sta)){
			return 0;
		}
		
		{
//			this.lock.lock();
			int i = 0; 
			for(i = start_sta; i < end_sta; i++) {
				if(-1 != this.ticket_id[i]) {
					rec =  0;
				}
			}
			if(i == end_sta) {
				rec =  1;
			}
//			this.lock.unlock();
		}

		return rec;
		
	}
	
	public int buy_seat(int start_sta, int end_sta) {
        //arg check;
		int rec = 0;
		if( ( ((start_sta >= 0) && (start_sta < (total_sta-1))) && ((end_sta >= 1) && (end_sta < total_sta)) ) && (end_sta > start_sta) ){
			//arg right;
		}
		else
		{
			System.out.println("buy_seat arr error: " + "start_sta" + start_sta + "  " + "end_sta" + end_sta);
			return -1;// arg error;
		}
		

		
		if(this.remind_sta < (end_sta - start_sta)){
			return 0;
		}
		
		{
			this.lock.lock();
			int i = 0; 
			for(i = start_sta; i < end_sta; i++) {      //检测该座位是否满足购票需求；
				if(-1 != this.ticket_id[i]) {
					rec =  0;
					break;
				}
			}
			if(i == end_sta) {
				for(i = start_sta; i < end_sta; i++) {
					this.ticket_id[i] = 1;
				}
				this.remind_sta = remind_sta  - (end_sta - start_sta);
				rec =  1;
				
			}
			this.lock.unlock();
		}
		return rec;
		
	}
	

	public int seat_refund(int start_sta ,int end_sta) {
		int rec = -1;
		if( ( ((start_sta >= 0) && (start_sta < (total_sta-1))) && ((end_sta >= 1) && (end_sta < total_sta)) ) && (end_sta > start_sta) ){
			//arg right;
		}
		else
		{
			System.out.println("seat_refund arr error: " + "start_sta" + start_sta + "  " + "end_sta" + end_sta);
			return -1;// arg error;
		}
		
		lock.lock();
		int i = 0;
		for(i = start_sta; i < end_sta; i++) {
			if(1 != this.getTicket_id()[i]) {
			    System.out.println("refund ticket info error");
				rec = -1;
			}
		}
		if(i == end_sta) {
			for(i = start_sta; i < end_sta; i++) {
				ticket_id[i] = -1;
			}
			this.remind_sta = remind_sta  + (end_sta - start_sta);
	        rec  =  1;
		}
		else {

		}
        lock.unlock();
		return rec;
	}
	
}

class Coach{
	private long coach_occupancy_rate;       //车厢的座位利用情况
    private int seat_num;                    //座位数目
    private Seat[] Local_seat;               //座位成员；
    
	public Coach(int seat_num,int station_num) {
		Local_seat = new Seat[seat_num];
		this.seat_num = seat_num;
		this.coach_occupancy_rate = 0;
		for(int i = 0; i < this.seat_num;i++)
		{
			Local_seat[i] = new Seat(station_num);
		}
	}
	
	
	public long getCoach_occupancy_rate() {
		return coach_occupancy_rate;
	}
	public void setCoach_occupancy_rate(long coach_occupancy_rate) {
		this.coach_occupancy_rate = coach_occupancy_rate;
	}
	
	public int getSeat_num() {
		return seat_num;
	}
	public void setSeat_num(int seat_num) {
		this.seat_num = seat_num;
	}


	public Seat[] getLocal_seat() {
		return Local_seat;
	}
	public void setLocal_seat(Seat[] local_seat) {
		Local_seat = local_seat;
	}
	
	public void show_coach_info() {
		for(int i = 0; i < this.seat_num;i++)
		{
			System.out.print("seat :" + i + "    ");
			Local_seat[i].show_seat_info();
		}
	}
}

class Train{
	private Coach[]  coach;         //车厢成员
	private int end_station;        //终点站编号；
	private int coachnum;           //车厢数量；
	private int next_coach_choice;  //推荐的占用率最小的车厢；初始化为0；
	
	Train(int coachnum,int seatnum,int stationnum){
		coach = new Coach[coachnum];
		this.coachnum = coachnum;
		this.end_station = stationnum;
		this.next_coach_choice = 0;                     
		for(int i = 0; i < coachnum;i++) {
			coach[i] = new Coach(seatnum,stationnum);  //最后一站不售票；
		}
	}

	public Coach[] getCoach() {
		return coach;
	}

	public void setCoach(Coach[] coach) {
		this.coach = coach;
	}

	public int getEnd_station() {
		return end_station;
	}

	public void setEnd_station(int end_station) {
		this.end_station = end_station;
	}


	public int getCoachnum() {
		return coachnum;
	}

	public void setCoachnum(int coachnum) {
		this.coachnum = coachnum;
	}
	

	public int getNext_coach_choice() {
		int i = 0;
		int best_choice = 0;
		for(i = 0; i < this.coachnum; i++)
		{
			if(this.coach[best_choice].getCoach_occupancy_rate()  > this.coach[i].getCoach_occupancy_rate()) {
				best_choice = i;
			}
		}
		next_coach_choice = best_choice;
		return next_coach_choice;
	}

	public void setNext_coach_choice(int next_coach_choice) {
		this.next_coach_choice = next_coach_choice;
	}
	
	public void show_train_info() {
		for(int i = 0; i < this.coachnum;i++)
		{
			System.out.println("coach :" + i);
			coach[i].show_coach_info();
			System.out.println("*********************");
		}
	}

}
