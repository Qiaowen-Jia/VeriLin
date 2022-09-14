package ticketingsystem;
import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;



public class VeriLin{
  static int threadNum;
  static List<String> methodList = new ArrayList<String>();
  static List<String> reverseList = new ArrayList<String>();
  static List<String> reversibleList = new ArrayList<String>();
  
/**********Manually Modified ***********/
  static boolean isPosttime = true;
  static boolean isDynamic = true;
  static boolean detail = false;
  final static int routenum = 3;
  final static int coachnum = 3;
  final static int seatnum = 5;
  final static int stationnum = 5;
  static ArrayList<HistoryLine> history = new ArrayList<HistoryLine>();
  static TicketingDS object;
  public static class hl_Comparator_1 implements Comparator<HistoryLine>{
	@Override
	public int compare(HistoryLine hl1, HistoryLine hl2){
	  if(hl1.pretime - hl2.pretime > 0)
		return 1;
	  else if(hl1.pretime - hl2.pretime == 0)
		return 0;
	  else
		return -1;
	};
  }
  public static class hl_Comparator_2 implements Comparator<HistoryLine>{
	@Override
	public int compare(HistoryLine hl1, HistoryLine hl2){
	  if(hl1.posttime - hl2.posttime > 0)
		return 1;
	  else if(hl1.posttime - hl2.posttime == 0)
		return 0;
	  else
		return -1;
	};
  }
  public static class HistoryLine{
	long pretime;
	long posttime;
	int threadid;
	String operationName;
	long tid;
	String passenger;
	int route;
	int coach;
	int seat;
	int departure;
	int arrival;
	String res;

  }
  private static boolean parseline(ArrayList<HistoryLine> historyList, String line) {
	Scanner linescanner = new Scanner(line);
	if (line.equals("")) {
	  linescanner.close();
	  return true;
	}
	HistoryLine tl = new HistoryLine();
	tl.pretime = linescanner.nextLong();
	tl.posttime = linescanner.nextLong();
	tl.threadid = linescanner.nextInt();
	tl.operationName = linescanner.next();
	tl.tid = linescanner.nextLong();
	tl.passenger = linescanner.next();
	tl.route = linescanner.nextInt();
	tl.coach = linescanner.nextInt();
	tl.departure = linescanner.nextInt();
	tl.arrival = linescanner.nextInt();	
	tl.seat = linescanner.nextInt();
	tl.res = linescanner.next();
	historyList.add(tl);
	return true;
  }
  private static void initialization(){
	object = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadNum);
	methodList.add("refundTicket");
	reversibleList.add("refundTicket");
	reverseList.add("buyTicket");
	methodList.add("buyTicket");
	reversibleList.add("buyTicket");
	reverseList.add("refundTicket");
	methodList.add("inquiry");
	reversibleList.add("inquiry");
	reverseList.add("inquiry");
  }
  private static boolean execute(String methodName, HistoryLine line, int line_num){
	Ticket ticket = new Ticket();
	boolean flag = false;
	ticket.tid = line.tid;
	ticket.passenger = line.passenger;
	ticket.route = line.route;
	ticket.coach = line.coach;
	ticket.departure = line.departure;
	ticket.arrival = line.arrival;
	ticket.seat = line.seat;
	if(methodName.equals("buyTicket")){
	  if(line.res.equals("false")){
		int num = object.inquiry(ticket.route, ticket.departure, ticket.arrival);
		if(num == 0)
		  return true;
		else
		  return false;
	  }
	  flag = object.buyTicketReplay(ticket);
	  if((flag && line.res.equals("true")) || (!flag && line.res.equals("false")))
		return true;
	  else if(flag){
		object.refundTicketReplay(ticket);
	  }
	  return false;
	}
	else if(methodName.equals("refundTicket")){
	  flag = object.refundTicketReplay(ticket);
	  if((flag && line.res.equals("true")) || (!flag && line.res.equals("false")))
		return true;
	  else if(flag){
		object.buyTicketReplay(ticket);
	  }
	  return false;
	}
	else if(methodName.equals("inquiry")){
	  int num = object.inquiry(line.route, line.departure, line.arrival);
//	  System.out.println("old_num = " + line.seat + ", current_num = " + num);
	  if(num == line.seat)
		return true;
	}
	return false;
  }
