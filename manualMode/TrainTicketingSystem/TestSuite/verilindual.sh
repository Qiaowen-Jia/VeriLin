#!/bin/bash
# user repeat thread tracenum  sequential msec nsec
#RPT=10
RPT=$2
cp ./ticketingsystem/GenerateHistory.java ../TicketingSystem/$1/myproject/ticketingsystem/GenerateHistory.java
cd ../TicketingSystem/$1/myproject
javac -encoding UTF-8 -cp . ticketingsystem/GenerateHistory.java 2>/dev/null
cd ../../../TestSuite

j=1
TO=30
count=0
ttpre=0
ttpost=0
ttdd=0
ratio=0
td3=0
while [ ${j} -le $RPT ]
 do
	cd ../TicketingSystem/$1/myproject
	timeout 30m java -cp . ticketingsystem/GenerateHistory $3 $4 $5 $6 $7 1> trace$1 2> /dev/null
  if [ $? -ne 0 ]; then
	  continue;
  fi

	echo "Generate History $1 of time ${j} finished"
	cp trace$1 ../../../TestSuite/trace$1
	cd ../../../TestSuite

	pref=0
	postf=0

	tstart3=`date "+%s%3N"`

	tth=`date "+%s%3N"`
	java -Xss1024m -Xmx20g -cp . ticketingsystem/VeriLin $3 trace$1 0 failedHistory0_$1_${j} > tmp_resd0_$1_${j} &
	prepid=$!
	java -Xss1024m -Xmx20g -cp . ticketingsystem/VeriLin $3 trace$1 1 failedHistory1_$1_${j} > tmp_resd1_$1_${j} &
	postpid=$!
	ttd=0
	while ( [ $ttd -lt $TO ] && [ $pref -eq 0 ] && [ $postf -eq 0 ] )
	do
	if [ -f tmp_resd0_$1_${j} ]; then
		preres=`tail -n 1 tmp_resd0_$1_${j}` 2> /dev/null
		if [ "$preres" = "VeriLin" ]; then
			pref=1
		fi
	fi
	if [ -f tmp_resd1_$1_${j} ]; then
		postres=`tail -n 1 tmp_resd1_$1_${j}` 2> /dev/null
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
		echo "Dualtime test is time out in time ${j} of $1 for $3 $4 $6 "
#cat tmp_resd0_$1_${j}
		kill $prepid 2> /dev/null
		kill $postpid 2> /dev/null
	elif [ $pref -eq 1 ]; then
		kill $postpid 2> /dev/null
		echo "Dual History $1 of time ${j} Pretime finished in $tdur3 for $3 $4 $6 "
		cat tmp_resd0_$1_${j}
		rm tmp_resd0_$1_${j}
	elif [ $postf -eq 1 ]; then
		kill $prepid 2> /dev/null
		echo "Dual History $1 of time ${j} Posttime finished in $tdur3 for $3 $4 $6 "
		cat tmp_resd1_$1_${j}
		rm tmp_resd1_$1_${j}
	fi

	durp3=`echo $((tdur3/1000)) | awk '{t=split("60 s 60 m 24 h 999 d",a);for(n=1;n<t;n+=2){if($1==0)break;s=$1%a[n]a[n+1]s;$1=int($1/a[n])}print s}'`

	if [ ! $durp3 ]; then
		durp3="0s"
	fi
	echo "DUAL $durp3 in $1 of time ${j} for ${3} ${4} ${6} " 

	j=$((j+1))
	
done

dur3=`echo $((td3/1000)) | awk '{t=split("60 s 60 m 24 h 999 d",a);for(n=1;n<t;n+=2){if($1==0)break;s=$1%a[n]a[n+1]s;$1=int($1/a[n])}print s}'`

if [ ! $dur3 ]; then
	dur3="0s"
fi

echo "Total: $RPT ; Dual timeout: $ttdd ;  Time: $dur3 "

