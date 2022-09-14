// package ticketingsystem;

// import java.util.concurrent.locks.*;

// // NOTE: this is for one Route Manager, or say, the car Manager

// public class RouteManager {
//     private Lock lock = new ReentrantLock();
//     private int coachnum;
//     private CoachManager [] coaches;

//     public RouteManager(int coachnum, int seatnum, int stationnum, int routeId) {
//         this.coachnum = coachnum;
//         coaches = new CoachManager[coachnum];
//         for (int i = 0; i < coachnum; i++ ) {
//             coaches[i] = new CoachManager(seatnum, stationnum);
//         }
//     }

//     public int inquiry(int departure, int arrival) {
//         lock.lock();

//         LogUtil.Log(String.format("  route inquiry begin departure:" + departure + " arrival:" + arrival));
//         int cnt = 0;
//         for (int i = 0; i < coachnum; i++ ) {
//             cnt += coaches[i].inquiry(departure, arrival);
//         }
//         LogUtil.Log(String.format("  route inquiry end:" + cnt));

//         lock.unlock();
//         return cnt;
//     }

//     public Ticket buyTicket(String passenger, int departure, int arrival) {
//         lock.lock();

//         LogUtil.Log(String.format("  route: buyTicket: passenger:%s departure:%d arrival:%d",
//                                               passenger, departure, arrival));
//         Ticket ticket;
//         for (int i = 0; i < coachnum; i++ ) {
//             if((ticket = coaches[i].buyTicket(passenger, departure, arrival)) != null) {
//                 lock.unlock();

//                 ticket.coach = i+1;
//                 LogUtil.Log(String.format("  coachnum " + i + " get the ticket"));
//                 return ticket;
//             }
//             LogUtil.Log(String.format("  coachnum " + i + " do not get the ticket"));
//         }

//         lock.unlock();
//         return null;
//     }

//     public boolean refundTicket(Ticket ticket) {
//         lock.lock();

//         boolean res = coaches[ticket.coach-1].refundTicket(ticket);
//         LogUtil.Log(String.format("  coach refund %s", res ? "successfully" : "failed"));

//         lock.unlock();
//         return res;
//     }
// }

// class CoachManager {
//     private int seatnum;
//     private SeatManager [] seats;

//     public CoachManager(int seatnum, int stationnum) {
//         this.seatnum = seatnum;
//         seats = new SeatManager[seatnum];
//         for (int i = 0; i < seatnum; i++ ) {
//             seats[i] = new SeatManager(stationnum);
//         }
//     }

//     public int inquiry(int departure, int arrival) {
//         int cnt = 0;
//         LogUtil.Log(String.format("    coach inquiry begin departure:%d arrival:%d", departure, arrival));
//         for (int i = 0; i < seatnum; i++ ) {
//             cnt += seats[i].inquiry(departure, arrival);
//         }
//         LogUtil.Log(String.format("    coach inquiry end: %d", cnt));
//         return cnt;
//     }

//     public Ticket buyTicket(String passenger, int departure, int arrival) {
//         Ticket ticket;
//         LogUtil.Log(String.format("  coach buyTicket begin: passenger:%s departure:%d arrival:%d", passenger, departure, arrival));
//         for (int i = 0; i < seatnum; i++ ) {
//             if ((ticket = seats[i].buyTicket(passenger, departure, arrival)) != null) {
//                 LogUtil.Log(String.format("    seat %d get the ticket", i));
//                 ticket.seat = i+1;
//                 return ticket;
//             }
//             LogUtil.Log(String.format("    seat %d do not get the ticket", i));
//         }
//         return null;
//     }

//     public boolean refundTicket(Ticket ticket) {
//         boolean res = seats[ticket.seat-1].refundTicket(ticket);

//         LogUtil.Log(String.format("   seat refund %s", res ? "successfully" : "failed"));
//         return res;
//     }
// }

