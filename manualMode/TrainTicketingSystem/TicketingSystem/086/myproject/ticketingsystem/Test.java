package ticketingsystem;

import java.io.*;
import java.util.*;

public class Test {
	private final static int threadNum = 48;
	private final static int routeNum = 20;
	private final static int coachNum = 30;
	private final static int seatNum = 100;
	private final static int stationNum = 10;
	private final static int testNum = 4000;

	public static void main(String[] args) throws Exception {
		final String file = "log.txt";
		try (FileReader fr = new FileReader(new File(file));
			 BufferedReader br = new BufferedReader(fr)) {
			String line;
			Queue<Node> queue = new LinkedList<>();
			while ((line = br.readLine()) != null) {
				String[] info = line.split(" ");
				Node node = new Node();
				if (info[0].equals("RemainTicket")) {
					node.op = OP.INQUIRY;
					node.left = Integer.parseInt(info[1]);
					node.routeID = Integer.parseInt(info[2]);
					node.departure = Integer.parseInt(info[3]);
					node.arrival = Integer.parseInt(info[4]);
				} else if (info[0].equals("TicketSoldOut")) {
					node.op = OP.BUY;
					node.tid = -1;
					node.routeID = Integer.parseInt(info[1]);
					node.departure = Integer.parseInt(info[2]);
					node.departure = Integer.parseInt(info[3]);
				} else {
					node.tid = Integer.parseInt(info[1]);
					node.passenger = info[2];
					node.routeID = Integer.parseInt(info[3]);
					node.coach = Integer.parseInt(info[4]);
					node.departure = Integer.parseInt(info[5]);
					node.arrival = Integer.parseInt(info[6]);
					node.seat = Integer.parseInt(info[7]);
					if (info[0].equals("TicketBought")) {
						node.op = OP.BUY;
					} else if (info[0].equals("TicketRefund")) {
						node.op = OP.REFUND;
					}
				}
				queue.add(node);
			}
			final TicketingDS single = new TicketingDS(routeNum, coachNum, seatNum, stationNum, threadNum);
			final TicketingDS multiple = (TicketingDS)deSerialize();
			while (!queue.isEmpty()) {
				Node node = queue.poll();
				if (node.op == OP.BUY) {
					single.buyTicket(passengerName(), node.routeID, node.departure, node.arrival);
				}
			}
			for (int k = 1; k <= routeNum; k++) {
				for (int i = 1; i < stationNum; i++) {
					for (int j = i + 1; j <= stationNum; j++) {
						int r1 = single.inquiry(k, i, j);
						int r2 = multiple.inquiry(k, i, j);
						if (r1 != r2) {
							System.out.println("fuck");
						}
					}
				}
			}
		}
	}

	static String passengerName() {
		Random rand = new Random();
		long uid = rand.nextInt(testNum);
		return "passenger" + uid;
	}
	private static Object deSerialize() {
		Object obj = null;
		try (FileInputStream fis = new FileInputStream(new File("tds.bin"));
		ObjectInputStream ois = new ObjectInputStream(fis)) {
			obj = ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj;
	}
}

enum OP{
	REFUND,
	BUY,
	INQUIRY
}

class Node {
	OP op;
	int departure;
	int arrival;
	int routeID;
	int coach;
	int seat;
	long tid;
	int left;
	String passenger;
}