package ticketingsystem;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.*;

class Route{
    Coach[] coachtable;
    Route(int coachnum, int seatnum, int stationnum){
        coachtable = new Coach[coachnum];
        for(int i=0;i<coachnum;i++){
            coachtable[i] = new Coach(seatnum, stationnum);
        }
    }
}

class Coach{
    AtomicInteger[] seattable;
    Coach(int seatnum, int stationnum){
        seattable = new AtomicInteger[seatnum];
        for(int i=0;i<seatnum;i++){
            seattable[i] = new AtomicInteger(0);
        }
    }
}

class LockFreeList<Ticket>{
    class Node {
        public Ticket ticket;
        public int key;
        public AtomicMarkableReference<Node> next;
        public Node(Ticket item){
            this.ticket=item;
            this.key=item.hashCode();
        }
        public Node(int integer){
            this.ticket=null;
            this.key=integer;
        }
    }
    private Node head,tail;
    public LockFreeList() {
        head = new Node(Integer.MIN_VALUE);
        tail = new Node(Integer.MAX_VALUE);
        head.next = new AtomicMarkableReference(tail, false);
        tail.next = new AtomicMarkableReference(null, false);
    }

    class Window {
        public Node pred, curr;
        Window(Node myPred, Node myCurr) {
            pred = myPred;
            curr = myCurr;
        }
    }

    public Window find(Node head, int key) {
        Node pred = null, curr = null, succ = null;
        boolean[] marked = {false};
        boolean snip;
        retry:
        while (true) {
            pred = head;
            curr = pred.next.getReference();
            while (true) {
                succ = curr.next.get(marked);
                while (marked[0]) {
                    snip = pred.next.compareAndSet(curr, succ, false, false);
                    if (!snip) continue retry;
                    curr = succ;
                    succ = curr.next.get(marked);
                }
                if (curr.key >= key)
                    return new Window(pred, curr);
                pred = curr;
                curr = succ;
            }
        }
    }

    public boolean add(Ticket item) {
        int key = item.hashCode();
        while (true) {
            Window window = find(head, key);
            Node pred = window.pred, curr = window.curr;
            if (curr.key == key) {
                return false;
            } else {
                Node node = new Node(item);
                node.next = new AtomicMarkableReference(curr, false);
                if (pred.next.compareAndSet(curr, node, false, false)) {
                    return true;
                }
            }
        }
    }

    public boolean remove(Ticket item) {
        int key = item.hashCode();
        boolean snip;
        while (true) {
            Window window = find(head, key);
            Node pred = window.pred, curr = window.curr;
            if (curr.key != key) {
                return false;
            } else {
                Node succ = curr.next.getReference();
                snip = curr.next.attemptMark(succ, true);
                if (!snip)
                    continue;
                pred.next.compareAndSet(curr, succ, false, false);
                return true;
            }
        }
    }

    public boolean contains(Ticket item) {
        boolean[] marked = {false};
        int key = item.hashCode();
        Node curr = head;
        while (curr.key < key) {
            curr = curr.next.getReference();
            Node succ = curr.next.get(marked);
        }
        return (curr.key == key && !marked[0]);
    }
}



public class TicketingDS implements TicketingSystem {
    Route[] routetable;
    static int routenum;
    static int coachnum;
    static int seatnum;
    static int stationnum;
    static int threadnum;
    AtomicInteger ticketID;
    LockFreeList[] ticketlist;
    TicketingDS (int routenum ,int coachnum ,int seatnum ,int stationnum ,int threadnum ) {
        this.routenum = routenum;
        this.coachnum = coachnum;
        this.seatnum = seatnum;
        this.stationnum = stationnum;
        this.threadnum = threadnum;
        ticketID = new AtomicInteger(0);
        ticketlist = new LockFreeList[routenum];
        routetable = new Route[routenum];
        for(int i=0;i<routenum;i++){
            routetable[i] = new Route(coachnum,seatnum,stationnum);
            ticketlist[i] = new LockFreeList();
        }
    }

    public Ticket buyTicket(String passenger, int route, int departure, int arrival){
        int stationstatus;
        boolean flag;
        if(route>=1&&route<=routenum&&departure>=1&&departure<arrival&&arrival<=stationnum){
            int neededstationstatus = station2Int(departure,arrival);
            for(int coachiter=0;coachiter<coachnum;coachiter++){
                for(int seatiter=0;seatiter<seatnum;seatiter++){
                    flag=true;
                    while(flag==true){
                        stationstatus = routetable[route-1].coachtable[coachiter].seattable[seatiter].get();
                        if((neededstationstatus&stationstatus)==0) {
                            if (routetable[route - 1].coachtable[coachiter].seattable[seatiter].compareAndSet(stationstatus, stationstatus ^ neededstationstatus)) {
                                Ticket ticket = new Ticket();
                                ticket.tid = ticketID.getAndIncrement();
                                ticket.passenger = passenger;
                                ticket.route = route;
                                ticket.coach = coachiter + 1;
                                ticket.seat = seatiter + 1;
                                ticket.departure = departure;
                                ticket.arrival = arrival;
                                ticketlist[route-1].add(ticket);
                                return ticket;
                            }
                        }
                        else {
                            flag=false;
                        }
                    }
                }
            }
            return null;
        }
        else{
            return null;
        }
    }

    public int inquiry(int route, int departure, int arrival) {
        if(route>=1&&route<=routenum&&departure>=1&&departure<arrival&&arrival<=stationnum){
            int counter=0;
            int neededstationstatus = station2Int(departure,arrival);
            for(int coachiter=0;coachiter<coachnum;coachiter++){
                for(int seatiter=0;seatiter<seatnum;seatiter++){
                    int stationstatus = routetable[route-1].coachtable[coachiter].seattable[seatiter].get();
                    if((neededstationstatus&stationstatus)==0){
                        counter++;
                    }
                }
            }
            return counter;
        }
        else {
            return 0;
        }
    }

    public boolean refundTicket(Ticket ticket){
        int stationstatus;
        int neededstationstatus = station2Int(ticket.departure,ticket.arrival);
        if (ticketlist[ticket.route-1].remove(ticket)){
            while(true){
                stationstatus = routetable[ticket.route-1].coachtable[ticket.coach-1].seattable[ticket.seat-1].get();
                if((stationstatus&neededstationstatus)==neededstationstatus){
                    if(routetable[ticket.route-1].coachtable[ticket.coach-1].seattable[ticket.seat-1].compareAndSet(stationstatus,stationstatus^neededstationstatus)) {
                        return true;
                    }
                }
                else{
                    return false;
                }
            }
        }
        else{
            return false;
        }
    }
    
    int station2Int(int depature, int arrival){
        int status=0;
        for(int i=depature;i<arrival;i++){
            status+=Math.pow(2,i);
        }
        return status;
    }
}


