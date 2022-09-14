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
  java GenerateHistory $THN $TLN > trace
  res=`java -Xss1024m -Xmx400g VeriLin trace writetrace $THN`
  echo "test$i:" >> result
  echo $res >> result

  h=`echo $res | head -n 1`
  if [ "$h" = "Verification Failed." ]
  then
	if [ -f writetrace ] 
	then
	  `mv writetrace writetrace$count`
	fi
	count=$((count+1))
  fi
  echo "Finished checking $i"
  i=$((i+1))
done
echo "test number = $CTR, failed number = $count" >> result 
cat result
cd ..


