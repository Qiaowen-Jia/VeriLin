#! /bin/sh

i=1
succ=0
fail=0
`javac VeriRegion.java`
`javac Trace.java`
`rm trace*`
while [ "$i" -le $1]
do
  `java Trace > trace`
  result=`java -Xss1500m -Xms200g -Xmx200g VeriRegion`
  result1=`tail -n 2 $result`
  result2=`tail -n 1 $result`
  if [ "$result1" == 'Verification Failed']
  then
	file="trace"+"$i"
	`cat trace` >> file
	fail=$fail+1
	echo "Failed $i: $result2" >> failedCase
  else
	succ=$succ+1
  fi
done
echo "Whole Test: $1, Success case: $succ, Failed case: $fail"

