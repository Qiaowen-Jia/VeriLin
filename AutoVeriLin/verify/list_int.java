/****************************************************************
-- File Name: list_int.java
-- Author: Qiaowen Jia
-- mail: jiaqw@ios.ac.cn
-- Created Time: Tue Jan 12 15:26:35 2021
****************************************************************************/
import java.util.*;

public class list_int{
  List<Integer> l;
  public list_int(){
	l = new ArrayList<Integer>();
  }
  public boolean add(int item){
	return l.add(item);
  }
  public boolean remove(int item){
	return l.remove((Integer)item);
  }
  public boolean contains(int item){
	return l.contains(item);
  }
}
