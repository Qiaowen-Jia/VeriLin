/****************************************************************
-- File Name: list_int.java
-- Author: Qiaowen Jia
-- mail: jiaqw@ios.ac.cn
-- Created Time: Tue Jan 12 15:26:35 2021
****************************************************************************/
import java.util.*;

public class list{
  List<Integer> l;
  public list(){
	l = new ArrayList<Integer>();
  }
  public boolean insert(int item){
	return l.add(item);
  }
  public boolean delete(int item){
	return l.remove((Integer)item);
  }
  public boolean contains(int item){
	return l.contains(item);
  }
}
