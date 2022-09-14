/****************************************************************
-- File Name: set_float.java
-- Author: Qiaowen Jia
-- mail: jiaqw@ios.ac.cn
-- Created Time: Wed Jan 13 01:39:40 2021
****************************************************************************/
import java.util.*;

public class set_float{
  Set<Float> s;
  public set_float(){
	s = new HashSet<Float>();
  }
  public boolean add(float value){
	return s.add(value);
  }
  public boolean remove(float value){
	return s.remove(value);
  }
  public boolean contains(float value){
	return s.contains(value);
  }
}
