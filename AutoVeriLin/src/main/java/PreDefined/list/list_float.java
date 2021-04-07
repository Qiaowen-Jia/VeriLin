import java.util.ArrayList;


/****************************************************************
-- File Name: list_float.java
-- Author: Qiaowen Jia
-- mail: jiaqw@ios.ac.cn
-- Created Time: Wed Jan 13 01:21:50 2021
****************************************************************************/
public class list_float{
  ArrayList<Float> l;
  public list_float(){
	l = new ArrayList<Float>();
  }
  public boolean add(float value){
	return l.add(value);
  }
  public boolean remove(float value){
	return l.remove(value);
  }
  public boolean contains(float value){
	return l.contains(value);
  }
}
