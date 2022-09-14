/****************************************************************
-- File Name: queue_long.java
-- Author: Qiaowen Jia
-- mail: jiaqw@ios.ac.cn
-- Created Time: Wed Jan 13 01:08:20 2021
****************************************************************************/
import java.util.*;

public class queue_long{
  LinkedList<Long> q;
  public void enq(long value){
	q.addFirst(value);
  }
  public long deq(){
	return q.removeLast();
  }
  public boolean contains(long value){
	return q.contains(value);
  }
}
