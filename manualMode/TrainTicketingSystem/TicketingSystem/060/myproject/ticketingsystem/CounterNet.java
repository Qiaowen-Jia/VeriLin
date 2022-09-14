package ticketingsystem;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

class Counter
{
	private AtomicLong value;
	private long step;
	public Counter(long initVal, long step_len)
	{
		value = new AtomicLong();
		value.set(initVal);
		step = step_len;
	}
	public long getAndStep()
	{
		return value.getAndAdd(step);
	}
}
class Balancer {
	private AtomicInteger toggle;
	public Balancer[] next;
	public Counter[] counters;
	public Balancer()
	{
		toggle = new AtomicInteger();
		toggle.set(0);
		next = new Balancer[2];
		counters = new Counter[2];
		next[0] = null;
		next[1] = null;
		counters[0] = null;
		counters[1] = null;
	}
	public boolean isLeaf()
	{
		if(next[0] == null && next[1] == null)
			return true;
		else
			return false;
	}
	public int traverse()
	{
		int toggleValue = toggle.getAndIncrement();
		return toggleValue;
	}
}
public class CounterNet
{
	private Balancer[] inBalancerSet;
	private Counter[] counterSet;
	private int numOfInputBalancer;
	public CounterNet(int inputsWidth)
	{
		Double tmp = Math.ceil(Math.log(Double.valueOf(inputsWidth/2)) / Math.log(2.0));
		tmp = Math.pow(2.0,tmp);
		numOfInputBalancer = tmp.intValue();
		if(numOfInputBalancer == 0) numOfInputBalancer = 1;
		inBalancerSet = new Balancer[numOfInputBalancer];
		counterSet = new Counter[numOfInputBalancer*2];
		buildNet();
	}
	private Balancer[][] buildBitonic(int frontBlancer)
	{
		if(frontBlancer == 1)
		{
			Balancer[][] result = new Balancer[2][1];
			result[0][0] = new Balancer();
			result[1][0] = result[0][0];
			return result;
		}
		Balancer[][] merger = buildMerger(frontBlancer);
		Balancer[][] previousBitonic_0 = buildBitonic(frontBlancer/2);
		Balancer[][] previousBitonic_1 = buildBitonic(frontBlancer/2);
		Balancer[][] result = new Balancer[2][frontBlancer];
		for(int i = 0; i < frontBlancer; i ++)
		{
			result[1][i] = merger[1][i];
			if(i<frontBlancer/2)
			{
				result[0][i] = previousBitonic_0[0][i];
				previousBitonic_0[1][i].next[0] =  merger[0][i * 2];
				previousBitonic_0[1][i].next[1] =  merger[0][i * 2 + 1];
			}
			else
			{
				int idx = i - (frontBlancer / 2);
				result[0][i] = previousBitonic_1[0][idx];
				previousBitonic_1[1][idx].next[0] =  merger[0][frontBlancer - idx * 2 - 1];
				previousBitonic_1[1][idx].next[1] =  merger[0][frontBlancer - idx * 2 - 2];
			}
		}
		return result;
	}
	private Balancer[][] buildLayer(int frontBlancer)
	{
		Balancer[][] result;
		if(frontBlancer == 1)
		{
			result = new Balancer[2][1];
			result[1][0] = new Balancer();
			result[0][0] = result[1][0];
			return result;
		}
		result = new Balancer[2][frontBlancer];
		Balancer[][] prevLayer_0 = buildLayer(frontBlancer/2);
		Balancer[][] prevLayer_1 = buildLayer(frontBlancer/2);
		for(int i = 0;i<frontBlancer;i++)
		{
			result[0][i] = new Balancer();
			result[0][i].next[0] = prevLayer_0[0][i%(frontBlancer/2)];
			result[0][i].next[1] = prevLayer_1[0][i%(frontBlancer/2)];
			if(i<frontBlancer/2)
				result[1][i] = prevLayer_0[1][i];
			else
				result[1][i] = prevLayer_1[1][i-frontBlancer/2];
		}
		return result;
	}
	private Balancer[][] buildMerger(int frontBlancer)
	{

		Balancer[][] backLayer_0 = buildLayer(frontBlancer/2);
		Balancer[][] backLayer_1 = buildLayer(frontBlancer/2);
		Balancer[][] result = new Balancer[2][frontBlancer];
		for(int i = 0;i<frontBlancer;i++)
		{
			result[0][i] = new Balancer();
			result[0][i].next[0] = backLayer_0[0][i%(frontBlancer/2)];
			result[0][i].next[1] = backLayer_1[0][frontBlancer/2 - (i%(frontBlancer/2))-1];
			if(i<frontBlancer/2)
				result[1][i] = backLayer_0[1][i];
			else
				result[1][i] = backLayer_1[1][i-frontBlancer/2];
		}
		return result;
	}
	private void buildNet()
	{
		Balancer[][] net = buildBitonic(numOfInputBalancer);
		for(int i = 0;i<(numOfInputBalancer<<1);i++)
		{
			counterSet[i] = new Counter(i,numOfInputBalancer<<1);
		}
		for(int i = 0;i<numOfInputBalancer;i++)
		{
			net[1][i].counters[0] = counterSet[i << 1];
			net[1][i].counters[1] = counterSet[(i << 1) + 1];
			inBalancerSet[i] = net[0][i];
		}
	}
	public long getAndStep()
	{
		long id = Thread.currentThread().getId();
		int port = (int)(id % (numOfInputBalancer*2))/2;
		Balancer b = inBalancerSet[port];
		int toggleValue;
		while(!b.isLeaf())
		{
			toggleValue = b.traverse();
			if(toggleValue % 2 == 0)
				b = b.next[0];
			else
				b = b.next[1];
		}
		toggleValue = b.traverse();
		if(toggleValue % 2 == 0)
			return b.counters[0].getAndStep();
		else
			return b.counters[1].getAndStep();
	}
}