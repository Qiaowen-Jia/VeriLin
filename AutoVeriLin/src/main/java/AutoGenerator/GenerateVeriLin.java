package AutoGenerator;
/****************************************************************
-- File Name: GenerateVeriLin.java
-- Author: Qiaowen Jia
-- mail: jiaqw@ios.ac.cn
-- Created Time: Tue Jan 26 06:39:29 2021
****************************************************************************/
//noted that the dual relation is for the specobject
import java.util.*;
import java.io.FileNotFoundException;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.Type;



import java.io.*;
public class GenerateVeriLin{
  public static List<MethodDeclaration> methodList;
  public static boolean isSpec;
  public static boolean copyFile(String name){
	try{
	  File f = new File(name);
	  Scanner scanner = new Scanner(f);
	  while(scanner.hasNextLine()){
		System.out.println(scanner.nextLine());
	  }
	  scanner.close();
	  return true;
	}catch(FileNotFoundException e){
	  System.out.println(e);
	  return false;
	}
  }
  public static boolean isPrimitive(String str){
	if(str.equals("boolean") || str.equals("int") || str.equals("double") || str.equals("float") || str.equals("long") || str.equals("short") || str.equals("byte") || str.equals("char")){
	  return true;
	}
	return false;
  }
  /* public static boolean checkDual(RMethods rMethods, String name, String dualName){
	Method m1 = rMethods.getMethodByName(name);
	Method m2 = rMethods.getMethodByName(dualName);
	if(!m1.type.equals(m2.type) || m1.parameters.size() != m2.parameters.size()){
	  System.out.println("Error: The type or Parameter size is not corresponded. name: " + name + " dualName: " + dualName);
	  return false;
	}
	String cond1 = "if(tl.actionName.equals(\"" + name + "\")){";
	String retTrue = "	return true;";
	String retFalse = "	return false;";
	String cond2;
	if(m1.type.isVoidType()){
	  cond2 = "if(object.;" + dualName + "(";
	  for(int i = 0; i < m1.parameters.size(); i++){
		if(i != 0)
		  cond2 = cond2 + ", ";
		cond2 = cond2 + "p_" + name + "_" + i;
	  }
	  cond2 = cond2 + ")) ";
	  System.out.println(cond1);
	  System.out.println(cond2);
	  System.out.println(retTrue);
	  System.out.println("}");
	}
	else{
	  String str = m1.type.toString() + " p_" + dualName + "_res = object." + dualName + "(";
	  cond2 = "if(" + name + "_res == ";
	  for(int i = 0; i < m1.parameters.size(); i++){
		if(i != 0)
		  cond2 = cond2 + "";
	  }
	  
	}
	return true;
  }*/
  public static boolean checkSpec(RMethods rMethods1, String name1, RMethods rMethods2, String name2){
	Method m1 = rMethods1.getMethodByName(name1);
	Method m2 = rMethods2.getMethodByName(name2);
	return true;	
  }
  public GenerateVeriLin(){
	isSpec = false;
  }
  public static boolean generateObject(RMethods rMethods){
	isSpec = rMethods.hasSpec;
	if(isSpec){
	  String specName = rMethods.specObject;
	  System.out.println("	static " + specName + " object = new " + specName + "();");	
	}
	else{
	  String objectName = rMethods.objectName;
	  System.out.println("	static " + objectName + " object = new " + objectName + "();");
	}
	return true;
  }



/*	String objectName = "";
	if(isSpec){
	  String[] specsplit = rMethods.specFileName.split(".");
	  if(specsplit[0].equals("")){
		return false;
	  }
	  objectName = specsplit[0].trim();
	}
	else{
	  objectName = rMethods.objectName;
	}
	Method m = rMethods.getMethodByName(rMethods.objectName);
	if(m.parameters.isEmpty())
	  System.out.println(objectName + " object = new " + objectName + "();");
	else{
	  if(!m.bounds.isEmpty()){
		System.out.println("Error: The construction function do not has exact parameters declared.");
		return false;
	  }
	  //TODO
	  String str = objectName + " object = new " + objectName + "(";
	  for(int i = 0; i < m.parameters.size(); i++){
		for(int j = 0; j < m.bounds.size(); j++){
		if(m.bounds.get(i).parameter.getNameAsString().equals(rMethods.objectName)){

		}
		if(i == m.bounds.size() - 1){
		  System.out.println("Error: The construction function do not has exact parameters declared.");
		  return false;
		}
	  }

	}
	return true;
  }*/
  public static boolean generateTraceStructure(RMethods rMethods){
//	System.out.println("testMethodNum = " +  rMethods.testMethodList.size());
	for(int i = 0; i < rMethods.testMethodList.size(); i++){
	  Method m = rMethods.testMethodList.get(i);
	  if(m.type.isVoidType())
		continue;
	  if(m.type.toString().equals("boolean"))
		System.out.println("		String p_" + i + "_res;");
	  else
		System.out.println("		" + m.type.toString() + " p_" + i + "_res;");
	  for(int j = 0; j < m.parameters.size(); j++){
		System.out.println("		" + m.parameters.get(j).getTypeAsString() + " p_" + i + "_" + j + ";");
	  }
	}
	return true;
  }





