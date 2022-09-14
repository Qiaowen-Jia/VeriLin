/****************************************************************
-- File Name: stack_float.java
-- Author: Qiaowen Jia
-- mail: jiaqw@ios.ac.cn
-- Created Time: Tue Jan 12 14:22:32 2021
****************************************************************************/
import java.util.*;

public class stack_float{
  Stack<Float> s;
  public stack_float(){
	s = new Stack<Float>();
  }
  public float pop(){
	return s.pop();
  }
  public void push(float value){
	s.push(value);
  }
  public boolean contains(float value){
	return s.contains(value);
  }
}
