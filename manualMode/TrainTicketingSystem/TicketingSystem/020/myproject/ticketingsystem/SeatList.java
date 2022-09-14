package ticketingsystem;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class SeatList {
    Node head, tail;

    SeatList() {
        tail = head = new Node(-1);
    }

    void add(long key, SeatWithInterval seat) {
        Node node = new Node(key, seat);
        while (true) {
        }
    }

    static class Node {
        long key;
        SeatWithInterval seat;
        AtomicMarkableReference<SeatWithInterval> next;

        Node(long key) {
            this(key, null);
        }

        Node(long key, SeatWithInterval seat) {
            this.key = key;
            this.seat = seat;
            next = new AtomicMarkableReference<>(null, false);
        }
    }

    static class Window {
        Node pred, curr;

        Window(Node pred, Node curr) {
            this.pred = pred;
            this.curr = curr;
        }
    }
}
