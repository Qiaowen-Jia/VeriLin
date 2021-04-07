package AutoGenerator;
/****************************************************************
-- File Name: RMethods.java
-- Author: Qiaowen Jia
-- mail: jiaqw@ios.ac.cn
-- Created Time: Mon Jan 25 14:59:43 2021
****************************************************************************/
import java.util.*;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.Type;

public class RMethods{
  public String objectName;
  public int methodNum;
  public boolean isJava;
  public String fileSuffix;
  public List<Method> testMethodList;
  public List<Integer> testMethodFreq;
  public boolean preDefined;
  public boolean hasSpec;
  public String specObject;
  public List<MethodDeclaration> specMethodList;
  public String dualObject;
  public List<MethodDeclaration> dualMethodList;

  public List<String> testTypeList;

  public RMethods(){
	methodNum = 0;
	isJava = true;
	fileSuffix = ".java";
  
	testMethodList = new ArrayList<Method>();
	testMethodFreq = new ArrayList<Integer>();
	testTypeList = new ArrayList<String>();
	preDefined = false;
	hasSpec = false;
  }

  public void setFileSuffix(String str){// not java
	isJava = false;
	fileSuffix = str;
  }

  public void setObjectName(String str){
	objectName = str;
  }

  public void setSpecFileName(String str){
	String[] split = str.split(".");
	if(split.length !=2){
	  System.out.println("Error in resolving SpecFileName.");
	  return;
	}
	specObject = str;
	specObject = split[0].trim();
  }
  
  public void setDualFileName(String str){
	String[] split = str.split(".");
	if(split.length !=2){
	  System.out.println("Error in resolving SpecFileName.");
	  return;
	}
	dualObject = str;
	specObject = split[0].trim();
  }

  public void setPreDefined(){
	preDefined = true;
  }

  public void addMethod(String name, Type t, NodeList<Parameter> p){
	Method m = new Method(methodNum, name, objectName);
	testMethodList.add(m);
	m.type = t;
	m.parameters = p;
	methodNum = methodNum + 1;
  }

  public void addFreq(int freq){
	testMethodFreq.add(freq);
  }

  public boolean checkMethodListFreq(){
	return testMethodList.size() == testMethodFreq.size();
  }

  public int getFreqByName(String name){
	for(int i = 0; i < methodNum; i ++){
	  if(testMethodList.get(i).methodName.equals(name)){
		return testMethodFreq.get(i);
	  }
	}
	return -1;
  }

  public Method getMethodByName(String name){
	for(int i = 0; i < methodNum; i ++){
	  if(testMethodList.get(i).methodName.equals(name))
		return testMethodList.get(i);
	}
	return null;
  }

  public boolean setMethodBoundByName(String name, MethodDeclaration md, String lower, String upper){
	for(int i = 0; i < methodNum; i++)
	  if(testMethodList.get(i).methodName.equals(name)){
		testMethodList.get(i).setBound(md.getParameter(i), lower, upper);
		return true;
	  }
	return false;		
  }
  public boolean checkValid(){
	return true;
  }

  public void printstate(){
	System.out.println("ObjectName: " + objectName);
	System.out.println("FileSuffix: " + fileSuffix);
	if(preDefined)
	  System.out.println("FileType: PreDefined");
	else
	  System.out.println("FileType: SelfDefined");
	System.out.println("Method Number: " + methodNum);
	if(hasSpec)
	  System.out.println("SpecFileName: " + specObject);
	else
	  System.out.println("");
	for(int i = 0; i < methodNum; i++){
	  System.out.println("MethodIndex: " + i + ", methodName: " + testMethodList.get(i).methodName + ", methodFreq:" + testMethodFreq.get(i));
	  Method m = testMethodList.get(i);
	  System.out.println("dualMethod: "+ m.dualName + ", specMethod: " + m.specName);
	  for(int j = 0; j < m.parameters.size();j ++)
		System.out.println("	parameters" + j + ": " + m.parameters.get(j).getTypeAsString() + " " + m.parameters.get(j).getNameAsString());
	  for(int j = 0; j < m.bounds.size(); j++)
		System.out.println("	Bound" + j + ": " + m.bounds.get(j).parameter.getNameAsString() + " " + m.bounds.get(j).getLower() + " " + m.bounds.get(j).getUpper());
	}
	for(int i = 0; i < testTypeList.size(); i++){
	  System.out.println("The selfDefinedType consist: " + testTypeList.get(i));
	}
  }
  public void addSelfDefinedType(String str){
	testTypeList.add(str);
	
  }
}