  public static boolean generateParseLine(RMethods rMethods){
	System.out.println("public static boolean parseline(String line){");
	System.out.println("	Scanner linescanner = new Scanner(line);");
	System.out.println("	if(line.equals(\"\")){");
	System.out.println("		linescanner.close();");
	System.out.println("		return true;");
	System.out.println("	}");
	System.out.println("	TraceLine tl = new TraceLine();");
	System.out.println("	tl.pretime = linescanner.nextLong();");
	System.out.println("	tl.posttime = linescanner.nextLong();");
	System.out.println("	tl.threadid = linescanner.nextInt();");
	System.out.println("	tl.actionName = linescanner.next();");
	for(int i = 0; i < rMethods.testMethodList.size();i++){
	  Method m = rMethods.testMethodList.get(i);
	  System.out.println("	if(tl.actionName.equals(\"" + m.methodName +"\")){");
	  if(m.parameters.isEmpty()){
		if(m.type.isVoidType()){//no parameter && no type
		  continue;
		}
		else{// has type && no parameter
		  if(m.type.toString().equals("int")){
			System.out.println("		tl.p_" + i + "_res = linescanner.nextInt();");
		  }
		  else if(m.type.toString().equals("long")){
			System.out.println("		tl.p_" + i + "_res = linescanner.nextLong();");
		  }
		  else if(m.type.toString().equals("double")){
			System.out.println("		tl.p_" + i + "_res = linescanner.nextDouble();");
		  }
		  else if(m.type.toString().equals("float")){
			System.out.println("		tl.p_" + i + "_res = linescanner.nextFloat();");
		  }
		  else if(m.type.toString().equals("byte")){
			System.out.println("		tl.p_" + i + "_res = linescanner.nextByte();");
		  }
		  else if(m.type.toString().equals("boolean")){
			System.out.println("		String str = linescanner.next();");
			System.out.println("		if(str.equals(\"True\"))");
			System.out.println("			tl.p_" + i + "_res = true;");
			System.out.println("		else");
			System.out.println("			tl.p_" + i + "_res = false;");
		  }
		  else{
			System.out.println("		tl.p_" + i + "_res = linescanner.next();");
		  }
		}
	  }
	  else{
		if(m.type.isVoidType()){//has parameter && no type
		  for(int j = 0; j < m.parameters.size();j++){
			Type t = m.parameters.get(j).getType();
			if(t.toString().equals("int")){
			  System.out.println("		tl.p_" + i + "_" + j + " = linescanner.nextInt();");
			}
			else if(t.toString().equals("double")){
			  System.out.println("		tl.p_" + i + "_" + j+ " = linescanner.nextDouble();");
			}
			else if(t.toString().equals("float")){
			  System.out.println("		tl.p_" + i + "_j = linescanner.nextFloat();");
			}
			else if(t.toString().equals("long")){
			  System.out.println("		tl.p_" + i + "_" + j + " = linescanner.nextLong();");
			}
			else if(t.toString().equals("byte")){
			  System.out.println("		tl.p_" + i + "_" + j + " = linescanner.nextByte();");
			}
			else{
			  System.out.println("		tl.p_" + i + "_" + j + " = linescanner.nextLong();");
			}
		  }
		}
		else{// has type && has parameter
		  for(int j = 0; j < m.parameters.size();j++){
			Type t = m.parameters.get(j).getType();
			if(t.toString().equals("int")){
			  System.out.println("		tl.p_" + i + "_" + j + " = linescanner.nextInt();");
			}
			else if(t.toString().equals("double")){
			  System.out.println("		tl.p_" + i + "_" + j + " = linescanner.nextDouble();");
			}
			else if(t.toString().equals("float")){
			  System.out.println("		tl.p_" + i + "_" + j + " = linescanner.nextFloat();");
			}
			else if(t.toString().equals("long")){
			  System.out.println("		tl.p_" + i + "_" + j + " = linescanner.nextLong();");
			}
			else if(t.toString().equals("byte")){
			  System.out.println("		tl.p_" + i + "_" + j + " = linescanner.nextByte();");
			}
			else{
			  System.out.println("		tl.p_" + i + "_" + j + " = linescanner.nextLong();");
			}
		  }
		  if(m.type.toString().equals("int")){
			System.out.println("		tl.p_" + i + "_res = linescanner.nextInt();");
		  }
		  else if(m.type.toString().equals("long")){
			System.out.println("		tl.p_" + i + "_res = linescanner.nextLong();");
		  }
		  else if(m.type.toString().equals("double")){
			System.out.println("		tl.p_" + i + "_res = linescanner.nextDouble();");
		  }
		  else if(m.type.toString().equals("float")){
			System.out.println("		tl.p_" + i + "_res = linescanner.nextFloat();");
		  }
		  else if(m.type.toString().equals("byte")){
			System.out.println("		tl.p_" + i + "_res = linescanner.nextByte();");
		  }
		  else{
			System.out.println("		tl.p_" + i + "_res = linescanner.next();");
		  }
		}
	  }
	  System.out.println("		trace.add(tl);");
	  System.out.println("	}");

	}
	System.out.println("	linescanner.close();");
	System.out.println("	return true;");
	System.out.println("}");
	return true;
  }
  public static boolean generateCheckLine_spec(RMethods rMethods){
	System.out.println("public static boolean checkline(int line){");
	System.out.println("	TraceLine tl = trace.get(line);");
	for(int i = 0; i < rMethods.testMethodList.size(); i++){
	  Method m = rMethods.testMethodList.get(i);
	  MethodDeclaration spec = m.spec;//spec
	  System.out.println("if(tl.actionName.equals(\"" + m.methodName +"\")){");
	  if(m.parameters.isEmpty()){
		if(m.type.isVoidType()){//no parameter && no type
		  System.out.println("	object." + spec.getNameAsString() + "();");
		}
		else{// has type && no parameter
		  String t1 = m.type.toString();
		  String t2 = spec.getType().toString();
		  if(t1.equals(t2)){
			System.out.println("	" + t1 + " res = object." + spec.getNameAsString() + "();");
		  }
		  else{
			System.out.println("Error: The type in " + m.methodName + " is not same to that in spec method " + spec.getNameAsString());
			return false;
		  }
		  if(isPrimitive(m.type.toString()))
			if(m.type.toString().equals("boolean"))
			  System.out.println("	if((res && tl.p_" + i + "_res.equals(\"False\")) ||(!res && tl.p_" + i + "_res.equals(\"True\")))");
			else
			  System.out.println("	if(res == tl.p_" + i + "_res)");
		  else
			System.out.println("	if(!res.equals(tl.p_" + i + "_res))");
		  System.out.println("		return false;");
		}
	  }
	  else{
		if(m.type.isVoidType()){//has parameter && no type
		  String print = "object." + spec.getNameAsString() + "(";
		  if(m.parameters.size() != spec.getParameters().size()){
			System.out.println("Error: The parameter number is not ");
			return false;
		  }
		  for(int j = 0; j < m.parameters.size(); j++){
			if(!m.parameters.get(j).getType().equals(spec.getParameter(j).getType())){
			  System.out.println("Error: The  " + j + "th parameters in " + m.methodName + " is not correspond to that in spec " + spec.getNameAsString() + " " + m.parameters.get(j).toString());

			  return false;
			}
			print = print + "tl.p_" + i +"_" + j;
			if(j != m.parameters.size() - 1)
			  print = print + ", ";
		  }
		  print = print + ");";
		  System.out.println(print);
		}
		else{// has type && has parameter
		  String print = "";
//			System.out.println(" m.methodname empty" + m.methodName);
		  if(spec.getType() == null)
			System.out.println(" m.methodname empty" + m.methodName);
		  if(m.type.equals(spec.getType())){
			print = "	" + m.type.toString() + " res = object." + spec.getNameAsString() + "(";
		  }
		  else{
			System.out.println("Error: The type in " + m.methodName + " is not same to that in spec method " + spec.getNameAsString());
			return false;
		  }
		  if(m.parameters.size() != spec.getParameters().size()){
			System.out.println("Error: The parameter number is not ");
			return false;
		  }
		  for(int j = 0; j < m.parameters.size(); j++){
			if(!m.parameters.get(j).getType().equals(spec.getParameter(j).getType())){
			  System.out.println("Error: The  " + j + "th parameters in " + m.methodName + " is not correspond to that in spec " + spec.getNameAsString() + " " + spec.getTypeAsString());
			  return false;
			}
			print = print + "tl.p_" + i +"_" + j;
			if(j != m.parameters.size() - 1)
			  print = print + ", ";
		  }
		  print = print + ");";
		  System.out.println(print);
		  if(isPrimitive(m.type.toString()))
			if(m.type.toString().equals("boolean"))
			  System.out.println("	if((res && tl.p_" + i + "_res.equals(\"False\")) ||(!res && tl.p_" + i + "_res.equals(\"True\")))");
			else
			  System.out.println("	if(res == tl.p_" + i + "_res)");
		  else
			System.out.println("	if(!res.equals(tl.p_" + i + "_res))");
		  System.out.println("		return false;");
		}
	  }		
	  System.out.println("}");
	}
	System.out.println("	return true;");
	System.out.println("}");
	return true;
  }

