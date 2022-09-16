#!/bin/bash

THN=4 # thread number
TLN=100 # trace length
CTR=10 # test trace number

i=0
javac -cp ".:lib/*" -d bin `find src -name '*.java' `
java -cp ".:bin/:lib/*"  AutoGenerator.GenerateVerifyDir

cd verify
rm result
javac $1.java
javac $2.java
javac GenerateHistory.java
javac VeriLin.java
count=0
while [ $i -le $CTR ]
do
  java GenerateHistory $THN $TLN > history
  res=`java -Xss1024m -Xmx20g VeriLin history failed_history $THN`
  echo "test$i:" >> result
  echo $res >> result

  h=`echo $res | head -n 1 | awk '{print $2}'`
  if [ "$h" = "Failed." ]
  then
	if [ -f failed_history ] 
	then
	  `mv failed_history failed_history$count`
	fi
	count=$((count+1))
  fi
  echo "Finished checking $i"
  i=$((i+1))
done
echo "test number = $CTR, failed number = $count" >> result 
cat result
cd ..


