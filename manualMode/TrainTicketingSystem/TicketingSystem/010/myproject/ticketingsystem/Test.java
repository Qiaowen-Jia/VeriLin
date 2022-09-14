package ticketingsystem;

/**
 * @author Jianyong Feng
 **/
public class Test {
    public static void main(String[] args) throws InterruptedException {

        // Thread Number Parameter
        int threadNum = 96;

        // Ticketing System Parameters
        int routeNum = 3;
        int stationNum = 3;
        int coachNum = 3;
        int seatNum = 3;

        // Test Parameters
        int operationNum = 50; // operations per thread executes
        int refundPercent = 5; // return ticket operation percent
        int buyPercent = 15; // buy ticket operation percent
        int inquiryPercent = 80; //inquiry ticket operation percent
        int testNum = 5000; // iterations of test

        TicketingSystemInfo ticketingSystemInfo = new TicketingSystemInfo(routeNum, stationNum, coachNum, seatNum);
        TicketingSystemCorrectnessTest ticketingSystemCorrectnessTest =
                new TicketingSystemCorrectnessTest(
                        ticketingSystemInfo, threadNum, operationNum,
                        refundPercent, buyPercent, inquiryPercent, testNum
                );

        ticketingSystemCorrectnessTest.CorrectnessTest();
    }
}
