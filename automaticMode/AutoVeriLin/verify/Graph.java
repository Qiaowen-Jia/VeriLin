/****************************************************************
-- File Name: Graph.java
-- Author: Qiaowen Jia
-- mail: jiaqw@ios.ac.cn
-- Created Time: Fri 09 Oct 2020 09:39:53 AM CST
****************************************************************************/


//Java program to print all topolgical sorts of a graph 
import java.util.*; 


public class Graph { 
	int V; // No. of vertices 

	List<Integer> adjListArray[]; 

	public Graph(int V) { 

		this.V = V; 

		@SuppressWarnings("unchecked") 
		List<Integer> adjListArray[] = new LinkedList[V]; 

		this.adjListArray = adjListArray; 

		for (int i = 0; i < V; i++) { 
			adjListArray[i] = new LinkedList<>(); 
		} 
	} 
	// Utility function to add edge 
	public void addEdge(int src, int dest) { 

		this.adjListArray[src].add(dest); 
	} 
	

	// Main recursive function to print all possible 
	// topological sorts
	//
	private void allTopologicalSortsUtil(boolean[] visited, 
						int[] indegree, ArrayList<Integer> stack, List<List<Integer>> rlist) { 
		// To indicate whether all topological are found 
		// or not 
		boolean flag = false; 

		for (int i = 0; i < this.V; i++) { 
			// If indegree is 0 and not yet visited then 
			// only choose that vertex 
			if (!visited[i] && indegree[i] == 0) { 
				
				// including in result 
				visited[i] = true; 
				stack.add(i); 
				for (int adjacent : this.adjListArray[i]) { 
					indegree[adjacent]--; 
				} 
				allTopologicalSortsUtil(visited, indegree, stack, rlist); 
				
				// resetting visited, res and indegree for 
				// backtracking 
				visited[i] = false; 
				stack.remove(stack.size() - 1); 
				for (int adjacent : this.adjListArray[i]) { 
					indegree[adjacent]++; 
				} 

				flag = true; 
			} 
		} 
		// We reach here if all vertices are visited. 
		// So we print the solution here 
		if (!flag) { 
			//stack.forEach(i -> System.out.print(i + " "));//origin 
			//System.out.println();//origin 
			List<Integer> result = new ArrayList<>();	
//			stack.forEach(i -> System.out.printf(i + " "));
//			System.out.println();
//			stack.forEach((i) -> {result[count] = i; count = count + 1;});			
//			stack.forEach(i -> result.add(Integer.valueOf(i)));
			for(int i = 0; i < stack.size(); i++){
			 result.add(Integer.valueOf(stack.get(i)));
			}
//			System.out.println("result num: " + result.size()  + "rlist num: " + rlist.size());
			rlist.add(result);
//			rlist.add(Arrays.stream(result).boxed().toArray( Integer[]::new ));
		} 

	} 
	
	// The function does all Topological Sort. 
	// It uses recursive alltopologicalSortUtil() 
	public List<List<Integer>> allTopologicalSorts() { 
		// Mark all the vertices as not visited 
		boolean[] visited = new boolean[this.V];
		List<List<Integer>>	result_list = new ArrayList<List<Integer>>();
		List<Integer> posttime_order = new ArrayList<Integer>();
		for(int i = 0; i < this.V; i++){
		  posttime_order.add(i);
		}
//		result_list.add(posttime_order);
		int[] indegree = new int[this.V]; 

		for (int i = 0; i < this.V; i++) { 

			for (int var : this.adjListArray[i]) { 
				indegree[var]++; 
			} 
		} 

		ArrayList<Integer> stack = new ArrayList<>(); 

		allTopologicalSortsUtil(visited, indegree, stack, result_list); 
		return result_list;
	} 
	
} 


