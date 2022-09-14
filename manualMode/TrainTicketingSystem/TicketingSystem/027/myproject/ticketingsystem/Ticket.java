package ticketingsystem;

import java.util.Objects;

/**
 * 车票类
 * @author 965087276@qq.com
 * @date 2019/12/5 10:19
 */
public class Ticket {
    /**
     * 车票编号
     */
    public int tid;
    /**
     * 乘客名称
     */
    public String passenger;
    /**
     * 车次号
     */
    public int route;
    /**
     * 车厢号
     */
    public int coach;
    /**
     * 座位号
     */
    public int seat;
    /**
     * 上车站
     */
    public int departure;
    /**
     * 下车站
     */
    public int arrival;

    public Ticket() {

    }

    public Ticket(int tid, String passenger, int route, int coach, int seat, int departure, int arrival) {
        this.tid = tid;
        this.passenger = passenger;
        this.route = route;
        this.coach = coach;
        this.seat = seat;
        this.departure = departure;
        this.arrival = arrival;
    }

    public int getTid() {
        return tid;
    }

    public void setTid(int tid) {
        this.tid = tid;
    }

    public String getPassenger() {
        return passenger;
    }

    public void setPassenger(String passenger) {
        this.passenger = passenger;
    }

    public int getRoute() {
        return route;
    }

    public void setRoute(int route) {
        this.route = route;
    }

    public int getCoach() {
        return coach;
    }

    public void setCoach(int coach) {
        this.coach = coach;
    }

    public int getSeat() {
        return seat;
    }

    public void setSeat(int seat) {
        this.seat = seat;
    }

    public int getDeparture() {
        return departure;
    }

    public void setDeparture(int departure) {
        this.departure = departure;
    }

    public int getArrival() {
        return arrival;
    }

    public void setArrival(int arrival) {
        this.arrival = arrival;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ticket)) return false;
        Ticket ticket = (Ticket) o;
        return getTid() == ticket.getTid() &&
                getRoute() == ticket.getRoute() &&
                getCoach() == ticket.getCoach() &&
                getSeat() == ticket.getSeat() &&
                getDeparture() == ticket.getDeparture() &&
                getArrival() == ticket.getArrival() &&
                getPassenger().equals(ticket.getPassenger());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTid(), getPassenger(), getRoute(), getCoach(), getSeat(), getDeparture(), getArrival());
    }
}

