import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.FileDescriptor;
import java.util.*;
import java.util.Collection;

public class VeriLin{
  static ArrayList<TraceLine> trace = new ArrayList<TraceLine>();
  public static class TraceLine implements Comparable{
	long pretime;
	long posttime;
	int threadid;
	String actionName;
		String p_0_res;
		int p_0_0;
		String p_1_res;
		int p_1_0;
		String p_2_res;
		int p_2_0;

	@Override
	public int compareTo(Object o){
	  TraceLine tl1 = this;
	  TraceLine tl2 = (TraceLine) o;
	  if(tl1.posttime - tl2.posttime > 0 )
		return 1;
	  else if(tl1.posttime - tl2.posttime == 0)
		return 0;
	  else
		return -1;
	};
  }

  public static class Region{
	final static int SCALE = 100000;
	long start;
	long end;
	List<TraceLine> regionMember;
	int start_line;
	int end_line;

	public Region(){
	  this.regionMember = new ArrayList<>();
	}

	public void init_region(int index, TraceLine line){
	  this.start = line.pretime;
	  this.end = line.posttime;
	  this.regionMember.add(line);
	  this.start_line = index;
	  this.end_line = index;
	}

	public boolean insert(int index, TraceLine line){
	  if(line.pretime > this.end || line.posttime < this.start)
		return false;
	  regionMember.add(line);
	  this.end_line = end_line >= index?end_line:index;
	  this.start_line = start_line <=index?start_line:index;
	  this.start = line.pretime >= start?start:line.pretime;
	  this.end = line.posttime >= end?line.posttime:end;
	  return true;
	}

	public List<List<Integer>> topo_sort(){
	  Graph graph = new Graph(regionMember.size());
	  for(int i = 0; i < regionMember.size(); i++)
		for(int j = 0; j < regionMember.size(); j++)
		  if(regionMember.get(i).posttime < regionMember.get(j).pretime)
			graph.addEdge(i, j);
	  List<List<Integer>> toporesult = graph.allTopologicalSorts();
	  return toporesult;
	}

	public boolean union(Region rg){
	  if(rg.start > end || rg.start < start)
		return false;
	  regionMember.addAll(rg.regionMember);
	  return true;
	}
  }

	static list_int object = new list_int();
public static boolean parseline(String line){
	Scanner linescanner = new Scanner(line);
	if(line.equals("")){
		linescanner.close();
		return true;
	}
	TraceLine tl = new TraceLine();
	tl.pretime = linescanner.nextLong();
	tl.posttime = linescanner.nextLong();
	tl.threadid = linescanner.nextInt();
	tl.actionName = linescanner.next();
	if(tl.actionName.equals("insert")){
		tl.p_0_0 = linescanner.nextInt();
		tl.p_0_res = linescanner.next();
		trace.add(tl);
	}
	if(tl.actionName.equals("delete")){
		tl.p_1_0 = linescanner.nextInt();
		tl.p_1_res = linescanner.next();
		trace.add(tl);
	}
	if(tl.actionName.equals("contains")){
		tl.p_2_0 = linescanner.nextInt();
		tl.p_2_res = linescanner.next();
		trace.add(tl);
	}
	linescanner.close();
	return true;
}
public static boolean backline(int line){
	TraceLine tl = trace.get(line);
	if(tl.actionName.equals("insert")){
		boolean res = object.remove(tl.p_0_0);
		return res;
}
	if(tl.actionName.equals("delete")){
		boolean res = object.add(tl.p_1_0);
		return res;
}
	if(tl.actionName.equals("contains")){
		boolean res = object.contains(tl.p_2_0);
		return res;
}
	return true;
}
public static boolean checkline(int line){
	TraceLine tl = trace.get(line);
if(tl.actionName.equals("insert")){
	boolean res = object.add(tl.p_0_0);
	if((res && tl.p_0_res.equals("False")) ||(!res && tl.p_0_res.equals("True")))
		return false;
}
if(tl.actionName.equals("delete")){
	boolean res = object.remove(tl.p_1_0);
	if((res && tl.p_1_res.equals("False")) ||(!res && tl.p_1_res.equals("True")))
		return false;
}
if(tl.actionName.equals("contains")){
	boolean res = object.contains(tl.p_2_0);
	if((res && tl.p_2_res.equals("False")) ||(!res && tl.p_2_res.equals("True")))
		return false;
}
	return true;
}
public static boolean writeline(int line){
	TraceLine tl = trace.get(line);
if(tl.actionName.equals("insert")){
	System.out.println(tl.pretime + " " + tl.posttime + " " + tl.threadid + " " + tl.actionName + " " + tl.p_0_0 + " " + tl.p_0_res);
}
if(tl.actionName.equals("delete")){
	System.out.println(tl.pretime + " " + tl.posttime + " " + tl.threadid + " " + tl.actionName + " " + tl.p_1_0 + " " + tl.p_1_res);
}
if(tl.actionName.equals("contains")){
	System.out.println(tl.pretime + " " + tl.posttime + " " + tl.threadid + " " + tl.actionName + " " + tl.p_2_0 + " " + tl.p_2_res);
}
	return true;
}

