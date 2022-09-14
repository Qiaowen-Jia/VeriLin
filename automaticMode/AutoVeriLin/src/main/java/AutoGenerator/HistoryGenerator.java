package AutoGenerator;

import java.io.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class HistoryGenerator{
  public static boolean copyFile(String fileName){
	try{
	  File f = new File(fileName);
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


  public static void printMethodRand(Method m){
	if(m.parameters.isEmpty()){
	  System.out.println("long preTime = System.nanoTime() - startTime;");
	  if(m.type.isVoidType()){
		System.out.println("object." + m.methodName + "();");
		System.out.println("long postTime = System.nanoTime() - startTime;");
		System.out.println("System.out.println(preTime + \" \" + postTime + \" \" + ThreadId.get() + \" \" + " + m.methodName + ");");
	  }
	  else{//has type
		System.out.println(m.type.toString() + " result = object." + m.methodName + "();");
		System.out.println("long postTime = System.nanoTime() - startTime;");
		if(m.type.toString().equals("boolean")){
		  System.out.println("if(result)");
		  System.out.println("	System.out.println(preTime + \" \" + postTime + \" \" + ThreadId.get() + \" \" + \" " + m.methodName + " \" + \" \" + \"True\");");
		  System.out.println("else");
		  System.out.println("	System.out.println(preTime + \" \" + postTime + \" \" + ThreadId.get() + \" \" + \" " + m.methodName + " \" + \" \" + \"False\");");
		}
		else{
		  System.out.println("	System.out.println(preTime + \" \" + postTime + \" \" + ThreadId.get() + \" \" + \" " + m.methodName + " \" + \" \" + result);");
		}
	  }
	}
	else{ // has parameters
	  String print = "	System.out.println(preTime + \" \" + postTime + \" \" + ThreadId.get() + \" \" + \"" + m.methodName + "\" + \" \"";
	  String call;
	  if(m.type.isVoidType()){
		call = "object." + m.methodName + "(";
		if(!m.hasBound){
		  for(int i = 0; i < m.parameters.size(); i++){
			String t = m.parameters.get(i).getTypeAsString();
			if(t.equals("int")){
			  System.out.println("int p" + i + " = rand.nextInt();");
			}
			else if(t.equals("double")){
			  System.out.println("double p" + i + " = rand.nextDouble();");
			}
			else if(t.equals("float")){
			  System.out.println("float p" + i + " = rand.nextFloat();");
			}
			else if(t.equals("long")){
			  System.out.println("long p" + i + " = rand.nextLong();");
			}
			else if(t.equals("short")){
			  System.out.println("short p" + i + " = rand.nextShort();");
			}
			else if(t.equals("boolean")){
			  System.out.println("boolean p" + i + " = rand.nextBoolean();");
			}
			else if(t.equals("byte")){
			  System.out.println("byte p" + i + " = rand.nextByte();");
			}
			else if(t.equals("String")){
			  System.out.println("String p" + i + " = \"str" + i + "\"");
			}
			else{
			  System.err.println("Error: the Type in Trace is not correct.");
			  return;
			}
			call = call + " p" + i;
			String append = " + p" + i + " + \" \"";
			print = print + append;
			if(i != m.parameters.size() - 1)
			  call = call + ", ";
		  }
		}
		else{//has bound && has parameters && void type
		  for(int i = 0; i < m.parameters.size(); i++){
			for(int j = 0; j < m.bounds.size(); i++){
			  if(m.parameters.get(i).equals(m.bounds.get(j).parameter)){
				String t = m.parameters.get(i).getTypeAsString();
				if(t.equals("int")){
				  int upper = (int)m.bounds.get(j).getUpper();
				  int lower = (int)m.bounds.get(j).getLower();
				  int s = upper - lower;
				  System.out.println("int p" + i + " = rand.nextInt(" + s + ") + " + lower + ";");
				  String append = "+ p" + i + " + \" \"";
				  print = print + append;
				  break;
				}
				else if(t.equals("double")){
				  double upper = (double)m.bounds.get(j).getUpper();
				  double lower = (double)m.bounds.get(j).getLower();
				  double s = upper - lower;
				  System.out.println("double p" + i + " = rand.nextDouble(" + s + ") + " + lower + ";");
				  String append = "+ p" + i + " + \" \"";
				  print = print + append;
				  break;
				}
				else if(t.equals("float")){
				  float upper = (float)m.bounds.get(j).getUpper();
				  float lower = (float)m.bounds.get(j).getLower();
				  float s = upper - lower;
				  System.out.println("float p" + i + " = rand.nextFloat(" + s + ") + " + lower + ";");
				  String append = "+ p" + i + " + \" \"";
				  print = print + append;
				  break;
				}
				else if(t.equals("long")){
				  long upper = (long)m.bounds.get(j).getUpper();
				  long lower = (long)m.bounds.get(j).getLower();
				  long s = upper - lower;
				  System.out.println("long p" + i + " = rand.nextLong(" + s + ") + " + lower + ";");
				  String append = "+ p" + i + " + \" \"";
				  print = print + append;
				  break;
				}
			  }
			}
			call = call + " p" + i;
//			call = call + " p" + i + " + \" \"";
			if(i != m.parameters.size() - 1)
			  call = call + ",";
		  }
		}
		call = call + ");";
//		print = print + ");";
	  }
	  else{
		call = m.type.asString() + " result = object." + m.methodName + "(";
		if(!m.hasBound){
		  for(int i = 0; i < m.parameters.size(); i++){
			String t = m.parameters.get(i).getTypeAsString();
			if(t.equals("int")){
			  System.out.println("int p" + i + " = rand.nextInt();");
			}
			else if(t.equals("double")){
			  System.out.println("double p" + i + " = rand.nextDouble();");
			}
			else if(t.equals("float")){
			  System.out.println("float p" + i + " = rand.nextFloat();");
			}
			else if(t.equals("long")){
			  System.out.println("long p" + i + " = rand.nextLong();");
			}
			else if(t.equals("short")){
			  System.out.println("short p" + i + " = rand.nextShort();");
			}
			else if(t.equals("byte")){
			  System.out.println("byte p" + i + " = rand.nextByte();");
			}
			else if(t.equals("boolean")){
			  System.out.println("boolean p" + i + " = rand.nextBoolean();");
			}
			else{
			  System.err.println("Error: Type is incorrect in Trace.");
			  return;
			}
			call = call + "p" + i;
			if(i != m.parameters.size() - 1)
			  call = call + ", ";
			String append = "+ p" + i + " + \" \"";
			print = print + append;
		  }
		}
		else{//has bound
		  for(int i = 0; i < m.parameters.size(); i++){
			for(int j = 0; j < m.bounds.size(); j++){
			  if(m.parameters.get(i).equals(m.bounds.get(j).parameter)){
				String t = m.parameters.get(i).getTypeAsString();
				if(t.equals("int")){
				  int upper = (int)m.bounds.get(j).getUpper();
				  int lower = (int)m.bounds.get(j).getLower();
				  int s = upper - lower;
				  System.out.println("int p" + i + " = rand.nextInt(" + s + ") + " + lower + ";");
				  String append = "+ p" + i + " + \" \"";
				  print = print + append;
				  break;
				}
				else if(t.equals("double")){
				  double upper = (double)m.bounds.get(j).getUpper();
				  double lower = (double)m.bounds.get(j).getLower();
				  double s = upper - lower;
				  System.out.println("double p" + i + " = rand.nextDouble(" + s + ") + " + lower + ";");
				  String append = "+ p" + i + " + \" \"";
				  print = print + append;
				  break;
				}
				else if(t.equals("float")){
				  float upper = (float)m.bounds.get(j).getUpper();
				  float lower = (float)m.bounds.get(j).getLower();
				  float s = upper - lower;
				  System.out.println("float p" + i + " = rand.nextFloat(" + s + ") + " + lower + ";");
				  String append = "+ p" + i + " + \" \"";
				  print = print + append;
				  break;
				}
				else if(t.equals("long")){
				  long upper = (long)m.bounds.get(j).getUpper();
				  long lower = (long)m.bounds.get(j).getLower();
				  long s = upper - lower;
				  System.out.println("long p" + i + " = rand.nextLong(" + s + ") + " + lower + ";");
				  String append = "+ p" + i + " + \" \"";
				  print = print + append;
				  break;
				}
			  }
			}
			call = call + " p" + i;
			if(i != m.parameters.size() - 1 )
			  call = call + ", ";
		  }
		}
		call = call + ");";
	  }
	  System.out.println("long preTime = System.nanoTime() - startTime;");
	  System.out.println(call);
	  System.out.println("long postTime = System.nanoTime() - startTime;");
	  if(m.type.isVoidType())
		System.out.println(print + ");");
	  else{
		if(m.type.toString().equals("boolean")){
		  String print1 = print + " + \" True \");";
		  String print2 = print + " + \" False \");";
		  System.out.println("if(result)");
		  System.out.println(print1);
		  System.out.println("else");
		  System.out.println(print2);
		}
		else{
		  System.out.println(print + " + \" \" + result);");
		}
	  }
	}
  }

  public static void main(String[] args){
	ConfigParser parser = new ConfigParser();
	parser.parseConfig("config");
	RMethods rMethods = parser.rMethods;
	try{
	  FileOutputStream f = new FileOutputStream("verify/GenerateHistory.java");
	  System.setOut(new PrintStream(f));
	  if(!copyFile("src/main/java/TracePart/trace.part1")){
		System.out.println("Error: copying trace.part1.");
		return;
	  }
	  //execution freq
	  int count = 0;
	  for(int i = 0; i < rMethods.testMethodList.size(); i++){
		String methodname = rMethods.testMethodList.get(i).methodName;
		count = count + rMethods.testMethodFreq.get(i);
		System.out.println("final static int " + methodname + "_pc = " + count + ";");
	  }
	  System.out.println("final static int total_pc = " + count + ";");
//	  System.out.println("Random rand = new Random();");
	  if(!copyFile("src/main/java/TracePart/trace.part2")){
		System.out.println("Error: copying trace.part1.");
		return;
	  }
	  
	  Method m = rMethods.getMethodByName(rMethods.objectName);
	  if(m == null || m.parameters.isEmpty()){
		System.out.println("final " + rMethods.objectName + " object = new " + rMethods.objectName + "();");
	  }
	  else{
		String str = "final " + rMethods.objectName + " object = new " + rMethods.objectName + "(";
		for(int i = 0; i < m.parameters.size(); i++){
		  if(i != 0)
			str = str + ", ";
		  if(m.type.toString().equals("int")){
			System.out.println(m.type.toString() + " para_" + i + " = rand.nextInt();");
		  }
		  else if(m.type.toString().equals("double")){
			System.out.println(m.type.toString() + " para_" + i + " = rand.nextDouble();");
		  }
		  else if(m.type.toString().equals("float")){
			System.out.println(m.type.toString() + " para_" + i + " = rand.nextFloat();");
		  }
		  else if(m.type.toString().equals("long")){
			System.out.println(m.type.toString() + " para_" + i + " = rand.nextLong();");
		  }
		  else if(m.type.toString().equals("boolean")){
			System.out.println(m.type.toString() + " para_" + i + " = rand.nextBoolean();");
		  }
		  str = str + "para_" + i;
		}
		str = str + ");";
		System.out.println(str);

	  }
	  if(!copyFile("src/main/java/TracePart/trace.part3")){
		System.out.println("Error: copying trace.part3.");
		return;
	  }
	  System.out.println("	int sel = rand.nextInt(total_pc);");

	  count = 0;
	  System.out.println("	if(0 <= sel && sel < " + rMethods.testMethodList.get(0).methodName + "_pc){");
	  printMethodRand(rMethods.testMethodList.get(0));
	  System.out.println("}");
	  for(int i = 1; i < rMethods.testMethodList.size(); i++){
		String prename = rMethods.testMethodList.get(i-1).methodName;
		String methodname = rMethods.testMethodList.get(i).methodName;
		count = count + rMethods.testMethodFreq.get(i);
		System.out.println("if(" + prename + "_pc <= sel && sel <" + methodname + "_pc){");
		printMethodRand(rMethods.testMethodList.get(i));
		System.out.println("}");
	  }
	  if(!copyFile("src/main/java/TracePart/trace.part4")){
		System.out.println("Error: copying trace.part4.");
		return;
	  }
	  System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
	}catch(FileNotFoundException e){
	  System.out.println(e);
	}
  }
}
