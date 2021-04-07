/****************************************************************
-- File Name: GenerateSh.java
-- Author: Qiaowen Jia
-- mail: jiaqw@ios.ac.cn
-- Created Time: Wed Feb 24 06:22:38 2021
****************************************************************************/
import java.util.*;
import java.io.*;

public class GenerateSh{

  public static void main(String[] args){
	//the argument test trace number
	  if(args.length != 1){
		System.out.println("The argument is incorrect in Generating Bash File");
		return;
	  }
	  try{
		FileOutputStream f = new FileOutputStream("executeBash.sh");
		System.setOut(new PrintStream(f));
		System.out.println("#! /bin/bash");
		System.out.println("CNT=" + args[1]);
		System.out.println("i=0");
		System.out.println("javac src/main/java/AutoGenerator/GenerateTrace.java");
		System.out.println("javac src/main/java/AutoGenerator/GenerateVeriRegion.java");
		System.out.println("while [ \"$i\" -le $CNT ]");
		System.out.println("do");
		System.out.println("	java Trace > trace ");
		System.out.println("	result=java -Xss1500m -Xms200g -Xmx200g VeriRegion");
		System.out.println("	result1=head -n 1 result");
		System.out.println("	result2=tail -n 2 result");
		System.out.println("	substr=${result2:0:21}");
		System.out.println("	if [ \"$substr\" == 'Verification Failed']");
		System.out.println("	then");
	
		System.out.println("	");
		System.out.println("done");
		System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
	  }catch(FileNotFoundException e){
		System.out.println(e);
	  }
  }
}