  public static void insert(List<Region> regionlist, int index){
	if((regionlist.size() == 0) || regionlist.get(regionlist.size() - 1).end < trace.get(index).pretime){
	  Region region = new Region();
	  region.init_region(index, trace.get(index));
	  regionlist.add(region);
	}
	else
	  regionlist.get(regionlist.size() - 1).insert(index, trace.get(index));
  }

  public static boolean readTraceFile(String filename){
	try{
	  Scanner scanner = new Scanner(new File(filename));
	  while(scanner.hasNextLine()){
		if(!parseline(scanner.nextLine())){
		  scanner.close();
		  return false;
		}
	  }
	  scanner.close();
	}catch(FileNotFoundException e){
	  System.out.println(e);
	}
	return true;
  }

  public static void writeTraceFile(String filename){
	try{
	  System.setOut(new PrintStream(new FileOutputStream(filename)));
	  System.out.println("Error in Verification. Result_Info: ");
//	  writeinfo();
	  for(int i = 0; i < trace.size(); i++)
		writeline(i);
	  System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
	}catch(FileNotFoundException e){
	  System.out.println(e);
	}
  }

  public static String ms2DHMS(long startMs, long endMs){
	String retval = null;
	long secondCount = (endMs - startMs) / 1000;
	String ms = (endMs - startMs) % 1000 + "ms";
	long days = secondCount / (60 * 60 *24);
	long hours = (secondCount % (60*60*24)) / (60 * 60);
	long minutes = (secondCount % (60 * 60)) / 60;
	long seconds = secondCount % 60;
	if(days > 0)
	  retval = days + "d" + hours + "h" + minutes + "m" + seconds + "s";
	else if(hours > 0)
	  retval = hours + "h" + minutes + "m" + seconds + "s";
	else if(minutes > 0)
	  retval = minutes + "m" + seconds + "s";
	else if(seconds > 0)
	  retval = seconds + "s";
	else
	  return ms;
	return retval + ms;
  }

  public static void changeRegion(Region region, List<Integer> tl){
	TraceLine[] tmpline = new TraceLine[region.end_line - region.start_line + 1];
	for(int i = 0; i < region.end_line - region.start_line + 1; i++)
	  tmpline[i] = region.regionMember.get(tl.get(i));
	for(int i = region.start_line; i <= region.end_line; i++)
	  trace.set(i, tmpline[i - region.start_line]);
  }

  public static void restoreRegion(Region region){
	for(int i = region.start_line; i <= region.end_line; i++)
	  trace.set(i, region.regionMember.get(i-region.start_line));
  }

  public static int forward_check(int start_line, int end_line){
	for(int i = start_line; i <= end_line; i++)
	  if(checkline(i) == false)
		return i;
	return -1;
  }

  public static boolean backtrack(int err_line, int start_line){
	for(int i = err_line - 1; i >= start_line; i--){
	  if(backline(i) == false){
		System.out.println("Error in backTrack.");
		return false;
	  }
	}
	return true;
  }

  public static boolean region_forward_check(int start_line, int end_line){
	int err_line = forward_check(start_line, end_line);
	if(err_line == -1)
	  return true;
	backtrack(err_line, start_line);
	return false;
  }

  public static boolean forward_search(List<Region> regionlist, int index){
	if(index == regionlist.size())
	  return true;
	Region cur_region = regionlist.get(index);
	List<List<Integer>> topo_list = cur_region.topo_sort();
	List<Integer> topo_line;
	for(int i = 0; i < topo_list.size(); i++){
	  topo_line = topo_list.get(i);
	  changeRegion(cur_region, topo_line);
	  if(!region_forward_check(cur_region.start_line, cur_region.end_line)){
		restoreRegion(cur_region);
		continue;
	  }
	  boolean ret = forward_search(regionlist, index+1);
	  if(ret == true)
		return true;
	  restoreRegion(cur_region);
	}
	return false;
  }

  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws InterruptedException{
	//The arguments given is traceFileName, writeFileName, threadnum
	if(args.length != 3){
	  System.out.println("The argument of VeriRegion is incorrect.");
	  System.out.println("VeriRegion should be given as java VeriRegion *readTraceFile*, *writeTraceFile*, *threadnum*");
	  return;
	}
	String readfilename = args[0];
	String writefilename = args[1];
//	int threadnum = Integer.valueOf(args[2]);
//	List<List<Integer>> topo_list;
	List<Region> regionlist = new ArrayList<Region>();
	long startMs = System.currentTimeMillis();
	readTraceFile(readfilename);
	Collections.sort(trace);
	for(int i = 0; i < trace.size(); i++){
//	  TraceLine tl = trace.get(i);
	  insert(regionlist, i);
	}
	if(forward_search(regionlist, 0))
	  System.out.println("Verification Finished.");
	else{
	  System.out.println("Verification Failed.");
	  writeTraceFile(writefilename);
	}
	long endMs = System.currentTimeMillis();
	System.out.println(ms2DHMS(startMs, endMs));
  }
}
