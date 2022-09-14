/****************************************************************
-- File Name: GenerateVerifyDir.java
-- Author: Qiaowen Jia
-- mail: jiaqw@ios.ac.cn
-- Created Time: Tue Mar  9 09:52:51 2021
****************************************************************************/
package AutoGenerator;
import java.util.*;
import java.io.*;

public class GenerateVerifyDir{
  public static boolean copyFile(String source){
	try{
	  File f = new File(source);
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
  public static boolean cpFile(String source, String target){
	try{
	  FileOutputStream f = new FileOutputStream(target);
	  System.setOut(new PrintStream(f));
	  copyFile(source);
	  System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
	  return true;
	}catch(FileNotFoundException e){
	  System.out.println(e);
	  return false;
	} 
  }
  public static boolean cpPackageFile(String source, String target){
	try{
	  FileOutputStream f = new FileOutputStream(target);
	  System.setOut(new PrintStream(f));
	  System.out.println("package verify;");
	  copyFile(source);
	  System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
	  return true;
	}catch(FileNotFoundException e){
	  System.out.println(e);
	  return false;
	} 
  }

  public static void main(String[] args){
	ConfigParser cp = new ConfigParser();
	cp.parseConfig("config");
	RMethods rMethods = cp.rMethods;
	File dir = new File("verify");
	dir.mkdir();
	String fileName = rMethods.objectName + "." + rMethods.fileSuffix;
	String specName = rMethods.specObject + "." + rMethods.fileSuffix;
	String specNamePath = "";
	if(rMethods.hasSpec){
	  if(rMethods.preDefined){
		String[] sp = rMethods.specObject.split("_");
		String type = sp[0];
		specNamePath = "src/main/java/PreDefined/" + type + "/" + specName;
	  }
	  else
		specNamePath = specName;
	}
	cpFile(fileName, "verify/" + fileName);
	cpFile(specNamePath, "verify/" + specName);
	HistoryGenerator gt = new HistoryGenerator();
	gt.main(null);
	GenerateVeriLin gvl = new GenerateVeriLin();
	gvl.main(null);
	cpFile("src/main/java/verify/README.md", "verify/README.md");
  }

}

