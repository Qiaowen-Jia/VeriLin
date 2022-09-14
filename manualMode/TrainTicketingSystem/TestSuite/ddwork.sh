#!/bin/bash
#repeatNum threadNum traceNum msec

CNT=103
i=1
while [ "$i" -le $CNT ]
do
	if [ "$i" -lt 10 ]	
	then
		stu=00$i
	else if [ "$i" -lt 100 ]	
		then
			stu=0$i
		else
			stu=$i
		fi
	fi
	 ./verilinddd.sh $stu $1 $2 $3 0 $4 0 1> elin$stu 2> /dev/null
	i=$((i+1))
done
		
