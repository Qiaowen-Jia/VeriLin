package AutoGenerator;
import java.io.File;
/****************************************************************
-- File Name: ConfigParser.java
-- Author: Qiaowen Jia
-- mail: jiaqw@ios.ac.cn
-- Created Time: Mon Jan 25 15:57:39 2021
****************************************************************************/
import java.util.*;
import java.io.FileNotFoundException;
import com.github.javaparser.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.utils.*;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class ConfigParser{
  public static RMethods rMethods;
  public static boolean ObjectFlag;
  public static boolean IsJavaFlag;
  public static boolean FileTypeFlag;
  public static boolean DualRelationFlag;
  public static boolean BoundFlag;
  public static boolean MethodListFlag;
  public static boolean MethodFreqFlag;
  public static boolean SpecFlag;
  public static boolean SpecFileFlag;
  public static boolean SpecRelationFlag;
  public static String type;
  public static String Suffix;
  public static List<MethodDeclaration> methods;
  public static List<MethodDeclaration> testMethods;

  public ConfigParser(){
	ObjectFlag = false;
	FileTypeFlag = false;
	DualRelationFlag = false;
	MethodListFlag = false;
	MethodFreqFlag = false;
	BoundFlag = false;
	SpecFlag = false;
	SpecFileFlag = false;
	SpecRelationFlag = false;
	IsJavaFlag = true;
	Suffix = "java";
	rMethods = new RMethods();
	methods = new ArrayList<MethodDeclaration>();
	testMethods = new ArrayList<MethodDeclaration>();
  }

  public static MethodDeclaration searchMethod(String str){
	if(!ObjectFlag){
	  System.out.println("Error: The objectname has not been set before methodList.");
	  return null;
	}
	for(int i = 0; i < methods.size(); i++){
	  if(methods.get(i).getName().toString().trim().equals(str)){
		return methods.get(i);
	  }
	}
	return null;
  }
   
  public static List<MethodDeclaration> getMethodList(CompilationUnit cu) {
          List<MethodDeclaration> methodList = new ArrayList<MethodDeclaration>();
          new MethodGetterVisitor().visit(cu, methodList);
         return methodList;
  }     
  private static class MethodGetterVisitor extends VoidVisitorAdapter<Object> { 
	@SuppressWarnings("unchecked")
    @Override
    public void visit(MethodDeclaration n, Object arg) {
      List<MethodDeclaration> methodList = new ArrayList<MethodDeclaration>();
        methodList =  (List<MethodDeclaration>) arg;
        methodList.add(n);
      }
  }

  public static boolean isFileExist(String filename){
	if(!rMethods.preDefined){
	  File file = new File(filename);
	  if(!file.exists() && !file.isDirectory())
		return false;
	  return true;
	}
	else{
	  String[] sp = filename.split("_");
	  if(sp.length != 2){
		return false;
	  }
	  else{
		type = sp[0];
		if((!sp[0].equals("list")) && (!sp[0].equals("stack")) && (!type.equals("queue")) && (!type.equals("set")) && (type.equals("hashmap"))){
		  return false;
		}
		else{
		  File file = new File("src/main/java/PreDefined/" + type + "/" + filename);
		  if(!file.exists() && !file.isDirectory())
			return false;
		  return true;
		}
	  }
	}
  }
  public static boolean isValid(){
	if(ObjectFlag && FileTypeFlag && MethodListFlag && MethodFreqFlag && DualRelationFlag){
//	  if((!SpecFlag && SpecRelationFlag) || (SpecFlag && !SpecRelationFlag))
//		return false;
	  return true;
	}
	return false;
  }

  public static String elimAllSymbol(String str){
	String elimstr = "";
	char[] s = str.toCharArray();
	for(int i = 0; i < s.length; i++)
	  if((s[i] == ' ') || (s[i] == ';') || (s[i] == ',') || (s[i] == '(') || (s[i] == ')') || (s[i] == '{') || (s[i] == '}') || (s[i] == '-'))
		continue;
	  else
		elimstr = elimstr + s[i];
	return elimstr;	
  }


  public static boolean parseObjectName(String str){
	String[] splitstr = str.trim().split("=");
	if(splitstr.length != 2){
	  System.out.println("Syntax Error in setting object name of configuration.");
	  return false;
	}
	if(!elimAllSymbol(splitstr[0]).equals("ObjectName")){
	  System.out.println("Syntax Error in ObjectName.");
	  return false;
	}
	String name = elimAllSymbol(splitstr[1]);
	rMethods.objectName = name;
	ObjectFlag = true;
	return true;
  }
  public static boolean parseFileType(String str){
	String[] splitstr = str.trim().split("=");
	if(splitstr.length != 2){
	  System.out.println("Syntax Error in setting FileType of configuration.");
	  return false;
	}
	if(!elimAllSymbol(splitstr[0]).equals("FileType")){
	  System.out.println("Syntax Error in FileType.");
	  return false;
	}
	String type = elimAllSymbol(splitstr[1]);
	if(type.toUpperCase().equals("PREDEFINED")){
	  rMethods.preDefined = true;
	}
	else if(type.toUpperCase().equals("SELFDEFINED")){
	  rMethods.preDefined = false;
	}
	else{
	  System.out.println("Syntax Error in setting Filetype.");
	  return false;
	}
	FileTypeFlag = true;
	return true;
  }
  public static boolean parseFileSuffix(String str){
	String[] splitstr = str.trim().split("=");
	if(splitstr.length != 2){
	  System.out.println("Syntax Error in setting FileSuffix of configuration.");
		return false;
	}
	if(!elimAllSymbol(splitstr[0]).equals("FileSuffix")){
	  System.out.println("Syntax Error in Implementing Language.");
	  return false;
	}
	String language = elimAllSymbol(splitstr[1]);
	if(!language.equals("java")){
	  IsJavaFlag = false;
	  Suffix = language;
	  rMethods.setFileSuffix(language);
	}
	else{
	  IsJavaFlag = true;
	  Suffix = "java".trim();
	  rMethods.setFileSuffix("java".trim());
	}
	return true;

  }
  public static boolean parseMethodList(String str){
	if(IsJavaFlag && ObjectFlag){//解析主文件的methodlist
//	  System.out.println("flag set right.");
	  File f = new File(rMethods.objectName + ".java");
	  try{
		CompilationUnit cu = StaticJavaParser.parse(f);
		methods = new ArrayList<MethodDeclaration>();
		MethodGetterVisitor v = new MethodGetterVisitor();
		v.visit(cu, methods);
	  }catch(FileNotFoundException e){
		System.out.println(e);
		return false;
	  }
//	  for(int i = 0; i < methods.size();i++)
//		System.out.println("PARSE MAIN FILE DONE. method"+ i +" = " +methods.get(i).getName());
//	new MethodGetterVisitor.visit(cu, methods);
	}

	String[] splitstr = str.split(":");
	if(splitstr.length != 2){
	  System.out.println("Syntax Error in setting MethodList of configuration.");
	  return false;
	}
	if(!elimAllSymbol(splitstr[0]).equals("MethodList")){
	  System.out.println("Syntax Error in MethodList.");
	  return false;
	}
	String list = splitstr[1].trim();
	String[] splitlist = list.split(",");
	for(int i = 0; i < splitlist.length; i++){
	  String method = elimAllSymbol(splitlist[i].trim());

	  MethodDeclaration md = searchMethod(method);
	  if(md == null){
		System.out.println("Error: method " + md +" is not in file " + rMethods.objectName + ".java.");
		return false;
	  }
	  else{
		rMethods.addMethod(method,md.getType(), md.getParameters());
//		Method m = new Method(i, method, rMethods.objectName);
//		m.methodInfo = md;
//		rMethods.addMethod(m);

		testMethods.add(md);
	  }
	}

	MethodListFlag = true;
	return true;
  }

  public static boolean parseMethodFreq(String str){
	String[] splitstr = str.split(":");
	if(splitstr.length != 2){
	  System.out.println("Syntax Error in setting MethodFreq of configuration.");
	  return false;
	}
	if(!elimAllSymbol(splitstr[0]).equals("MethodFreq")){
	  System.out.println("Syntax Error in MethodFreq.");
	  return false;
	}
	String list = splitstr[1].trim();
	String[] splitlist = list.split(",");
	if(splitlist.length != rMethods.methodNum){
	  System.out.println("Error: The number of MethodFreq " + splitlist.length + " is not corresponding to the Method List " + rMethods.methodNum );
	  return false;
	}
	for(int i = 0; i < splitlist.length; i++){
	  String method = elimAllSymbol(splitlist[i].trim());
	  int freq = Integer.parseInt(method); 
	  rMethods.testMethodFreq.add(freq); 
	}
	MethodFreqFlag = true;
	return true;
  }
/*  public static boolean parseDualFileName(String str){
	String[] splitstr = str.trim().split("=");
	if(splitstr.length != 2){
	  System.out.println("Syntax Error in setting object name of configuration.");
	  return false;
	}
	if(!elimAllSymbol(splitstr[0]).equals("DualFileName")){
	  System.out.println("Syntax Error in DualFilename.");
	  return false;
	}
	String name = elimAllSymbol(splitstr[1]);
	if(IsJavaFlag){
	  if(!isFileExist(name + ".java")){
		System.out.println("Error: The file " + name + ".java is not exist.");
		return false;
	  }
	}
	else{
	  if(!isFileExist(name + "." + Suffix)){
		System.out.println("Error: The file " + name + "." + Suffix.trim() + " is not exist.");
		return false;
	  }
	}
	rMethods.dualObject = name;
	File f = new File(name + ".java");
	try{
	  CompilationUnit cu = StaticJavaParser.parse(f);
	  rMethods.dualMethodList = new ArrayList<MethodDeclaration>();
	  MethodGetterVisitor v = new MethodGetterVisitor();
	  v.visit(cu, methods);
	}catch(FileNotFoundException e){
	  System.out.println(e);
	}

	DualFileFlag = true;
	return true;
  }*/
  public static boolean parseSpec(String str){
	String[] splitstr = str.trim().split("=");
	if(splitstr.length != 2){
	  System.out.println("Syntax Error in setting object name of configuration.");
	  return false;
	}
	if(!elimAllSymbol(splitstr[0]).equals("SpecFlag")){
	  System.out.println("Syntax Error in SpecFlag");
	  return false;
	}
	String flag = elimAllSymbol(splitstr[1].trim());
	if(flag.equals("true"))
	  SpecFlag = true;
	else if(flag.equals("false"))
	  SpecFlag = false;
	else{
	  System.out.println("Syntax Error in SpecFlag");
	  return false;
	}
	return true;
  }
  public static boolean parseSpecFileName(String str){
	if(!SpecFlag){
	  System.out.println("Error: The SpecFlag has not been set before the definition of SpecFileName.");
	  return false;
	}
	String[] splitstr = str.trim().split("=");
	if(splitstr.length != 2){
	  System.out.println("Syntax Error in setting object name of configuration.");
	  return false;
	}
	if(!elimAllSymbol(splitstr[0]).equals("SpecFileName")){
	  System.out.println("Syntax Error in SpecFilename.");
	  return false;
	}
	String name = elimAllSymbol(splitstr[1]);
	if(IsJavaFlag){
	  if(!isFileExist(name + ".java")){
		System.out.println("Error: The file " + name + ".java is not exist.");
		return false;
	  }
	}
	else{
	  if(!isFileExist(name + "." + Suffix)){
		System.out.println("Error: The file " + name + "." + Suffix + "is not exist.");
		return false;
	  }
	}
	rMethods.specObject = name;
	rMethods.hasSpec = true;
	File f;
	if(rMethods.preDefined){
	  f = new File("src/main/java/PreDefined/" + type + "/"+ name + ".java");
	}
	else
	  f = new File(name + ".java");
	try{
//	  System.out.println(f.getPath());
	  CompilationUnit cu = StaticJavaParser.parse(f);
	  rMethods.specMethodList = new ArrayList<MethodDeclaration>();
	  MethodGetterVisitor v = new MethodGetterVisitor();
	  v.visit(cu, rMethods.specMethodList);
	}catch(FileNotFoundException e){
	  System.out.println(e);
	}
	SpecFileFlag = true;
	return true;
  
  }
  // here the dual might be itself
  public static boolean parseDualRelation(String str){
	if(rMethods.dualMethodList == null){
	  if(rMethods.hasSpec && rMethods.specMethodList != null){
		rMethods.dualMethodList = rMethods.specMethodList;
	  }
	  else{
		if(!rMethods.hasSpec){
		  rMethods.dualMethodList = methods;
		}
		else{
		  System.out.println("The spec should be defined before dual in the configuration.");
		  return false;
		}
	  }
	}
	String[] splitstr = str.split(":");
	if(splitstr.length != 2){
	  System.out.println("Syntax Error in setting dual relation of configuration.1");
	  return false;
	}
	if(!elimAllSymbol(splitstr[0]).equals("DualRelation")){
	  System.out.println("Syntax Error in DualRelation.");
	  return false;
	}
	String[] splitline = splitstr[1].trim().split(",");
	if(splitline.length != 2){
	  System.out.println("Syntax Error in setting dual relation of configuration.2");
	  return false;
	}
	String mainname = elimAllSymbol(splitline[0]);
	String dualname = elimAllSymbol(splitline[1]);
	Method m = null;
	for(int i = 0; i < rMethods.testMethodList.size(); i++){
	  if(rMethods.testMethodList.get(i).methodName.equals(mainname) || rMethods.testMethodList.get(i).specName.equals(mainname))
		m = rMethods.testMethodList.get(i);
	}
	if(m == null){
	  System.out.println("Error: There is no method named " + mainname + "in " + rMethods.objectName + ".java.");
	  return false;
	}
	else{
/*	  if(!SpecFileFlag){
		System.out.println("Error: The dual file has not been set before dual relations.");
		return false;
	  }*/
	  m.setDual(dualname);
	}

	for(int i = 0; i < rMethods.dualMethodList.size(); i++){
	  if(rMethods.dualMethodList.get(i).getNameAsString().equals(dualname)){
		m.setDualMethod(rMethods.dualMethodList.get(i));
		break;
	  }
	  if(i == rMethods.dualMethodList.size() - 1){
		System.out.println("There is no method named " + dualname + " in File " + rMethods.dualObject);
		return false;
	  }
	}
	boolean flag = true;
	for(int i = 0; i < rMethods.methodNum; i++){
	  if(rMethods.testMethodList.get(i).dual == null){
//		System.out.println(rMethods.testMethodList.get(i).methodName + " dual false");
		flag = false;
	  }
	}
	DualRelationFlag = flag;
	return true;
  }
  public static boolean parseSpecRelation(String str){
	if(!SpecFlag){
	  System.out.println("Error: The SpecFlag has not been set before the definition of SpecRelation.");
	  return false;
	}
	String[] splitstr = str.trim().split(":");
	if(splitstr.length != 2){
	  System.out.println("Syntax Error in setting dual relation of configuration.");
	  return false;
	}
	if(!elimAllSymbol(splitstr[0]).equals("SpecRelation")){
	  System.out.println("Syntax Error in SpecRelation.");
	  return false;
	}
	String[] splitline = splitstr[1].trim().split(",");
	if(splitline.length != 2){
	  System.out.println("Syntax Error in setting spec relation of configuration.");
	  return false;
	}
	String mainname = elimAllSymbol(splitline[0]);
	String specname = elimAllSymbol(splitline[1]);
	Method m = rMethods.getMethodByName(mainname);
	if(m == null){
	  System.out.println("Error: There is no method named " + mainname + "in " + rMethods.objectName + ".java.");
	  return false;
	}
	else{
	  if(!SpecFileFlag){
		System.out.println("Error: The spec file has not been set before spec relations.");
		return false;
	  }
	  m.setSpec(specname); 
	}

	for(int i = 0; i < rMethods.specMethodList.size(); i++){
	  if(rMethods.specMethodList.get(i).getNameAsString().equals(specname)){
		m.setSpecMethod(rMethods.specMethodList.get(i));
		break;
	  }
	  if(i == rMethods.specMethodList.size() - 1){
		System.out.println("There is no method named " + specname + " in File " + rMethods.specObject);
		return false;
	  }
	}
	boolean flag = true;
	for(int i = 0; i < rMethods.methodNum; i++){
	  if(rMethods.testMethodList.get(i).spec == null)
		flag = false;
	}
	SpecRelationFlag = flag;	
	return true;
  }
  public static boolean parseBound(String str){
	String[] splitstr = str.split(":");
	if(splitstr.length != 2){
	  System.out.println("Syntax Error in setting object name of Configuration.");
	  return false;
	}
	String[] splitline = splitstr[0].trim().split(" ");
	if(splitline.length < 2){
	  System.out.println("Syntax Error in setting object name of Configuration.");
	  return false;
	}
	if(!elimAllSymbol(splitline[0]).equals("SetBound")){
	  System.out.println("Syntax Error in SetBound");
	  return false;	  
	}
	String[] splitmethod = splitline[1].split("\\.");
	if(splitmethod.length < 2){
	  System.out.println("Syntax Error in SetBound." + splitmethod.length);
	  return false;
	}
	String method = elimAllSymbol(splitmethod[0].trim());
	Method m = rMethods.getMethodByName(method);
	if(m == null){
	  System.out.println("Error: There is no method named " + method + " in " + rMethods.objectName + ".");
	  return false;
	}
	if(m.parameters == null){
	  System.out.println("Error: There is no parameters in method " + method);
	  return false;
	}
	String parameter = elimAllSymbol(splitmethod[1].trim());
	Type t = null;
	Parameter p = null;
	for(int i = 0; i < m.parameters.size();i++){
	  if(m.parameters.get(i).getName().toString().trim().equals(parameter)){
		t = m.parameters.get(i).getType();
		p = m.parameters.get(i);
	  }
	}
	splitline = splitstr[1].split(",");
	if(t == null || p == null) {
	  System.out.println("Error: There is no parameter named " + parameter + " in method " + method + ".");
	  return false;
	}else{
	  String lower = elimAllSymbol(splitline[0]);
	  String upper = elimAllSymbol(splitline[1]);
	  m.setBound(p, lower, upper);
	}
	
	BoundFlag = true;
	return true;
  }
  public static boolean parseConfig(String filename){
	try{
	  Scanner scanner = new Scanner(new File(filename));
	  String line = "";
	  int count = 0;
	  boolean flag = false;
	  while(scanner.hasNextLine()){
		line = scanner.nextLine();
		count ++;
		if(line.equals("") || line.charAt(0) == '#')
		  continue;
		else if(line.contains("ObjectName")){
		  flag = parseObjectName(line);
		  if(flag)
			System.out.println("Set ObjectName to " + rMethods.objectName + " successfully.");
			else{
			  System.out.println("Syntax Error in line: " + count + ". Set ObjectName incorrect.");  
			  break;
			}
		}
		else if(line.contains("FileSuffix")){
		  flag = parseFileSuffix(line);
		  if(flag){
			if(IsJavaFlag)
			  System.out.println("Set Language to java successfully.");
			else
			  System.out.println("Set Language to " + Suffix + " successfully.");
		  }
		  else{
			System.out.println("Syntax Error in line: " + count + ". Set Implementing Language incorrect.");
			  break;
		  }
		}
		else if(line.contains("FileType")){
		  flag = parseFileType(line);
		  if(flag){
			if(rMethods.preDefined)
			  System.out.println("Set FileType to PreDefined successfully.");
			else
			  System.out.println("Set FileType to SelfDefined successfully.");
		  }
		  else{
			System.out.println("Syntax Error in line: " + count + ". Set FileType incorrect.");
			break;
		  }
		}
		else if(line.contains("MethodList")){
		  flag = parseMethodList(line);
		  if(flag){
			System.out.println("Set MethodList successfully.");
		  }
		  else{
			System.out.println("Syntax Error in line: " + count + ". Set MethodList incorrect.");
			break;
		  }
		}
		else if(line.contains("MethodFreq")){
		  flag = parseMethodFreq(line);
		  if(flag){
			System.out.println("Set MethodFreq successfully.");
		  }
		  else{
			System.out.println("Syntax Error in line: " + count + ". Set MethodFreq incorrect.");
			break;
		  }
		}

/*		else if(line.contains("DualFileName")){
		  flag = parseDualFileName(line);
		  if(flag)
			System.out.println("Set ObjectName to " + rMethods.dualObject + " successfully.");
		  else{
			System.out.println("Syntax Error in line: " + count + ". Set DualFileName incorrect.");
			break;
		  }
		}*/
		else if(line.contains("DualRelation")){
		  flag = parseDualRelation(line);
		  if(flag){
			System.out.println("Set DualRelation successfully.");
		  }
		  else{
			System.out.println("Syntax Error in line: " + count + ". Set DualRelation incorrect.");
			break;
		  }
		}
		else if(line.contains("SpecFlag")){
		  flag = parseSpec(line);
		  if(flag)
			System.out.println("Set SpecFlag ssuccessfully.");
		  else{
			System.out.println("Syntax Error in line: " + count + ". SSet SpecFlag incorrect.");
			break;
		  }
		}
		else if(line.contains("SpecFileName")){
		  flag = parseSpecFileName(line);
		  if(flag)
			System.out.println("Set SpecFileName to " + rMethods.specObject + " successfully.");
		  else{
			System.out.println("Syntax Error in line: " + count + ". Set SpecFileName incorrect.");
			break;
		  }
		}
		else if(line.contains("SpecRelation")){
		  flag = parseSpecRelation(line);
		  if(flag){
			System.out.println("Set Spec Relation successfully.");
		  }
		  else{
			System.out.println("Syntax Error in line: " + count + ". Set SpecRelation incorrect.");
			break;
		  }
		}
		else if(line.contains("SetBound")){
		  flag = parseBound(line);
		  if(flag){
			System.out.println("Set Bound successfully.");
		  }
		  else{
			System.out.println("Syntax Error in line: " + count + ". Set Bound incorrect.");
			break;
		  }
		}
		else{
		  flag = false;
		  System.out.println("Syntax Error in line: " + count + ". The command is not exist.");
		  System.out.println(line);
		  return false;
		}
	  }
	  boolean flag1 = true;
	  if(SpecFlag){
		for(int i = 0; i < rMethods.testMethodList.size(); i++){
		  String sname = rMethods.testMethodList.get(i).methodName;
		  if(rMethods.testMethodList.get(i).spec == null){
			for(int j = 0; j < testMethods.size();j++){
			  if(testMethods.get(j).getNameAsString().equals(sname)){
				rMethods.testMethodList.get(i).spec = testMethods.get(j);
				System.out.println("already set spec " + rMethods.testMethodList.get(i).methodName);
			  }	
			  else
				flag1 = false;
			}
		  }
		}
	  }else{
		for(int i = 0; i < rMethods.testMethodList.size(); i++){
		  String sname = rMethods.testMethodList.get(i).methodName;
		  if(rMethods.testMethodList.get(i).spec == null){
			for(int j = 0; j < testMethods.size();j++){
			  if(testMethods.get(j).getNameAsString().equals(sname)){
				rMethods.testMethodList.get(i).spec = testMethods.get(j);
				System.out.println("already set spec " + rMethods.testMethodList.get(i).methodName);
			  }	
			  else
				flag1 = false;
			}
		  }
		}		
	  }
	  if(flag1)
		SpecRelationFlag = true;
	  scanner.close();
	  return flag;

	}catch(FileNotFoundException e){
	  System.out.println(e);
	}
	return false;
  }
  public static void main(String[] args){
	testMethods = new ArrayList<MethodDeclaration>();
	rMethods = new RMethods();
	boolean flag = parseConfig("config");
	if(flag)
	  System.out.println("Successfully Parsed Configuration.");
	else
	  System.out.println("Failed Parsed Configuration.");
	if(isValid())
	  rMethods.printstate();
	else
	  System.out.println("Not valid");
  }

}
