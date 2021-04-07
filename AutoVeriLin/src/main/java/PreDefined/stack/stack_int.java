/****************************************************************
-- File Name: stack_int.java
-- Author: Qiaowen Jia
-- mail: jiaqw@ios.ac.cn
-- Created Time: Tue Jan 12 13:10:34 2021
****************************************************************************/
import java.util.*;
public class stack_int{
  Stack<Integer> s;
  public stack_int(){
	s = new Stack<Integer>();
  }
  public int pop(){
	return s.pop();
  
  }
  public void push(int value){
	s.push(value);
  }
  public boolean contains(int value){
	return s.contains(value);
  }
}