  public static boolean generateCheckLine_seq(RMethods rMethods){//name object
	System.out.println("public static boolean checkline(int line){");
	System.out.println("	TraceLine tl = trace.get(line);");
	for(int i = 0; i < rMethods.testMethodList.size(); i++){
	  Method m = rMethods.testMethodList.get(i);
	  System.out.println("if(tl.actionName.equals(\"" + m.methodName + "\")){");
	  if(m.parameters.isEmpty()){
		if(m.type.isVoidType()){//no parameter && no type
		  System.out.println("	object." + m.methodName + "();");
		}
		else{// no parameter && has type
			
		  System.out.println("	" + m.type.toString()+ " res = object." + m.methodName + "();");
		  if(isPrimitive(m.type.toString()))
			if(m.type.toString().equals("boolean"))
			  System.out.println("	if((res && tl.p_" + i + "_res.equals(\"False\")) ||(!res && tl.p_" + i + "_res.equals(\"True\")))");
			else
			  System.out.println("	if(res == tl.p_" + i + "_res)");
		  else
			System.out.println("	if(!res.equals(tl.p_" + i + "_res))");
		  System.out.println("		return false;");
		}
	  }
	  else{
		if(m.type.isVoidType()){//has parameter && no type
		  String print = "object." + m.methodName + "(";
		  for(int j = 0; j < m.parameters.size(); j++){
			if(j != m.parameters.size() - 1)
			  print = print + ", ";
			print = print + "tl.p_" + i + "_" + j;
		  }
		  print = print + ");";
		  System.out.println(print);
		}
		else{// has type && has parameter
		  String print = "	" + m.type.toString() + " res = " + "object." + m.methodName + "(";
		  for(int j = 0; j < m.parameters.size(); j++){
			print = print + "tl.p_" + i + "_" + j;
			if(j != m.parameters.size() - 1)
			  print = print + ", ";
		  }
		  print = print + ");";
		  System.out.println(print);
		  if(isPrimitive(m.type.toString()))
			if(m.type.toString().equals("boolean"))
			  System.out.println("	if((res && tl.p_" + i + "_res.equals(\"False\")) ||(!res && tl.p_" + i + "_res.equals(\"True\")))");
			else
			  System.out.println("	if(res == tl.p_" + i + "_res)");
		  else
			System.out.println("	if(!res.equals(tl.p_" + i + "_res))");
		  System.out.println("		return false;");
	
		}
	  }
	  System.out.println("}");
	}
	System.out.println("	return true;");
	System.out.println("}");
	return true;
  }
  
