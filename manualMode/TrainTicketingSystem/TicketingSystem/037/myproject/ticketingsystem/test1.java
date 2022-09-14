package ticketingsystem;

public class Test {

	public static void main(String[] args) throws InterruptedException {
		final int threadnum = 4; // concurrent thread number
		final int routenum = 3; // route is designed from 1 to 3
		final int coachnum = 3; // coach is arranged from 1 to 5
		final int seatnum = 10; // seat is allocated from 1 to 20
		final int stationnum = 10; // station is designed from 1 to 5
		final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
        Ticket[] ticket=new Ticket[20];
       // inqureAll(tds);
        for(int k=0;k<8;k++) {
        	ticket[k]=tds.buyTicket("sss", 1, k+1, k+3);
        	System.out.printf("ticket %d tid is %d\n", k,ticket[k].tid);
        	System.out.printf("departure is %d ,arrival is %d\n", k+1,k+3);
        	inqureAll(tds);
        }
        inqureAll(tds);
		
		for(int k=0;k<8;k++) {
			System.out.printf("k is %d\n",k);
			if(tds.refundTicket(ticket[k])) {
				System.out.println("退票成功");
			}else {
				System.out.println("退票失败");
			}
			System.out.printf("ticket %d tid is %d\n", k,ticket[k].tid);
			System.out.printf("departure is %d ,arrival is %d\n", k+1,k+3);
			inqureAll(tds);
		}
	

		//ToDo
	    
	}
	static void inqureAll(TicketingDS tds) {
		for(int i=1;i<=9;i++)
			for(int j=i+1;j<11;j++) {
				System.out.printf("车次1，从%d到%d，余票还有%d\n",i,j,tds.inquiry(1, i, j));
			}
	}
}
