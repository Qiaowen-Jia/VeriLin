package ticketingsystem;

import java.util.*; 


public class NewGraph { 
	int V; // No. of vertices 
	List<Integer> adjListArray[]; 
	public NewGraph(int V) { 
		this.V = V; 
		@SuppressWarnings("unchecked") 
		List<Integer> adjListArray[] = new LinkedList[V]; 
		this.adjListArray = adjListArray; 
		for (int i = 0; i < V; i++) { 
			adjListArray[i] = new LinkedList<>(); 
		} 
	} 
	public void addEdge(int src, int dest) { 

		this.adjListArray[src].add(dest); 
	} 
	private void allTopologicalSortsUtil(boolean[] visited, 
						int[] indegree, ArrayList<Integer> stack, List<List<Integer>> rlist) { 
		boolean flag = false; 
		for (int i = 0; i < this.V; i++) { 
			if (!visited[i] && indegree[i] == 0) { 
				visited[i] = true; 
				stack.add(i); 
				for (int adjacent : this.adjListArray[i]) { 
					indegree[adjacent]--; 
				} 
				allTopologicalSortsUtil(visited, indegree, stack, rlist); 
				visited[i] = false; 
				stack.remove(stack.size() - 1); 
				for (int adjacent : this.adjListArray[i]) { 
					indegree[adjacent]++; 
				} 
				flag = true; 
			} 
		} 
		if (!flag) { 
			int count = 0;
			List<Integer> result = new ArrayList<>();	
			stack.forEach(i -> result.add(Integer.valueOf(i)));
			rlist.add(result);
		} 

	} 
	public List<List<Integer>> allTopologicalSorts() { 
		boolean[] visited = new boolean[this.V];
		List<List<Integer>>	result_list = new ArrayList<>();	
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