  public static boolean generateBackLine(RMethods rMethods){
	System.out.println("public static boolean backline(int line){");
	System.out.println("	TraceLine tl = trace.get(line);");
	for(int i = 0; i < rMethods.testMethodList.size(); i++){
	  Method m = rMethods.testMethodList.get(i);
	  MethodDeclaration dual = m.dual;//the parameter name is correspond to that of 
	  System.out.println("	if(tl.actionName.equals(\"" + m.methodName +"\")){");
	  if(dual.getParameters().isEmpty()){
		if(dual.getType().isVoidType()){//no parameter && no type
		  System.out.println("		object." + dual.getNameAsString() + "();");
		}
		else{// no paramter && has type
		  System.out.println("		" + dual.getTypeAsString() + "res = object." + dual.getNameAsString() + "();");
		  if(dual.getTypeAsString().equals("boolean")){
			System.out.println("		return res;");
		  }
		  else{
			System.out.println("		return true;");
			//check if the ret is correspond to ? //need to check?
		  }
		}
	  }
	  else{
		if(dual.getType().isVoidType()){//has parameter && no type
		  String print = "		object." + dual.getNameAsString() + "(";
		  for(int j = 0; j < dual.getParameters().size(); j++){
			String pname = dual.getParameter(j).getNameAsString();
			Type ptype = dual.getParameter(j).getType();
			if(pname.equals("result") && dual.getParameter(j).getType().equals(m.type)){
			  print = print + "tl.p_" + i + "_res";
			}
			else{
			  for(int k = 0; k < m.parameters.size(); k++){
				if(pname.equals(m.parameters.get(k).getNameAsString()) && ptype.equals(m.parameters.get(k).getType())){
				  print  = print + "tl.p_" + i + "_" + k;
				  break;
				}
				if(k == m.parameters.size() - 1){
				  System.out.println("There is no matching parameter named " + pname + " in method " + dual.getNameAsString());
				  return false;
				}
			  }
			}
			if(j != dual.getParameters().size() - 1)
			  print = print + ", ";
		  
		  }
		  print = print + ");";
		  System.out.println(print);
		}
		else{// has type && has parameter
		  String print = "		" + dual.getTypeAsString() + " res = object." + dual.getNameAsString() + "(";
		  for(int j = 0; j < dual.getParameters().size(); j++){
			String pname = dual.getParameter(j).getNameAsString();
			Type ptype = dual.getParameter(j).getType();
			if(pname.equals("result") && dual.getParameter(j).getType().equals(m.type))
			  print = print + "tl.p_" + i + "_res";
			else{
			  for(int k = 0; k < m.parameters.size(); k++){
				if(pname.equals(m.parameters.get(k).getNameAsString()) && ptype.equals(m.parameters.get(k).getType())){
				  print = print + "tl.p_" + i + "_" + j;
				  break;
				}
				if(k == m.parameters.size() - 1){
//				  System.out.println("pname = " + ptype.toString() + " m.name = " + m.parameters.get(k).getTypeAsString() + " ");
				  System.out.println("There is no matching parameter named " + pname + " in method " + dual.getNameAsString());
				  return false;
				}
			  }
			}
			if(j != dual.getParameters().size() - 1)
			  print = print + ", ";
		  }
		  print = print + ");";
		  System.out.println(print);
		  if(dual.getTypeAsString().equals("boolean"))
			System.out.println("		return res;");
		}
	  }

	  System.out.println("}");
	}
	System.out.println("	return true;");
	System.out.println("}");
	return true;
  }
/*  public static boolean generateBackLine_seq(RMethods rMethods){
	System.out.println("public static boolean backline(int line){");
	System.out.println("	TraceLine tl = trace.get(line);");
	for(int i = 0; i < rMethods.testMethodList.size(); i++){
	  Method m = rMethods.testMethodList.get(i);
	  MethodDeclaration dual = m.dual;
	  System.out.println("if(tl.actionName.equals(" + m.methodName +"){");
	  if(dual.getParameters().isEmpty()){
		if(dual.getType().isVoidType()){//no parameter && no type
		  System.out.println("	object." + m.dual.getNameAsString() + "();");
		  System.out.println("	if(res == false)");
		  System.out.println("		return false;");
		}
		else{// no parameter && has type
		  System.out.println("	" + m.type.toString() + " res = object." + m.dualName + "();");
		}
	  }
	  else{
		if(m.type.isVoidType()){//has parameter && no type
		  String print;
		  NodeList<Parameter> p = m.dual.getParameters();
		  for(int j = 0;j < p.size(); j++){
		  }
		}
		else{// has type && has parameter
		  String print = "	" + m.type.toString() + " res = object." + m.dualName + "(";
		  for(int j = 0; j < dual.
		}
	  }

	  System.out.println("}");
	}
	System.out.println("	return true;");
	System.out.println("}");
	return true;
  }*/

