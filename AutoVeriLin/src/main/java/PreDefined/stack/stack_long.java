/****************************************************************
-- File Name: stack_long.java
-- Author: Qiaowen Jia
-- mail: jiaqw@ios.ac.cn
-- Created Time: Tue Jan 12 14:15:24 2021
****************************************************************************/
import java.util.*;
public class stack_long{
  Stack<Long> s;
  public stack_long(){
	s = new Stack<Long>();
  }
  public boolean contains(long value){
	return s.contains(value);
  }
  public long pop(){
	return s.pop();
  }
  public void push(long value){
	s.push(value);
  }
}
