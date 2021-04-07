import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
//for creating threadid
class ThreadId{
  private static final AtomicInteger nextId = new AtomicInteger(0);
  private static final ThreadLocal<Integer> threadId = new ThreadLocal<Integer>(){
  @Override protected Integer initialValue(){
	return nextId.getAndIncrement();
  }
  };
  public static int get(){
	return threadId.get();
  }
}


public class Trace{

final static int insert_pc = 30;
final static int delete_pc = 60;
final static int contains_pc = 110;
final static int total_pc = 110;

  public static void main(String[] args) throws InterruptedException{
	if(args.length != 2){
	  System.out.println("Error: The argument number is not two");
	  return;
	}
	final int threadnum = Integer.valueOf(args[0]);
	final int testnum = Integer.valueOf(args[1]);
	Thread[] threads = new Thread[threadnum];
final list object = new list();

	final long startTime = System.nanoTime();

	for(int i = 0; i < threadnum; i++){
	  threads[i] = new Thread(new Runnable(){
		public void run(){
		  Random rand = new Random();
		  for(int i = 0; i < testnum; i++){
	int sel = rand.nextInt(total_pc);
	if(0 <= sel && sel < insert_pc){
int p0 = rand.nextInt(4) + 1;
long preTime = System.nanoTime() - startTime;
boolean result = object.insert( p0);
long postTime = System.nanoTime() - startTime;
if(result)
	System.out.println(preTime + " " + postTime + " " + ThreadId.get() + " " + "insert" + " "+ p0 + " " + " True ");
else
	System.out.println(preTime + " " + postTime + " " + ThreadId.get() + " " + "insert" + " "+ p0 + " " + " False ");
}
if(insert_pc <= sel && sel <delete_pc){
int p0 = rand.nextInt(4) + 1;
long preTime = System.nanoTime() - startTime;
boolean result = object.delete( p0);
long postTime = System.nanoTime() - startTime;
if(result)
	System.out.println(preTime + " " + postTime + " " + ThreadId.get() + " " + "delete" + " "+ p0 + " " + " True ");
else
	System.out.println(preTime + " " + postTime + " " + ThreadId.get() + " " + "delete" + " "+ p0 + " " + " False ");
}
if(delete_pc <= sel && sel <contains_pc){
int p0 = rand.nextInt(5) + 1;
long preTime = System.nanoTime() - startTime;
boolean result = object.contains( p0);
long postTime = System.nanoTime() - startTime;
if(result)
	System.out.println(preTime + " " + postTime + " " + ThreadId.get() + " " + "contains" + " "+ p0 + " " + " True ");
else
	System.out.println(preTime + " " + postTime + " " + ThreadId.get() + " " + "contains" + " "+ p0 + " " + " False ");
}

		  }
		}
	  });
	  threads[i].start();
	}
	for(int i = 0; i < threadnum; i++){
	  threads[i].join();
	}
  }
}
