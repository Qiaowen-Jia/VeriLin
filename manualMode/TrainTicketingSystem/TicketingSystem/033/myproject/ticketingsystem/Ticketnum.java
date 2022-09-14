package ticketingsystem;

import java.util.HashSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Ticketnum {
	HashSet<Integer> tickets;
	ReentrantReadWriteLock lock;
	Ticketnum(int num)
	{
		tickets = new HashSet<Integer>();
		for(int i=0;i<num;i++)
		{
			tickets.add(i+1);
		}
		lock = new ReentrantReadWriteLock();
	}
}
