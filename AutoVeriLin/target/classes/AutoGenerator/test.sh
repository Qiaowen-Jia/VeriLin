#!/bin/sh

CTR=100

i=0
`mvn compile`
`mvn exec:java -Dexec.mainClass="AutoGenerator.GenerateVerifyDir" `
`javac Trace.java`
`javac VeriLin.java`
count=0
while [ "$i -le $CTR" ] do
  java Trace > trace
  res=`java VeriLin`
  echo "test$i:" >> result
  echo $res >> result
  h=`head -1 $res`
  if [ $h == "Verification Finished" ]
	count=$count+1
  fi
  echo "test number = $CTR, correct number = $h" >> result 
  i=$i+1
done


