package ticketingsystem;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class Tuple{
	
	public int first;
	public AtomicInteger second;
	public int[] third;

    public Tuple(int a, AtomicInteger b, int[] c){
        first = a;
        second = b;
	third = c;
	Arrays.fill(third, 0);
    }

    public String toString(){
        return "(" + first + ", " + second + ")";
    }
}
