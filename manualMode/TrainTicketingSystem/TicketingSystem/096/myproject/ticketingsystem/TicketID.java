package ticketingsystem;

import java.util.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TicketID { // Tmp ID generator, need change next
    public static long get() {
        return (ThreadId.get() << (Long.SIZE-8)) |
               ((long )(System.nanoTime()) & ((1<<(Long.SIZE-8)) - 1));
    }
}