package ticketingsystem;

import java.util.ArrayList;
import java.util.Random;

public class Test {
    final static int routeNum = 20;
    final static int coachNum = 15;
    final static int seatNum = 100;
    final static int stationNum = 10;
    static int threadNum = 2;

    final static int testNum = 500_000;
    final static int refundRatio = 5; // refund ticket: 5%
    final static int buyRatio = 15; // buy ticket: 15%
    final static int inquireRatio = 80; // inquire ticket: 80%

    static String passengerName() {
        Random rand = new Random();
        long uid = rand.nextInt(testNum);
        return "P" + uid;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.printf(
                "*Configuration*\t| %d routes, %d coaches, %d seats, %d stations, %d threads, %d tests/thread\n",
                routeNum,
                coachNum,
                seatNum,
                stationNum,
                threadNum,
                testNum);

        final TicketingDS tds = new TicketingDS(routeNum, coachNum, seatNum, stationNum, threadNum);
        Thread[] threads = new Thread[threadNum];

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadNum; ++i) {
            threads[i] = new Thread(() -> {
                long inquiryTime = 0;
                long refundTime = 0;
                long buyTime = 0;
                long inquiryCount = 0;
                long refundCount = 0;
                long buyCount = 0;
                Random random = new Random();
                Ticket ticket;
                ArrayList<Ticket> tickets = new ArrayList<>();

                for (int i1 = 0; i1 < testNum; ++i1) {
                    int decision = random.nextInt(100);
                    if (decision < refundRatio && tickets.size() > 0) {
                        // refund one random ticket
                        int ticketId = random.nextInt(tickets.size());
                        ticket = tickets.remove(ticketId);

                        long refundStartTime = System.currentTimeMillis();
                        if (ticket != null) {
                            if (tds.refundTicket(ticket)) {
//                                System.out.printf(
//                                        "*Refund* \t\t| TID %013d, " +
//                                                "Passenger %s, Route %d, From %d to %d, Coach %d, Seat %d\n",
//                                        ticket.tid,
//                                        ticket.passenger,
//                                        ticket.route,
//                                        ticket.departure,
//                                        ticket.arrival,
//                                        ticket.coach,
//                                        ticket.seat);
                            } else {
//                                System.out.printf(
//                                        "*Error* \t\t| Refund TID %013d, " +
//                                                "Passenger %s, Route %d, From %d to %d, Coach %d, Seat %d\n",
//                                        ticket.tid,
//                                        ticket.passenger,
//                                        ticket.route,
//                                        ticket.departure,
//                                        ticket.arrival,
//                                        ticket.coach,
//                                        ticket.seat);
                            }
                        }
                        long refundEndTime = System.currentTimeMillis();
                        refundTime += refundEndTime - refundStartTime;
                        ++refundCount;
//                        System.out.flush();
                    } else if (refundRatio <= decision && decision < refundRatio + buyRatio) {
                        // buy one ticket
                        String passenger = passengerName();
                        int route = random.nextInt(routeNum) + 1;
                        int departure = random.nextInt(stationNum - 1) + 1;
                        int arrival = departure + random.nextInt(stationNum - departure) + 1;

                        long buyStartTime = System.currentTimeMillis();
                        ticket = tds.buyTicket(passenger, route, departure, arrival);
                        long buyEndTime = System.currentTimeMillis();
                        buyTime += buyEndTime - buyStartTime;
                        ++buyCount;

                        if (ticket != null) {
                            tickets.add(ticket);
//                            System.out.printf(
//                                    "*Buy* \t\t\t| TID %013d, " +
//                                            "Passenger %s, Route %d, From %d to %d, Coach %d, Seat %d\n",
//                                    ticket.tid,
//                                    ticket.passenger,
//                                    ticket.route,
//                                    ticket.departure,
//                                    ticket.arrival,
//                                    ticket.coach,
//                                    ticket.seat);
                        } else {
//                            System.out.printf(
//                                    "*Buy* \t\t\t| Sold out on Route %d, From %d to %d\n",
//                                    route,
//                                    departure,
//                                    arrival);
                        }
//                        System.out.flush();
                    } else if (refundRatio + buyRatio <= decision && decision < refundRatio + buyRatio + inquireRatio) {
                        // make inquiry
                        int route = random.nextInt(routeNum) + 1;
                        int departure = random.nextInt(stationNum - 1) + 1;
                        int arrival = departure + random.nextInt(stationNum - departure) + 1;

                        long inquiryStartTime = System.currentTimeMillis();
                        int ticketCount = tds.inquiry(route, departure, arrival);
                        long inquiryEndTime = System.currentTimeMillis();
                        inquiryTime += inquiryEndTime - inquiryStartTime;
                        ++inquiryCount;
//                        System.out.printf(
//                                "*Inquire* \t\t| %d ticket(s) left on Route %d, From %d to %d\n",
//                                ticketCount,
//                                route,
//                                departure,
//                                arrival);
//                        System.out.flush();
                    }
                }
//                System.out.printf(
//                        "*Statistics* \t| Buy %d times in %d ms, Refund %d times in %d ms, Inquiry %d times in %d ms\n",
//                        buyCount, buyTime,
//                        refundCount, refundTime,
//                        inquiryCount, inquiryTime);
                System.out.printf(
                        "%d | %d | %d | %d | %d | %d\n",
                        buyCount, buyTime,
                        refundCount, refundTime,
                        inquiryCount, inquiryTime);
                System.out.flush();
            });
            threads[i].start();
        }

        for (int i = 0; i < threadNum; ++i) {
            threads[i].join();
        }
		long endTime = System.currentTimeMillis();
        double usedTime = (endTime - startTime) / 1000.0;
		System.out.printf(
				"*Statistics* \t| Finished in %.2f s, throughput %.2f op/s\n",
				usedTime,
                testNum * threadNum / usedTime);
	}
}
