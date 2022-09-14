#!/bin/bash
# repeat thread tracenum msec 
#RPT=10

RPT=${1}
j=1
TO=30
count=0
ttdd=0
td3=0
while [ ${j} -le $RPT ]
do
	timeout 10m java -jar GenerateHistory.jar  ${2} ${3} 1 ${4} 0 0 0 1> trace${j} 2>/dev/null
	if [ $? -ne 0 ]; then
		continue;
	fi
	echo "Generate History of time ${j} finished"
	pref=0
	postf=0
	tstart3=`date "+%s%3N"`

	tth=`date "+%s%3N"`
	java -Xss1024m -Xmx20g -jar  VeriLin.jar ${2} trace${j} failed_trace0_${j} 1 > tmp_resd0_${j} &
	prepid=$!
	java -Xss1024m -Xmx20g -jar  VeriLin.jar ${2} trace${j} failed_trace1_${j} 0 > tmp_resd1_${j} &
	postpid=$!
	ttd=0
	while ( [ $ttd -lt $TO ] && [ $pref -eq 0 ] && [ $postf -eq 0 ] )
	do
	if [ -f tmp_resd0_${j} ]; then
		preres=`tail -n 1 tmp_resd0_${j}` 2> /dev/null
		if [ "$preres" = "VeriLin" ]; then
			pref=1
		fi
	fi
	if [ -f tmp_resd1_${j} ]; then
		postres=`tail -n 1 tmp_resd1_${j}` 2> /dev/null
		if [ "$postres" = "VeriLin" ]; then
			postf=1
		fi
	fi
		sleep 0.01
		tte=`date "+%s%3N"`
		ttd=$(((tte-tth)/60000))
	done
	tend3=`date "+%s%3N"`
	tdur3=$((tend3-tstart3))
	td3=$((tdur3+td3))
	if [ $pref -eq 0 ] && [ $postf -eq 0 ]; then
		ttdd=$((ttdd+1))
	  echo "Dualtime test is time out of time ${j} in $tdur3 for ${2} ${3} ${4} " 
	  echo "Dualtime test is time out of time ${j} in $tdur3 for ${2} ${3} ${4} " > elin${j} 
		kill $prepid 2> /dev/null
		kill $postpid 2> /dev/null
	elif [ $pref -eq 1 ]; then
		kill $postpid 2>/dev/null
		echo "Dual History of time ${j} Pretime finished in $tdur3 for ${2} ${3} ${4} " > elin${j} 
		cat tmp_resd0_${j} >> elin${j}
		rm tmp_resd0_${j}
	elif [ $postf -eq 1 ]; then
		kill $prepid 2>/dev/null
		echo "Dual History of time ${j} Posttime finished in $tdur3 for ${2} ${3} ${4} " > elin${j} 
		cat tmp_resd1_${j} >> elin${j}
		rm tmp_resd1_${j}
	fi

	durp3=`echo $((tdur3/1000)) | awk '{t=split("60 s 60 m 24 h 999 d",a);for(n=1;n<t;n+=2){if($1==0)break;s=$1%a[n]a[n+1]s;$1=int($1/a[n])}print s}'`

	if [ ! $durp3 ]; then
		durp3="0s"
	fi
	echo "DUAL $durp3 " >> elin${j}

	j=$((j+1))
	
done


dur3=`echo $((td3/1000)) | awk '{t=split("60 s 60 m 24 h 999 d",a);for(n=1;n<t;n+=2){if($1==0)break;s=$1%a[n]a[n+1]s;$1=int($1/a[n])}print s}'`

if [ ! $dur3 ]; then
	dur3="0s"
fi

echo "Total: $RPT ; Dual Timeout: $ttdd ; Time: $dur3 "
