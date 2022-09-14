package AutoGenerator;
import com.github.javaparser.ast.body.Parameter;

/****************************************************************
-- File Name: Bound.java
-- Author: Qiaowen Jia
-- mail: jiaqw@ios.ac.cn
-- Created Time: Mon Jan 25 12:22:57 2021
****************************************************************************/

public class Bound{
  public Parameter parameter;
  public String lower;
  public String upper;

  public Bound(Parameter p, String l, String u){
	parameter = p;
	lower = l;
	upper = u;
  }
  public Object getLower(){
	String p = parameter.getType().toString();
	if(p.equals("int") || p.equals("Integer")){
	  return Integer.valueOf(lower);

	}
	else if(p.equals("float") || p.equals("Float")){
	  return Float.valueOf(lower);
	}
	else if(p.equals("double") || p.equals("Double")){
	  return Double.valueOf(lower);
	}
	else if(p.equals("long") || p.equals("Long")){
	  return Long.valueOf(lower);
	}
	else if(p.equals("boolean") || p.equals("Boolean")){
	  return Boolean.valueOf((lower));
	}
	return null;
  }
  public Object getUpper(){
	String p = parameter.getType().toString();
	if(p.equals("int") || p.equals("Integer")){
	  return Integer.valueOf(upper);
	}
	else if(p.equals("float") || p.equals("Float")){
	  return Float.valueOf(upper);
	}
	else if(p.equals("double") || p.equals("Double")){
	  return Double.valueOf(upper);
	}
	else if(p.equals("long") || p.equals("Long")){
	  return Long.valueOf(upper);
	}else if(p.equals("boolean") || p.equals("Boolean")){
	  return Boolean.valueOf(upper);
	}
	return null;
  }
}
