/****************************************************************
-- File Name: list_double.java
-- Author: Qiaowen Jia
-- mail: jiaqw@ios.ac.cn
-- Created Time: Wed Jan 13 01:15:42 2021
****************************************************************************/
import java.util.*;

public class list_double{
  List<Double> l;
  public list_double(){
	l = new ArrayList<Double>();
  }
  public boolean add(double value){
	return l.add(value);
  }
  public boolean remove(double value){
	return l.remove(value);
  }
  public boolean contains(double value){
	return l.contains(value);
  }
}
