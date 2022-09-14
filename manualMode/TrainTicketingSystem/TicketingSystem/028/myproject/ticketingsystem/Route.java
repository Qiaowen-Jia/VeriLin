package ticketingsystem;

import java.util.concurrent.atomic.AtomicReference;

public class Route {
    final static int[] stationCode = { 0b0, 0b1, 0b11, 0b111, 0b1111, 0b11111, 0b111111, 0b1111111, 0b11111111,
            0b111111111, 0b1111111111, 0b11111111111, 0b111111111111, 0b1111111111111, 0b11111111111111,
            0b111111111111111, 0b1111111111111111, 0b11111111111111111, 0b111111111111111111, 0b1111111111111111111,
            0b11111111111111111111, 0b111111111111111111111, 0b1111111111111111111111, 0b11111111111111111111111,
            0b111111111111111111111111, 0b1111111111111111111111111, 0b11111111111111111111111111,
            0b111111111111111111111111111, 0b1111111111111111111111111111, 0b11111111111111111111111111111,
            0b111111111111111111111111111111, 0b1111111111111111111111111111111, 0b11111111111111111111111111111111 };

    int seatPerCoach;
    int seatnum;
    int stationnum;

    Ticket[][] seatDepartureTickets;
    int[] seatRideCode;

    int switchId;
    RemainTickets[] switcher;
    AtomicReference<RemainTickets> remainTickets; 

    public Route(int routeId, int coachnum, int seatPerCoach, int stationnum) {
        this.seatPerCoach = seatPerCoach;
        seatnum = seatPerCoach * coachnum;
        this.stationnum = stationnum;
        seatRideCode = new int[seatnum];
        seatDepartureTickets = new Ticket[seatnum][stationnum - 1];
        for (int seat = 0; seat < seatnum; seat++) {
            seatRideCode[seat] = stationCode[stationnum - 1];
            for (int station = 0; station < stationnum - 1; station++) {
                seatDepartureTickets[seat][station] = new Ticket();
                seatDepartureTickets[seat][station].route = routeId + 1;
                seatDepartureTickets[seat][station].coach = seat / seatPerCoach + 1;
                seatDepartureTickets[seat][station].seat = seat % seatPerCoach + 1;
                seatDepartureTickets[seat][station].departure = station + 1;
            }
        }
        switchId = 0;
        switcher = new RemainTickets[2];
        switcher[0] = new RemainTickets(stationnum, seatnum);
        switcher[1] = new RemainTickets(stationnum, seatnum);
        remainTickets = new AtomicReference<>();
        remainTickets.set(switcher[switchId]);
    }

    public static int getRideCode(int departure, int arrival) {
        return stationCode[arrival] & ~stationCode[departure];
    }

    public int getRemainTicketsIndex(int departure, int arrival) {
        return departure * (stationnum - 1) + arrival - 1;
    }

    public Ticket buyTicket(long tid, String passenger, int departure, int arrival) {
        int hash = (passenger.hashCode() & 0x7fffffff) % seatnum;
        int[][] round = { { hash, seatnum }, { 0, hash } };
        int rideCode = getRideCode(departure, arrival);
        while (inquiry(departure, arrival) != 0) {
            for (int ri = 0; ri < 2; ri++) {
                for (int seat = round[ri][0]; seat < round[ri][1]; seat++) {
                    int seatCode = seatRideCode[seat];
                    if ((seatCode & rideCode) == rideCode) {
                        int startStation = departure, terminalStation = arrival;
                        while (((seatCode >>> (startStation - 1)) & 0b1) != 0) {
                            startStation = startStation - 1;
                        }
                        while (((seatCode >>> terminalStation) & 0b1) != 0) {
                            terminalStation = terminalStation + 1;
                        }
                        synchronized (this) {
                            if ((seatRideCode[seat] & rideCode) != rideCode)
                                continue;
                            if (seatCode != seatRideCode[seat]) {
                                seatCode = seatRideCode[seat];
                                startStation = departure;
                                terminalStation = arrival;
                                while (((seatCode >>> (startStation - 1)) & 0b1) != 0) {
                                    startStation = startStation - 1;
                                }
                                while (((seatCode >>> terminalStation) & 0b1) != 0) {
                                    terminalStation = terminalStation + 1;
                                }
                            }
                            seatRideCode[seat] = seatRideCode[seat] & ~rideCode;
                            switchId = 1 - switchId;
                            switcher[switchId].updateV2(departure, arrival, startStation, terminalStation, -1);
                            remainTickets.set(switcher[switchId]);
                            switcher[1 - switchId].updateV2(departure, arrival, startStation, terminalStation, -1);
                        }
                        seatDepartureTickets[seat][departure].tid = tid;
                        seatDepartureTickets[seat][departure].passenger = passenger;
                        seatDepartureTickets[seat][departure].arrival = arrival + 1;
                        return seatDepartureTickets[seat][departure];
                    }
                }
            }
        }
        return null;
    }

    public Boolean refundTicket(Ticket ticket) {
        int seat = (ticket.coach - 1) * seatPerCoach + ticket.seat - 1;
        int departure = ticket.departure - 1;
        int arrival = ticket.arrival - 1;
        // Check
        if (seat < 0 || seat >= seatnum || departure < 0 || departure >= stationnum - 1)
            return false;
        if (ticket != seatDepartureTickets[seat][departure])
            return false;
        int rideCode = getRideCode(departure, arrival);
        int seatCode = seatRideCode[seat];
        int startStation = departure, terminalStation = arrival;
        while (((seatCode >>> (startStation - 1)) & 0b1) != 0) {
            startStation = startStation - 1;
        }
        while (((seatCode >>> terminalStation) & 0b1) != 0) {
            terminalStation = terminalStation + 1;
        }
        synchronized (this) {
            if (seatCode != seatRideCode[seat]) {
                seatCode = seatRideCode[seat];
                startStation = departure;
                terminalStation = arrival;
                while (((seatCode >>> (startStation - 1)) & 0b1) != 0) {
                    startStation = startStation - 1;
                }
                while (((seatCode >>> terminalStation) & 0b1) != 0) {
                    terminalStation = terminalStation + 1;
                }
            }
            seatRideCode[seat] = seatRideCode[seat] | rideCode;
            switchId = 1 - switchId;
            switcher[switchId].updateV2(departure, arrival, startStation, terminalStation, 1);
            remainTickets.set(switcher[switchId]);
            switcher[1 - switchId].updateV2(departure, arrival, startStation, terminalStation, 1);
        }
        return true;
    }

    public int inquiry(int departure, int arrival) {
        int remain;
        do {
            remain = remainTickets.get().tryGetV2(departure, arrival);
        } while (remain < 0);
        return remain;
    }
}