  public static boolean generateWriteLine(RMethods rMethods){
	System.out.println("public static boolean writeline(int line){");
	System.out.println("	TraceLine tl = trace.get(line);");
	for(int i = 0; i < rMethods.testMethodList.size(); i++){
	  Method m = rMethods.testMethodList.get(i);
	  System.out.println("if(tl.actionName.equals(\"" + m.methodName +"\")){");
	  String print ="	System.out.println(tl.pretime + \" \" + tl.posttime + \" \" + tl.threadid + \" \" + tl.actionName";
	  if(m.parameters.isEmpty()){
		if(m.type.isVoidType()){//no parameter && no type
		  print = print + ");";
		}
		else{// has type && no parameter
		  print = print + " + tl.p_"+ i +"_res);";	  
		}
	  }
	  else{
		if(m.type.isVoidType()){//has parameter && no type
		  for(int j = 0; j < m.parameters.size(); j++){
			if(j != m.parameters.size() - 1)
			  print = print + " + \" \"";
			print = print + " + tl.p_" + i + "_" + j + " + \" \"";
		  }
		  print = print + ");";
		}
		else{// has type && has parameter
		  for(int j = 0; j < m.parameters.size(); j++){
			print = print + " + \" \"";
			print = print + " + tl.p_" + i + "_" + j + " + \" \"";
		  }
		  print = print + " + tl.p_" + i + "_res);";
		}
	  }
	  System.out.println(print);
	  System.out.println("}");
	}
	System.out.println("	return true;");
	System.out.println("}");
	return true;
  }

