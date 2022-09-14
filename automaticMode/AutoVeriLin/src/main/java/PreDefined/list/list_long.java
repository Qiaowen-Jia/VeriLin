/****************************************************************
-- File Name: list_long.java
-- Author: Qiaowen Jia
-- mail: jiaqw@ios.ac.cn
-- Created Time: Wed Jan 13 01:29:59 2021
****************************************************************************/
import java.util.*;

public class list_long{
  List<Long> l;
  public list_long(){
	l = new ArrayList<Long>();
  }
  public boolean add(long value){
	return l.add(value);
  }
  public boolean remove(long value){
	return l.remove(value);
  }
  public boolean contains(long value){
	return l.contains(value);
  }
}
