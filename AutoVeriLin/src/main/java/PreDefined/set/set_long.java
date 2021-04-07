/****************************************************************
-- File Name: set_long.java
-- Author: Qiaowen Jia
-- mail: jiaqw@ios.ac.cn
-- Created Time: Wed Jan 13 01:41:33 2021
****************************************************************************/
import java.util.*;

public class set_long{
  Set<Long> s;
  public set_long(){
	s = new HashSet<Long>();
  }
  public boolean add(long value){
	return s.add(value);
  }
  public boolean remove(long value){
	return s.remove(value);
  }
  public boolean contains(long value){
	return s.contains(value);
  }
}