/***********************VeriLin*************** */
  public static class Graph{
	int V;
	List<Integer> adjListArray[];
	public Graph(int V){
	  this.V = V;
	  @SuppressWarnings("unchecked")
	  List<Integer> adjListArray[] = new LinkedList[V];
	  this.adjListArray = adjListArray;
	  for(int i = 0; i < V; i++){
		adjListArray[i] = new LinkedList<>();
	  }
	}
	public void addEdge(int src, int desc){
	  this.adjListArray[src].add(desc);
	}
  }
  public static class Region implements Comparable{
	final static int SCALE = 1000000;
	long start;
	long end;
	List<HistoryLine> regionMember;
	int start_line;
	int end_line;
	public Region(){
	  this.regionMember = new ArrayList<>();
	}

	//according to pretime
	@Override
	public int compareTo(Object o){
	  Region rg = (Region)o;
	  if(this.end - rg.end > 0)
		return 1;
	  else if(this.end == rg.end)
		return 0;
	  else
		return -1;
	}

	public void init_region(int index, HistoryLine line){
	  this.start = line.pretime;
	  this.end = line.posttime;
	  this.regionMember.add(line);
	  this.start_line = index;
	  this.end_line = index;
	}

	public boolean contains(long pretime, long posttime){
	  if(pretime > end || posttime < start)
		return false;
	  else
		return true;
	}
	public boolean insert(int index, HistoryLine line){
	  if(!contains(line.pretime, line.posttime))
		return false;

	  regionMember.add(line);
	  
	  if(end_line < index)
		end_line = index;
	  else if(start_line > index)
		start_line = index;

	  if(line.pretime < start)
		start = line.pretime;
	  if(line.posttime > end)
		end = line.posttime;
	  return true;
	}

	public boolean check_hp(HistoryLine tl1, HistoryLine tl2){
	  if(tl1.posttime < tl2.pretime)
		return true;
	  else
		return false;
	}
	public List<List<Integer>> topo_sort(){
	  NewGraph new_graph = new NewGraph(regionMember.size());
	  for(int i = 0; i < regionMember.size(); i++)
		for(int j = 0; j < regionMember.size(); j++)
		  if(check_hp(regionMember.get(i), regionMember.get(j)))
			new_graph.addEdge(i, j);
	  List<List<Integer>> toporesult = new_graph.allTopologicalSorts();
	  return toporesult;
	}

	public boolean union(Region rg){
	  if(rg.start_line < start_line)
		start_line = rg.start_line;
	  else if(rg.end_line > end_line)
		end_line = rg.end_line;

	  if(rg.start < start){
		start = rg.start;
	  }
	  else if(rg.end > end){
		end = rg.end;
	  }
	  regionMember.addAll(rg.regionMember);
	  return true; 
	}

	public void print(){
	  if(regionMember.size() > 1){
		System.out.printf("start_line:%d, end_line:%d, member_num:%d\n\n", start_line, end_line, regionMember.size());
		for(int i = 0; i < regionMember.size(); i++)
		System.out.printf("%d %d %d %s\n", regionMember.get(i).pretime, regionMember.get(i).posttime,regionMember.get(i).threadid, regionMember.get(i).operationName);
	  }
	}
  }


  public static List<Integer> check_overlap(ArrayList<HistoryLine> historyList, List<Region> regionlist, int index){
	List<Integer> check_list = new ArrayList<>();
	for(int i = 0; i < regionlist.size(); i++)
	  if(regionlist.get(i).contains(historyList.get(index).pretime, historyList.get(index).posttime))
		check_list.add(i);
	return check_list;
  }

  public static void insert2(ArrayList<HistoryLine> historyList, List<Region> regionlist, int index){
	if((regionlist.size() == 0) || regionlist.get(regionlist.size() - 1).end < historyList.get(index).pretime){
	  Region region = new Region();
	  region.init_region(index, historyList.get(index));
	  regionlist.add(region);
	}
	else{
	  for(int i = regionlist.size() - 1; i >=0; i--){
		if(regionlist.get(i).end < historyList.get(index).pretime){
		  if(i == regionlist.size() - 2)
			break;
		  for(int j = i + 2; j < regionlist.size(); j++){
			regionlist.get(i+1).union(regionlist.get(j));
		  }
		  for(int j = regionlist.size() - 1; j >= i + 2 ; j--){
			regionlist.remove(j);
		  }
			
			break;
		  }
		  if(i == 0 && regionlist.get(0).end > historyList.get(index).pretime){
			for(int j = 1; j < regionlist.size(); j++){
			  regionlist.get(0).union(regionlist.get(j));
			}
			for(int j = regionlist.size() - 1; j >= 1 ; j--){
			  regionlist.remove(j);
			}			
		  }
		}
		regionlist.get(regionlist.size() - 1).insert(index, historyList.get(index));
	  }
  }
  private static void writeHistoryToFile(ArrayList<HistoryLine> historyList, String filename) {
	try {
	  System.setOut(new PrintStream(new FileOutputStream(filename)));
	  writeHistory(historyList);
	  System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
	}catch (FileNotFoundException e) {
	  System.out.println(e);
	}
  }
  private static void writeHistory(ArrayList<HistoryLine> historyList) {
	for (int i = 0; i < historyList.size(); i++) {
	  writeline(historyList, i);
	}
  }
  private static void writeline(ArrayList<HistoryLine> historyList, int line) {
	HistoryLine tl = historyList.get(line);
	System.out.println(tl.pretime + " " + tl.posttime + " " + tl.threadid + " " + tl.operationName + " " + tl.tid + " " + tl.passenger + " " + tl.route + " " + tl.coach  + " " + tl.departure + " " + tl.arrival + " " + tl.seat);
  }
  private static boolean readHistory(ArrayList<HistoryLine> historyList, String filename) {
	try {
	  Scanner scanner = new Scanner(new File(filename));
	  int i = 0;
	  while (scanner.hasNextLine()) {
		if(parseline(historyList, scanner.nextLine()) == false) {
		  scanner.close();
		  System.out.println("Error in parsing line " + i);
		  return false;
		}
		i++;
	  }
	  scanner.close();
	}catch (FileNotFoundException e) {
	  System.out.println(e);
	}
	return true;
  }

  private static boolean checkline(ArrayList<HistoryLine> historyList, int index){
	HistoryLine line = historyList.get(index);
	for(int i = 0; i < methodList.size();i++){
	  if(line.operationName.equals(methodList.get(i))){
		boolean flag = execute(methodList.get(i), line, index);
//		System.out.println("Line " + index + " executing " + methodList.get(i) + " res: " + flag + " tid = " + line.tid);
		return flag;
	  }
	}
	return false;
	
  }
  private static boolean backline(ArrayList<HistoryLine> historyList, int index){
	HistoryLine line = historyList.get(index);
	for(int i = 0; i < reversibleList.size();i++){
	  if(reversibleList.get(i).equals(line.operationName)){
		boolean flag = execute(reverseList.get(i), line, index);
		return flag;
	  }
	}
	return false;
  }

  public static int forwardcheck1(ArrayList<HistoryLine> historyList, int start_line, int end_line){
	for(int i = start_line; i <= end_line; i++)
	  if(checkline(historyList, i) == false)
		return i;
	return -1;
  }
  public static boolean region_forward_check(ArrayList<HistoryLine> historyList, int start_line, int end_line){
	int err_line = forwardcheck1(historyList, start_line, end_line);
	if(err_line == -1)
	  return true;
	backtrack(historyList, err_line, start_line);
	return false;	
  }

  public static void changeRegion(ArrayList<HistoryLine> historyList, Region region, List<Integer> tl){
	HistoryLine[] tmpline = new HistoryLine[region.end_line - region.start_line + 1];
	for(int i = 0; i < region.end_line - region.start_line + 1; i++){
	  tmpline[i] = region.regionMember.get(tl.get(i));
	}
	for(int i = region.start_line; i <= region.end_line; i++){
	  historyList.set(i, tmpline[i-region.start_line]);
	}
  }
  public static void restoreRegion(ArrayList<HistoryLine> historyList, Region region){
	for(int i = region.start_line; i <= region.end_line; i++){
	  historyList.set(i, region.regionMember.get(i-region.start_line));
	}
  }
  private static boolean backtrack(ArrayList<HistoryLine> historyList, int err_line, int start_line) {
	for(int i = err_line - 1; i >= start_line; i--) {
	  if (backline(historyList, i) == false) {
		System.out.println("Error in backtrack");
		return false;
	  }
	}
	return true;
  }
  public static boolean forward_search_region(ArrayList<HistoryLine> historyList, List<Region> regionlist, int index, List<Integer> topo_line){
	Region cur_region = regionlist.get(index);
	changeRegion(historyList, cur_region, topo_line);
	if(!region_forward_check(historyList, cur_region.start_line, cur_region.end_line)){
	  restoreRegion(historyList, cur_region);
	  return false;
	}
	return true;
  }
	

  public static boolean stack_forward_search(ArrayList<HistoryLine> historyList, List<Region> regionlist, int region_index, Graph graph, boolean[] visited, int[] indegree, ArrayList<Integer> stack){
	if(region_index == regionlist.size())
	  return true;

	Region cur_region = regionlist.get(region_index);
	boolean flag = false;
	for(int i = 0; i < graph.V; i++){
	  if(!visited[i] && indegree[i] == 0){
		visited[i] = true;
		stack.add((Integer)i);
//		System.out.println("stack add " + i);
		for(int adjacent: graph.adjListArray[i]){
		  indegree[adjacent]--;
		}
//		System.out.println("indegree index");
//		for(int j = 0; j < graph.V; j++){
//		  System.out.print(indegree[j] + " ");
//		}
//		System.out.println();

		if(stack_forward_search(historyList, regionlist, region_index, graph, visited, indegree, stack))
		  return true;
		
		visited[i] = false;
		stack.remove((Integer) i);
//		System.out.println("stack remove " + i);
		for(int adjacent: graph.adjListArray[i]){
		  indegree[adjacent]++;
		}
		flag = true;
		continue;
	  }
	}
	if(!flag && stack.size() == graph.V){// a new region
//	  System.out.println("region " + region_index + " stack: ");
//	  for(int i = 0; i < stack.size();i++){
//		System.out.println(stack.get(i));
//	  }
//	  System.out.println();
	  for(int i = 0; i < stack.size() ; i++){
		int current_line = cur_region.start_line + stack.get(i);
		if(checkline(historyList, current_line)){
		  continue;
		}
		else{
		  for(int j = i-1; j >=0; j--){
			int d = cur_region.start_line + stack.get(j);
			backline(historyList, d);
		  }
		  return false;
		}
	  }
	  if(region_index + 1 == regionlist.size())
		return true;
	  cur_region = regionlist.get(region_index + 1);
//	  System.out.println("Working on Region " + (region_index + 1) + " start_line = " + cur_region.start_line + " end_line = " + cur_region.end_line);
//	  for(int i = cur_region.start_line; i <= cur_region.end_line; i++){
//		writeline(historyList, i);
//	  }
	  int new_region_size = cur_region.regionMember.size();
	  Graph graph1 = new Graph(new_region_size);
	  boolean[] visited1 = new boolean[new_region_size];
	  int[] indegree1 = new int[new_region_size];
	  ArrayList<Integer> stack1 = new ArrayList<Integer>();
	  for(int i  = 0; i < new_region_size; i++){
		visited1[i] = false;
		indegree1[i] = 0;
	  }
	  for(int i = 0; i < new_region_size; i++){
		for(int j = 0; j < new_region_size; j++){
		  if(cur_region.check_hp(cur_region.regionMember.get(i), cur_region.regionMember.get(j))){
			graph1.addEdge(i, j);
		  }
		}
	  }
	  for(int i = 0; i < new_region_size; i++){
		for(int var: graph1.adjListArray[i]){
		  indegree1[var]++;
		}
	  }
	  boolean res = stack_forward_search(historyList, regionlist, region_index + 1, graph1, visited1, indegree1, stack1);
	  if(!res){
		for(int j = stack1.size() - 1; j >=0; j--){
		  int d = cur_region.start_line + stack1.get(j);
		  backline(historyList, d);
		}
		return false;
	  }
	  return true;
	}
//	System.out.println("Failed in region " + region_index);
	return false;
  }

  public static boolean preset_forward_search(ArrayList<HistoryLine> historyList, List<Region> regionlist, List<List<List<Integer>>> whole_topo_list){
	int region_size = regionlist.size();
	Region cur_region;
	List<List<Integer>> topo_list;
	boolean flag = false;
	int[] change_topo_list = new int[region_size];
	int max_line = 0;
	long starttime = System.currentTimeMillis();
	for(int i = 0; i < region_size; i++){
	  cur_region = regionlist.get(i);
	  int n = cur_region.end_line - cur_region.start_line + 1;
	  max_line = max_line>n?max_line:n;
	  topo_list = cur_region.topo_sort();// do topo
	  whole_topo_list.add(topo_list);
	  change_topo_list[i] = -1;
	}
	long midTime = System.currentTimeMillis();
	int longest_region = 0;
	for(int i = 0; i < regionlist.size(); i++){
	  int r = regionlist.get(i).end_line - regionlist.get(i).start_line + 1;
	  int w = whole_topo_list.get(i).get(0).size();
	  if(r != w){
		System.out.println("topo_size = " + w + " region_size = " + r);
	  }
	}
	for(int i = 0; i < region_size;){
	  if(i < 0)
		return false;
	  topo_list = whole_topo_list.get(i);
	  int topo_size = topo_list.size();
	  for(int j = change_topo_list[i] + 1; j < topo_size; j++){
		if(forward_search_region(historyList, regionlist, i, topo_list.get(j))){
		  flag = true;
		  longest_region = i;
		  change_topo_list[i] = j;
		  break;
		}
		else
		  flag = false;
	  }
	  if(!flag){
		i--;
		if(i < 0){
		  return false;
		}
		restoreRegion(historyList, regionlist.get(i));
	  }
	  else
		i++;
	}
	long endTime = System.currentTimeMillis();
	return true;


  }


  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws InterruptedException{
	if(args.length != 4){
	  System.out.println("The parameter list of VeriLin is threadNum, historyFile, isPosttime(0/1), failedTrace.");
	  return;
	}
	threadNum = Integer.parseInt(args[0]);
	String fileName = args[1];
	if(Integer.parseInt(args[2]) == 0){
	  isPosttime = false;
	}
	else if(Integer.parseInt(args[2]) == 1){
	  isPosttime = true;
	}
	else{
	  System.out.println("The parameter list of VeriLin is threadNum, historyFile, isPosttime(0/1), failedTrace.");
	  return;
	}
	String ft = args[3];
	long startMs, endMs;
	readHistory(history, fileName);
	initialization();
	startMs = System.currentTimeMillis();
	if(!isPosttime){
	  hl_Comparator_1 com1 = new hl_Comparator_1();
	  Collections.sort(history, com1);
	}
	else{
	  hl_Comparator_2 com2 = new hl_Comparator_2();
	  Collections.sort(history, com2);
	}
	  
	List<Region> regionList = new ArrayList<Region>();
	for(int i = 0; i < history.size(); i++){
	  insert2(history, regionList, i);
	}
	int max_region_size = 0;
	for(int i = 0; i < regionList.size();i++){
	  if(regionList.get(i).regionMember.size() > max_region_size)
		max_region_size = regionList.get(i).regionMember.size();
	}
	System.out.println("history size = " + history.size() + ", region size = " + regionList.size() + ", max_region_size = " + max_region_size);
	if(isDynamic){
	  Region cur_region = regionList.get(0);
	  int region_size = cur_region.regionMember.size();
	  Graph graph = new Graph(region_size);
	  boolean[] visited = new boolean[region_size];
	  int[] indegree = new int[region_size];
	  ArrayList<Integer> stack = new ArrayList<Integer>();
	  for(int i  = 0; i < region_size; i++){
		visited[i] = false;
		indegree[i] = 0;
	  }
	  for(int i = 0; i < region_size; i++){
		for(int j = 0; j < region_size; j++){
		  if(cur_region.check_hp(cur_region.regionMember.get(i), cur_region.regionMember.get(j))){
			graph.addEdge(i, j);
		  }
		}
	  }
	  for(int i = 0; i < region_size; i++){
		for(int var: graph.adjListArray[i]){
		  indegree[var]++;
		}
	  }
	  if(!stack_forward_search(history, regionList, 0, graph, visited, indegree, stack)){
		System.out.println("Verification Failed.");
		endMs = System.currentTimeMillis();
		writeHistoryToFile(history, ft);
	  }
	  else{
		System.out.println("Verification Finished.");
		endMs = System.currentTimeMillis();
	  }
	}
	else{
	  List<List<List<Integer>>> whole_topo_list = new ArrayList<List<List<Integer>>>();
	  if(!preset_forward_search(history, regionList, whole_topo_list)){
		System.out.println("Verification Failed.");
		endMs = System.currentTimeMillis();
		writeHistoryToFile(history, "failTrace");
	  }else{
		System.out.println("Verification Finished.");
		endMs = System.currentTimeMillis();
	  }
	}
    System.out.println(ms2DHMS(startMs,endMs));
		System.out.println("VeriLin");
  }
  private static String ms2DHMS(long startMs, long endMs) {
    String retval = null;
    long secondCount = (endMs - startMs) / 1000;
    String ms = (endMs - startMs) % 1000 + "ms";
    long days = secondCount / (60 * 60 * 24);
    long hours = (secondCount % (60 * 60 * 24)) / (60 * 60);
    long minutes = (secondCount % (60 * 60)) / 60;
    long seconds = secondCount % 60;

    if (days > 0) {
	  retval = days + "d" + hours + "h" + minutes + "m" + seconds + "s";
    } else if (hours > 0) {
      retval = hours + "h" + minutes + "m" + seconds + "s";
    } else if (minutes > 0) {
      retval = minutes + "m" + seconds + "s";
    } else if(seconds > 0) {
      retval = seconds + "s";
    }else {
      return ms;
    }
    return retval + ms;
  }
}