// class SeatManager {
//     // lock is set by RouteManager, so SeatManager does not need
//     private int stationnum;
//     private TicketNode head;

//     public SeatManager(int stationnum) {
//         this.stationnum = stationnum;
//         head = new TicketNode();
//     }

//     public int inquiry(int departure, int arrival) {
//         TicketNode curr = head.next;
//         while (curr != null) {
//             if (arrival <= curr.getDeparture()) return 1;
//             if (curr.intersect(departure, arrival)) return 0;
//             curr = curr.next;
//         }
//         return 1;
//     }

//     public Ticket buyTicket(String passenger, int departure, int arrival) {
//         TicketNode pred = head;
//         TicketNode curr = pred.next;
//         while (curr != null) {
//             LogUtil.Log(String.format("      node: departure:%d arrival:%d", curr.getDeparture(), curr.getArrival()));
//             if (arrival <= curr.getDeparture()) {
//                 LogUtil.Log(String.format("      should insert before the node"));
//                 TicketNode node = new TicketNode(passenger, departure, arrival);
//                 pred.insert(node);
//                 return node.getTicket();
//             }
//             if (curr.intersect(departure, arrival)) {
//                 LogUtil.Log(String.format("      intersect"));
//                 return null;
//             }
//             pred = curr;
//             curr = pred.next;
//         }
//         LogUtil.Log(String.format("      should insert at the tail"));
//         TicketNode node = new TicketNode(passenger, departure, arrival);
//         pred.insert(node);
//         return node.getTicket();
//     }

//     public boolean refundTicket(Ticket ticket) {
//         TicketNode pred = head;
//         TicketNode curr = pred.next;
//         while (curr != null) {
//             if (curr.isSame(ticket)) {
//                 return pred.deleteNext();
//             }
//             if (ticket.arrival <= curr.getArrival())
//                 return false; // NOTE: not found
//             pred = curr;
//             curr = pred.next;
//         }
//         return false; // NOTE: not found
//     }
// }

// class TicketNode {
//     private PartTicket ticket;
//     public TicketNode next;

//     public TicketNode() {
//         this.ticket = null;
//         this.next = null;
//     }

//     public TicketNode(String passenger, int departure, int arrival) {
//         ticket = new PartTicket();
//         ticket.tid       = Counter.get();
//         ticket.passenger = passenger;
//         ticket.departure = departure;
//         ticket.arrival   = arrival;
//         next = null;
//     }

//     public boolean intersect(int departure, int arrival) {
//         return ((Math.max(departure, ticket.departure) - Math.min(arrival, ticket.arrival)) < 0);
//     }

//     public boolean isSame(Ticket ticket) {
//         return (this.ticket.tid       == ticket.tid       &&
//                 this.ticket.passenger == ticket.passenger &&
//                 this.ticket.departure == ticket.departure &&
//                 this.ticket.arrival   == ticket.arrival);
//     }

//     public boolean insert(TicketNode node) {
//         TicketNode n = this.next;
//         this.next = node;
//         node.next = n;
//         return true;
//     }

//     public boolean deleteNext() {
//         if (next == null) {
//             System.out.println("Error, try to delete null node");
//             return false;
//         }
//         next = next.next;
//         return true;
//     }

//     public int getDeparture() {
//         return ticket.departure;
//     }

//     public int getArrival() {
//         return ticket.arrival;
//     }

//     public Ticket getTicket() {
//         Ticket ticket = new Ticket();
//         ticket.tid = this.ticket.tid;
//         ticket.passenger = this.ticket.passenger;
//         ticket.departure = this.ticket.departure;
//         ticket.arrival = this.ticket.arrival;
//         ticket.seat = 0;
//         ticket.coach = 0;
//         ticket.route = 0;
//         return ticket;
//     }
// }

// class PartTicket {
//     public long tid;
//     public String passenger;
//     public int departure;
//     public int arrival;
// }