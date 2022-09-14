package ticketingsystem;


import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class SingleThreadTest {
	public static void main(String[] args) {
		int[] threadNums = {4, 8, 16, 32, 64};
		TicketingSystem ticketingSystem = new TicketingDS(
				5,
				8,
				10,
				10,
				2
		);
		ticketingSystemCorrectnessTest(ticketingSystem);
		TicketingDS ticketingDS = new TicketingDS(3, 3, 3, 3, 4);
		ticketingSystemCorrectnessTest2(ticketingDS);

	}
	private static void ticketingSystemCorrectnessTest2(TicketingSystem ticketingSystem) {
		List<Ticket> ticketList = new ArrayList<>();
		Ticket ticket;
		int ticketNum;
		boolean refundSuccess;

		ticket = ticketingSystem.buyTicket("passenger0", 3, 2,3);
		ticketList.add(ticket);

		ticket = ticketingSystem.buyTicket("passenger1", 3, 1,3);
		ticketList.add(ticket);

		ticket = ticketingSystem.buyTicket("passenger2", 3, 1,3);
		ticketList.add(ticket);

		ticket = ticketingSystem.buyTicket("passenger3", 3, 2,3);
		ticketList.add(ticket);

		refundSuccess = ticketingSystem.refundTicket(ticketList.get(3));
		println(refundSuccess);
		Assert.assertTrue(refundSuccess);

		refundSuccess = ticketingSystem.refundTicket(ticketList.get(1));
		println(refundSuccess);
		Assert.assertTrue(refundSuccess);

		ticket = ticketingSystem.buyTicket("passenger4", 3, 1,2);
		ticketList.add(ticket);

		refundSuccess = ticketingSystem.refundTicket(ticketList.get(0));
		println(refundSuccess);
		Assert.assertTrue(refundSuccess);

		refundSuccess = ticketingSystem.refundTicket(ticketList.get(4));
		println(refundSuccess);
		Assert.assertTrue(refundSuccess);

		ticket = ticketingSystem.buyTicket("passenger5", 3, 2,3);
		ticketList.add(ticket);

		ticket = ticketingSystem.buyTicket("passenger6", 3, 2,3);
		ticketList.add(ticket);

		println(ticket);
	}

	private static void ticketingSystemCorrectnessTest(TicketingSystem ticketingSystem) {
	    int ticketNum;
	    Ticket ticket;
	    List<Ticket> ticketList = new ArrayList<>();
	    boolean refundSuccess;

	    // 初始查询余票
		ticketNum = ticketingSystem.inquiry(1, 1, 10);
		println("expected: " + 80 + " inquiry: " + ticketNum);
		Assert.assertEquals(ticketNum, 80);

		// 购买一张全程票
		ticket = ticketingSystem.buyTicket("jack", 1, 1, 10);
		ticketList.add(ticket);

		// 查询余票
		ticketNum = ticketingSystem.inquiry(1, 1, 10);
		println("expected: " + 79 + " inquiry: " + ticketNum);
		Assert.assertEquals(79, ticketNum);

		println(ticket.route + " " + ticket.seat);

		// 退上次买的票
		refundSuccess = ticketingSystem.refundTicket(ticketList.get(0));
		ticketList.remove(0);
		println("expected: " + true + " actual: " + refundSuccess);
		Assert.assertTrue(refundSuccess);

		// 退票后查询余票
		ticketNum = ticketingSystem.inquiry(1, 1, 10);
		println("expected: " + 80 + " inquiry: " + ticketNum);
		Assert.assertEquals(80, ticketNum);

		// 购买一张全程票
		ticket = ticketingSystem.buyTicket("jack", 1, 1, 10);
		ticketList.add(ticket);
		println("ticket1: " + ticket.tid + " seat: " +  ticket.seat);

		ticket = ticketingSystem.buyTicket("jack", 1, 5, 8);
		ticketList.add(ticket);

		println("ticket2: " + ticket.tid + " seat: " + ticket.seat);

		// 查询余票
		ticketNum = ticketingSystem.inquiry(1, 1, 10);
		println("expected: " + 78 + " inquiry: " + ticketNum);
		Assert.assertEquals(78, ticketNum);

		// 查询余票
		ticketNum = ticketingSystem.inquiry(1, 8, 9);
		println("expected: " + 79 + " inquiry: " + 79);
		Assert.assertEquals(ticketNum, 79);

		ticketNum = ticketingSystem.inquiry(2, 8, 9);
		println("expected: " + 80 + " inquiry: " + 80);
		Assert.assertEquals(ticketNum, 80);

		refundSuccess = ticketingSystem.refundTicket(ticketList.get(1));
		ticketList.remove(1);
		println("expected: " + true + " actual: " + refundSuccess);
		Assert.assertTrue(refundSuccess);

		refundSuccess = ticketingSystem.refundTicket(ticketList.get(0));
		ticketList.remove(0);
		println("expected: " + true + " actual: " + refundSuccess);
		Assert.assertTrue(refundSuccess);

		// 查询余票
		ticketNum = ticketingSystem.inquiry(1, 8, 9);
		println("expected: " + 80 + " inquiry: " + 80);
		Assert.assertEquals(ticketNum, 80);

		// 查询余票
		ticketNum = ticketingSystem.inquiry(1, 1, 10);
		println("expected: " + 80 + " inquiry: " + 80);
		Assert.assertEquals(80, ticketNum);

		for (int i = 0; i < 81; i++) {
			ticket = ticketingSystem.buyTicket("jack", 1, 5, 8);
			if (ticket != null) {
				ticketList.add(ticket);
				println(
						"i = " + i +
						" tid: " + ticket.tid +
						" route: " + ticket.route +
						" coach: " + ticket.coach +
						" seat: " + ticket.seat +
						" departure " + ticket.departure +
						" arrival " + ticket.arrival
				);
			} else {
				println("ticket is null");
			}
		}

		ticketNum = ticketingSystem.inquiry(1, 1, 10);
		println("expected: " + 0 + " inquiry: " + ticketNum);
		Assert.assertEquals(0, ticketNum);

		ticketNum = ticketingSystem.inquiry(1, 1, 6);
		println("expected: " + 0 + " inquiry: " + 0);
		Assert.assertEquals(ticketNum, 0);

		ticketNum = ticketingSystem.inquiry(1, 7, 9);
		println("expected: " + 0 + " inquiry: " + 0);
		Assert.assertEquals(ticketNum, 0);

		ticketNum = ticketingSystem.inquiry(1, 8, 9);
		println("expected: " + 80 + " inquiry: " + 80);
		Assert.assertEquals(ticketNum, 80);

		ticketNum = ticketingSystem.inquiry(1, 4, 5);
		println("expected: " + 80 + " inquiry: " + 80);
		Assert.assertEquals(ticketNum, 80);

		ticketNum = ticketingSystem.inquiry(1, 1, 9);
		println("expected: " + 0 + " inquiry: " + 0);
		Assert.assertEquals(ticketNum, 0);

		ticketNum = ticketingSystem.inquiry(1, 1, 10);
		println("expected: " + 0 + " inquiry: " + ticketNum);
		Assert.assertEquals(0, ticketNum);
		println(ticketNum);

		// 这里发现如果买了5-8站的票，那么如果退6-8站返回为true
		// 如果退3-8站则返回为false
		ticket = ticketList.get(ticketList.size() - 1);
		ticket.arrival = 9;
		refundSuccess = ticketingSystem.refundTicket(ticket);
		println(refundSuccess);
		Assert.assertFalse(refundSuccess);
		ticket.arrival = 8;

		ticketNum = ticketingSystem.inquiry(1, 1, 10);
		Assert.assertEquals(0, ticketNum);
		println(ticketNum);

		for (int i = 79; i >= 0; i--) {
			ticket = ticketList.get(i);
			refundSuccess = ticketingSystem.refundTicket(ticket);
			ticketList.remove(i);
			Assert.assertTrue(refundSuccess);

			ticketNum = ticketingSystem.inquiry(1, 1, 10);
//			println("expected: " + (80-i) + " inquiry: " + ticketNum);
			Assert.assertEquals(ticketNum, 80-i);
		}
		ticketNum = ticketingSystem.inquiry(1, 1, 10);
		println("expected: " + 80 + " inquiry: " + ticketNum);
		Assert.assertEquals(ticketNum, 80);

		// 测试错误的车票信息是否能退票
//		ticket = ticketingSystem.buyTicket("jack", 1, 5, 8);
//		ticketList.add(ticket);
//		println(
//						" tid: " + ticket.tid +
//						" route: " + ticket.route +
//						" coach: " + ticket.coach +
//						" seat: " + ticket.seat +
//						" departure " + ticket.departure +
//						" arrival " + ticket.arrival
//		);
	}

	private static void ticketingSystemThroughputTest(TicketingSystem ticketingSystem) {

	}

	private static void println(Object line) {
		System.out.println(line);
	}
}
