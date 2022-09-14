package ticketingsystem;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
public class TicketingDS implements TicketingSystem {
		
	//ToDo
    int routeNum;
    int coachNum; // every route
    int seatNum;  // every coach
    int stationNum;


    Route[] routes;
    AtomicLong tidGenerator;


    ConcurrentHashMap<Long, Ticket> soldTickets;

    public TicketingDS(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
        this.routeNum = routeNum;
        this.coachNum = coachNum;
        this.seatNum = seatNum;
        this.stationNum = stationNum;

        routes = new Route[routeNum + 1];
        tidGenerator = new AtomicLong(0);

        for(int i = 1; i <= routeNum; i++) {
            routes[i] = new Route(coachNum, seatNum, stationNum);
        }

        soldTickets = new ConcurrentHashMap<>(5 * routeNum * coachNum * seatNum);

    }
    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        // 查询如果没有余票，就直接返回
        if(inquiry(route, departure, arrival) == 0) {
            return null;
        }
        int passengerHash = Math.abs((passenger + System.nanoTime()).hashCode()) % 32767;
        Route chosenRoute = routes[route];
        for(int k = 0; k < coachNum; k++) {
            int coach = (passengerHash + k) % coachNum + 1; // [1, coachNum]
//            int coach = random.nextInt(coachNum) % coachNum + 1; // [1, coachNum]
            Coach chosenCoach = chosenRoute.coaches[coach];
            for(int i = 0; i < seatNum; i++) {
                int seat = (passengerHash + i) % seatNum + 1;
                Seat chosenSeat = chosenCoach.seats[seat];
                if(chosenSeat.sold[departure][arrival] == true) {
                    continue;
                }
                chosenSeat.seatLock.lock();
                try {
                    if(chosenSeat.sold[departure][arrival] == true) {
                        continue;
                    }
                    Ticket ticket = new Ticket();
                    ticket.tid = tidGenerator.getAndIncrement();
                    ticket.passenger = passenger;
                    ticket.route = route;
                    ticket.coach = coach;
                    ticket.seat = seat;
                    ticket.departure = departure;
                    ticket.arrival = arrival;
                    soldTickets.put(ticket.tid, ticket);
                    chosenSeat.sold[departure][arrival] = true;

                    for(int d = 1; d <= stationNum - 1; d++) {
                        for(int a = d + 1; a <= stationNum; a++) {
                            if(!((arrival <= d) || (a <= departure))) {
                                chosenCoach.seats[seat].sold[d][a] = true;
                                if(chosenCoach.seats[seat].influence[d][a] == 0) {
                                    chosenRoute.remainTicketNum[d][a].getAndDecrement();
                                }
                                chosenCoach.seats[seat].influence[d][a]++;
                            }
                        }
                    }
                    return ticket;

                } finally {
                    chosenSeat.seatLock.unlock();
                }
            }
        }

        return null;




    }
    @Override
    public int inquiry(int route, int departure, int arrival) {
        return routes[route].getRemainTicketNum(departure, arrival);
    }

    @Override
    public boolean refundTicket(Ticket ticket) {

        Route chosenRoute = routes[ticket.route];
        Coach chosenCoach = chosenRoute.coaches[ticket.coach];
        Seat chosenSeat = routes[ticket.route].coaches[ticket.coach].seats[ticket.seat];
        chosenSeat.seatLock.lock();
        try {
            if(!validateTicket(ticket)) {
                return false;
            }
            soldTickets.remove(ticket.tid);
            chosenSeat.sold[ticket.departure][ticket.arrival] = false;

            for(int d = ticket.departure; d <= ticket.arrival - 1; d++) {
                for (int a = d + 1; a <= ticket.arrival; a++) {


                    chosenCoach.seats[ticket.seat].influence[d][a]--;
                    if (chosenCoach.seats[ticket.seat].influence[d][a] == 0) {
                        chosenRoute.remainTicketNum[d][a].getAndIncrement();
                        chosenCoach.seats[ticket.seat].sold[d][a] = false;
                    }
                }
            }


            for(int d = ticket.departure - 1; d >= 1; d--) {
                for(int a = ticket.departure + 1; a <= ticket.arrival; a++) {

                    chosenCoach.seats[ticket.seat].influence[d][a]--;
                    if(chosenCoach.seats[ticket.seat].influence[d][a] == 0) {
                        chosenRoute.remainTicketNum[d][a].getAndIncrement();
                        if(chosenCoach.seats[ticket.seat].sold[d][ticket.departure] == false) {
                            chosenCoach.seats[ticket.seat].sold[d][a] = false;
                        }
                    }

                }

                for(int a = ticket.arrival + 1; a <= stationNum; a++) {

                    chosenCoach.seats[ticket.seat].influence[d][a]--;
                    if(chosenCoach.seats[ticket.seat].influence[d][a] == 0) {
                        chosenRoute.remainTicketNum[d][a].getAndIncrement();
                        if((chosenCoach.seats[ticket.seat].sold[d][ticket.departure] == false) &&
                                (chosenCoach.seats[ticket.seat].sold[a][ticket.arrival] == false)){
                            chosenCoach.seats[ticket.seat].sold[d][a] = false;
                        }
                    }



                }
            }

            for(int a = stationNum; a >= ticket.arrival + 1; a--) {
                for(int d = ticket.arrival - 1; d >= ticket.departure; d--) {

                    chosenCoach.seats[ticket.seat].influence[d][a]--;
                    if(chosenCoach.seats[ticket.seat].influence[d][a] == 0) {
                        chosenRoute.remainTicketNum[d][a].getAndIncrement();
                        if(chosenCoach.seats[ticket.seat].sold[ticket.arrival][a] == false) {
                            chosenCoach.seats[ticket.seat].sold[d][a] = false;
                        }
                    }
                }

//                for(int d = ticket.departure - 1; d >= 1; d--) {
//                    if(chosenCoach.seats[ticket.seat].sold[d][ticket.departure] == false){
//                        chosenCoach.seats[ticket.seat].sold[d][a] = false;
//                    }
//                }
            }






        } finally {
            chosenSeat.seatLock.unlock();

        }

        return true;


    }



    private boolean validateTicket(Ticket ticket) {
        if(routes[ticket.route].coaches[ticket.coach].seats[ticket.seat].sold[ticket.departure][ticket.arrival] == false) {
            return false;
        }

        Ticket soldTicket = soldTickets.get(ticket.tid);

        if (soldTicket == null) {
           return false;
        }

        return  ticket.tid == soldTicket.tid &&
                ticket.passenger.equals(soldTicket.passenger) &&
                ticket.route == soldTicket.route &&
                ticket.coach == soldTicket.coach &&
                ticket.seat == soldTicket.seat &&
                ticket.departure == soldTicket.departure &&
                ticket.arrival == soldTicket.arrival;
    }
}
