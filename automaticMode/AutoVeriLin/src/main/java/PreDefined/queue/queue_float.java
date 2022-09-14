/****************************************************************
-- File Name: queue_float.java
-- Author: Qiaowen Jia
-- mail: jiaqw@ios.ac.cn
-- Created Time: Wed Jan 13 01:06:18 2021
****************************************************************************/
import java.util.*;
public class queue_float{
  LinkedList<Float> q;
  public queue_float(){
	q = new LinkedList<Float>();
  }

  public void enq(float value){
	q.addFirst(value);
  }
  public float deq(){
	return q.removeLast();
  }
  public boolean contains(float value){
	return q.contains(value);
  }
}