  public static void main(String[] args){
	ConfigParser parser = new ConfigParser();
	parser.parseConfig("config");
	RMethods rMethods = parser.rMethods;
	if(rMethods == null){
	  System.out.println("Error: Error in parsing config.");
	  return;
	}
	isSpec = rMethods.hasSpec;
	try{
	  FileOutputStream f = new FileOutputStream("verify/VeriLin.java");
	  System.setOut(new PrintStream(f));
	  if(!copyFile("src/main/java/LinPart/region.part1")){
		System.out.println("Error: copying region.part1.");
		return;
	  }
	  if(!generateTraceStructure(rMethods)){
		System.out.println("Error: Generateing TraceStructure.");
		return;
	  }
	  if(!copyFile("src/main/java/LinPart/region.part2")){
		System.out.println("Error: copying region.part2.");
		return;
	  }
	  if(isSpec){
	  if(!generateObject(rMethods)){
		System.out.println("Error: Generateing Object.");
		return;
	  }
	  if(!generateParseLine(rMethods)){
		System.out.println("Error: Generateing ParseLine.");
		return;
	  }
	  if(!generateBackLine(rMethods)){
		System.out.println("Error: Generateing BackLine.");
		return;
	  }
		if(!generateCheckLine_spec(rMethods)){
		  System.out.println("Error: Generateing CheckLine.");
		  return;
		}
	  }
	  else{
	  if(!generateObject(rMethods)){
		System.out.println("Error: Generateing Object.");
		return;
	  }
	  if(!generateParseLine(rMethods)){
		System.out.println("Error: Generateing ParseLine.");
		return;
	  }
	  if(!generateBackLine(rMethods)){
		System.out.println("Error: Generateing BackLine.");
		return;
	  }
		if(!generateCheckLine_seq(rMethods)){
		  System.out.println("Error: Generateing CheckLine.");
		  return;
		}
	  }
	  if(!generateWriteLine(rMethods)){
		System.out.println("Error: Generateing WriteLine.");
		return;
	  }
	  if(!copyFile("src/main/java/LinPart/region.part3")){
		System.out.println("Error: copying region.part3.");
		return;
	  }
	  System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
	}catch(FileNotFoundException e){
	  System.out.println(e);
	}
  }
}
