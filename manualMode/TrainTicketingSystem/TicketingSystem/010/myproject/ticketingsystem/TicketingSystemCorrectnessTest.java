package ticketingsystem;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Jianyong Feng
 **/
public class TicketingSystemCorrectnessTest {

    private TicketingSystemInfo ticketingSystemInfo;

    private int threadNum; // test thread number

    private int operationNum; // operations per thread
    private int refundPercent; // return ticket operation percent
    private int buyPercent; // buy ticket operation percent
    private int inquiryPercent; //inquiry ticket operation percent

    private int testNum; // times of test

    private Map<Long, Ticket> boughtMap;
    private Map<Long, Ticket> refundMap;

    private int totalSeatNum;


    public TicketingSystemCorrectnessTest(
            TicketingSystemInfo ticketingSystemInfo,
            int threadNum, int operationNum, int refundPercent, int buyPercent,
            int inquiryPercent, int testNum) {
        this.ticketingSystemInfo = ticketingSystemInfo;
        this.threadNum = threadNum;
        this.operationNum = operationNum;
        this.refundPercent = refundPercent;
        this.buyPercent = buyPercent;
        this.inquiryPercent = inquiryPercent;
        this.testNum = testNum;
        this.totalSeatNum = ticketingSystemInfo.getCoachNum() * ticketingSystemInfo.getSeatNum();
        boughtMap = new ConcurrentHashMap<>();
        refundMap = new ConcurrentHashMap<>();
    }

    public void CorrectnessTest() throws InterruptedException {
        printTestInfo();
        for (int i = 0; i < testNum; i++) {
            System.out.println("----------The " + (i + 1) + "th " + "test Start----------");
            TicketingSystem ticketingSystem = new TicketingDS(
                    ticketingSystemInfo.getRouteNum(), ticketingSystemInfo.getCoachNum(),
                    ticketingSystemInfo.getSeatNum(), ticketingSystemInfo.getStationNum(),
                    threadNum
            );
            boughtMap.clear();
            refundMap.clear();
            boolean success = simulateMultiThread(ticketingSystem, threadNum);
            if (!success) return;
            System.out.println("Bought Map Size: " + boughtMap.size());
            System.out.println("Refund Map Size: " + refundMap.size());
            BitSet[][] seatStatus = seatStatusGenerate(ticketingSystemInfo.getRouteNum(), ticketingSystemInfo.getStationNum());
            success = boughtAndRefundTest(seatStatus);
            if (!success) return;
            success = inquiryTest(ticketingSystem, seatStatus);
            if (!success) return;
            System.out.println("----------The " + (i + 1) + "-th " + "Test End----------");
            System.out.println();
        }

        System.out.println("Congratulations! \nAll " + testNum + " Test Passed!");
    }

    private boolean simulateMultiThread(TicketingSystem ticketingSystem, int threadNum) throws InterruptedException {

        Thread[] threads = new Thread[threadNum];

        AtomicBoolean success = new AtomicBoolean(true);

        for (int i = 0; i< threadNum; i++) {
            threads[i] = new Thread(() -> {
                Random rand = new Random();
                Ticket ticket;
                ArrayList<Ticket> soldTicket = new ArrayList<>();

                for (int i1 = 0; i1 < operationNum; i1++) {
                    int sel = rand.nextInt(inquiryPercent);
                    if (0 <= sel && sel < refundPercent && soldTicket.size() > 0) { // return ticket
                        int select = rand.nextInt(soldTicket.size());
                        if ((ticket = soldTicket.remove(select)) != null) {
                            boolean refundSuccess = ticketingSystem.refundTicket(ticket);
                            if (!refundSuccess){
                                System.out.println("ErrOfRefund");
                                System.out.flush();
                                success.set(false);
                            }
                            refundMap.put(ticket.tid, ticket);
                        } else {
                            System.out.println("ErrOfRefund");
                            System.out.flush();
                            success.set(false);
                        }
                    } else if (refundPercent <= sel && sel < buyPercent) { // buy ticket
                        String passenger = passengerName();
                        int route = rand.nextInt(ticketingSystemInfo.getRouteNum()) + 1;
                        int departure = rand.nextInt(ticketingSystemInfo.getStationNum() - 1) + 1;
                        int arrival = departure + rand.nextInt(ticketingSystemInfo.getStationNum() - departure) + 1; // arrival is always greater than departure
                        if ((ticket = ticketingSystem.buyTicket(passenger, route, departure, arrival)) != null) {
                            soldTicket.add(ticket);
                            boughtMap.put(ticket.tid, ticket);
                        }
                    } else if (buyPercent <= sel && sel < inquiryPercent) { // inquiry ticket
                        int route = rand.nextInt(ticketingSystemInfo.getRouteNum()) + 1;
                        int departure = rand.nextInt(ticketingSystemInfo.getStationNum() - 1) + 1;
                        int arrival = departure + rand.nextInt(ticketingSystemInfo.getStationNum() - departure) + 1; // arrival is always greater than departure
                        int leftTicket = ticketingSystem.inquiry(route, departure, arrival);
                    }
                }

            });
            threads[i].start();
        }

        for (int i = 0; i< threadNum; i++) {
            threads[i].join();
        }
        return success.get();
    }

