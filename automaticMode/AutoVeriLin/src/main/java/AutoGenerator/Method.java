package AutoGenerator;
/****************************************************************
-- File Name: Method.java
-- Author: Qiaowen Jia
-- mail: jiaqw@ios.ac.cn
-- Created Time: Mon Jan 25 12:44:40 2021
****************************************************************************/
import java.util.*;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

//no excepetion considered 
//no javaparser parsing here
public class Method{
  public int index;
  public String objectName;
  public String methodName;
//  public MethodDeclaration methodInfo;
  public Type type;
  public NodeList<Parameter> parameters;
  public String reversibleName;
  public MethodDeclaration reversible;

  public boolean isSpec;
  public String specName;
  public MethodDeclaration spec;

  public int freq;

  public boolean isPreDefined;
  public String preDefinedName;
  public String selfDefinedName;

  public boolean hasBound;
  public List<Bound> bounds;

  


  public Method(int i, String methodname, String objectname){
	index = i;
	methodName = methodname;
	objectName = objectname;
	
	isSpec = false;
	isPreDefined = false;
	specName = methodname;
	preDefinedName = "";
	selfDefinedName = "";

	hasBound = false;
	bounds = new ArrayList<Bound>();
  }

  public void setReversible(String str){
	reversibleName = str;
  }
  public void setSpec(String str){
	isSpec = true;
	specName = str;
  }
  public void setPreDefined(String str){
	isPreDefined = true;
	preDefinedName = str;
  }
  public void setSelfDefined(String str){
	isPreDefined = false;
	selfDefinedName = str;
  }
  public void setReversibleMethod(MethodDeclaration m){
	reversible = m;
  }
  public void setSpecMethod(MethodDeclaration m){
	spec = m;
  }
  public boolean checkPreDefined(){
	return isPreDefined;
  }
  public void setMethodInfo(MethodDeclaration md){
	type = md.getType();
	parameters = md.getParameters();
  }
  public void setBound(Parameter p, String lower, String upper){
	Bound b = new Bound(p, lower, upper);
	bounds.add(b);
	hasBound = true;
  }
}
