/****************************************************************
-- File Name: queue_double.java
-- Author: Qiaowen Jia
-- mail: jiaqw@ios.ac.cn
-- Created Time: Wed Jan 13 01:04:12 2021
****************************************************************************/
import java.util.*;

public class queue_double{
  LinkedList<Double> q;
  public queue_double(){
	q = new LinkedList<Double>();
  }
  public void enq(double value){
	q.addFirst(value);
  }
  public double deq(){
	return q.removeLast();
  }
  public boolean contains(double value){
	return q.contains(value);
  }
}
