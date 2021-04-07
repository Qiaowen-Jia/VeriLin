/****************************************************************
-- File Name: queue_int.java
-- Author: Qiaowen Jia
-- mail: jiaqw@ios.ac.cn
-- Created Time: Wed Jan 13 00:48:43 2021
****************************************************************************/
package verify;
import java.util.*;
public class queue_int{
  LinkedList<Integer> q;
  public queue_int(){
	q = new LinkedList<Integer>();
  }
  public void enq(int value){
	q.addFirst(value);
  }
  public int deq(){
	return q.removeLast();
  }
  public boolean contains(int value){
	return q.contains(value);
  }
}
