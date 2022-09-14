/****************************************************************
-- File Name: set_double.java
-- Author: Qiaowen Jia
-- mail: jiaqw@ios.ac.cn
-- Created Time: Wed Jan 13 01:37:50 2021
****************************************************************************/
import java.util.*;

public class set_double{
  Set<Double> s;
  public set_double(){
	s = new HashSet<Double>();
  }
  public boolean add(double value){
	return s.add(value);
  }
  public boolean remove(double value){
	return s.remove(value);
  }
  public boolean contains(double value){
	return s.contains(value);
  }
}
