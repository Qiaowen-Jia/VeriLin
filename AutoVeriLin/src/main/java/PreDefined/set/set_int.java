/****************************************************************
-- File Name: set_int.java
-- Author: Qiaowen Jia
-- mail: jiaqw@ios.ac.cn
-- Created Time: Wed Jan 13 01:34:04 2021
****************************************************************************/
import java.util.*;

public class set_int{
  Set<Integer> s;
  public set_int(){
	s = new HashSet<Integer>();
  }
  public boolean add(int value){
	return s.add((Integer)value);
  }
  public boolean remove(int value){
	return s.remove((Integer)value);
  }
  public boolean contains(int value){
	return s.contains(value);
  }
}
