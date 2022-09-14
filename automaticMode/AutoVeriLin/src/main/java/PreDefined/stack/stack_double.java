/****************************************************************
-- File Name: stack_double.java
-- Author: Qiaowen Jia
-- mail: jiaqw@ios.ac.cn
-- Created Time: Tue Jan 12 14:19:27 2021
****************************************************************************/
import java.util.*;
public class stack_double{
  Stack<Double> s;
  public stack_double(){
	s = new Stack<Double>();
  }
  public void push(double value){
	s.push(value);
  }
  public double pop(){
	return s.pop();
  }
  public boolean contains(double value){
	return s.contains(value);
  }
}
