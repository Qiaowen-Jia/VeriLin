#!/bin/bash

THN=4 # thread number
TLN=1000 # trace length
CTR=100 # test trace number

i=0
#`mvn compile`
mvn exec:java -Dexec.mainClass="AutoGenerator.GenerateVerifyDir" 
cd verify
javac Trace.java
javac VeriLin.java
count=0
while ((i < CTR))
do
  java Trace $THN $TLN > trace
  h=`java VeriLin trace writetrace $THN | head -1`
  echo "test$i:" >> result
#  echo $res >> result
#  h=`head -1 $res`
  if [ "$h != Verification Finished." ]
  then
	if [ -f writetrace ] 
	then
	  `mv writetrace writetrace$count`
	fi
	((count++))
  fi
  ((i++))
  echo $i
done
echo "test number = $CTR, false number = $count" >> result 
cd ..