    private boolean boughtAndRefundTest(BitSet[][] seatStatus) {
        SortedSet<Long> keys = new TreeSet<>(boughtMap.keySet());
        for (Long key : keys) {
            Ticket ticket = boughtMap.get(key);
            if (!refundMap.containsKey(key)) {
                int route = ticket.route;
                int departure = ticket.departure;
                int arrival = ticket.arrival;
                int coach = ticket.coach;
                int seat = ticket.seat;
                int seatIndex = (coach - 1) * ticketingSystemInfo.getSeatNum() + seat;

                for (int stationIndex = departure; stationIndex < arrival; stationIndex++) {
                    boolean seatState = seatStatus[route][stationIndex].get(seatIndex);
                    if (seatState) {
                        System.out.println("ERROR Bought!////////////////");
                        printlnBoughtInfo(ticket);
                        System.out.println("ERROR Bought!////////////////");
                        return false;
                    }
                    seatStatus[route][stationIndex].set(seatIndex, true);
                }
            }
        }
        System.out.println("Ticket Bought Test Passed!");
        return true;
    }

    private BitSet[][] seatStatusGenerate(int routeNum, int stationNum) {
        BitSet[][] seatStatus = new BitSet[routeNum + 1][stationNum];
        for (int i = 1; i <= routeNum; i++) {
            for (int j = 1; j < stationNum; j++) {
                seatStatus[i][j] = new BitSet( totalSeatNum + 1);
            }
        }
        return seatStatus;
    }

    private boolean inquiryTest(TicketingSystem ticketingSystem, BitSet[][] seatStatus) {
        for (int route = 1; route <= ticketingSystemInfo.getRouteNum(); route++) {
            for (int departure = 1; departure < ticketingSystemInfo.getStationNum(); departure++) {
                for (int arrival = departure + 1; arrival <= ticketingSystemInfo.getStationNum(); arrival++) {
                    BitSet seatCopy = (BitSet) seatStatus[route][departure].clone();
                    for (int i = departure + 1; i < arrival; i++) {
                        seatCopy.or(seatStatus[route][i]);
                    }
                    int expectTicketNum = this.totalSeatNum - seatCopy.cardinality();
                    int actualTicketNum = ticketingSystem.inquiry(route, departure, arrival);
                    if (expectTicketNum != actualTicketNum) {
                        System.out.println("ERROR Inquiry!////////////////////////////////////////////////////////");
                        System.out.println("Route: " + route + " Departure: " + departure + " Arrival: " + arrival);
                        System.out.println("Expect: " + expectTicketNum + " Actual: " + actualTicketNum);
                        System.out.println("ERROR Inquiry!////////////////////////////////////////////////////////");
                        return false;
                    }
                }
            }
        }
        System.out.println("Ticket Test Passed!");
        return true;
    }

    private String passengerName() {
        Random rand = new Random();
        long uid = rand.nextInt(operationNum);
        return "passenger" + uid;
    }

    private void printTestInfo() {
        System.out.println("Ticketing System Test Start!");
        System.out.println("threadNum: " + threadNum + "\nrouteNum: " + ticketingSystemInfo.getRouteNum()
                + " stationNum: "+ ticketingSystemInfo.getStationNum() +
                " coachNum: " + ticketingSystemInfo.getCoachNum()
                + " seatNum: " + ticketingSystemInfo.getSeatNum() +
                "\nTestNum: " + testNum + " OperationNum: " + operationNum +
                "\nrefundPercent: " + refundPercent + " buyPercent: "
                + buyPercent + " inquiryPercent: " + inquiryPercent

        );
    }

    private void printlnBoughtInfo(Ticket ticket) {
        System.out.println("<Bought>" + " tid: " + ticket.tid + ", passenger: " + ticket.passenger + ", route: " + ticket.route + ", coach: " + ticket.coach + ", departure: " + ticket.departure + ", arrival: " + ticket.arrival + ", seat: " + ticket.seat);
        System.out.flush();
    }
}

