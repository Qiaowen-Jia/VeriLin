#!/bin/bash
# user repeat thread tracenum  sequential msec nsec
#RPT=10
RPT=$2
cp ./ticketingsystem/GenerateHistory.java ../TicketingSystem/$1/myproject/ticketingsystem/GenerateHistory.java
cd ../TicketingSystem/$1/myproject
javac -encoding UTF-8 -cp . ticketingsystem/GenerateHistory.java
cd ../../../TestSuite

j=1
TO=30
precnt=0
postcnt=0
ttpre=0
ttpost=0
ttdd=0
ratio=0
td1=0
td2=0
td3=0
while [ ${j} -le $RPT ]
 do
	cd ../TicketingSystem/$1/myproject
	timeout 10m java -cp . ticketingsystem/GenerateHistory $3 $4 $5 $6 $7 1> trace$1 2> /dev/null
	#echo "Generate History $1 of time ${j} finished"
	cp trace$1 ../../../TestSuite/trace$1
	cd ../../../TestSuite
#cp trace$1 trace_$1_${j}_$3_$4_$6
	pretrue=0
	prefalse=0
	posttrue=0
	postfalse=0
	tstart1=`date "+%s%3N"`
	timeout 30m java -Xss1024m -Xmx20g -cp . ticketingsystem/VeriLin $3 trace$1 0 failedHistory0_$1_${j} > tmp_res0_$1_${j}
	if [ $? -eq 124 ]; then
		tend1=`date "+%s%3N"`
		tdur1=$((tend1-tstart1))
		ttpre=$((ttpre+1))
	  echo "Pretime test is time out in time ${j} of $1 for $3 $4 $6 "
	else
		tend1=`date "+%s%3N"`
		tdur1=$((tend1-tstart1))
	  res0=`cat tmp_res0_$1_${j}`
	  res0_1=`cat tmp_res0_$1_${j} | sed -n '2p'`
	  if [ "$res0_1" = "Verification Finished." ]; then
			pretrue=1
			precnt=$((precnt+1))
	  fi
	  if [ "$res0_1" = "Verification Failed." ]; then
			prefalse=1
	  fi
	  echo "Pretime test in time ${j} of $1 for $3 $4 $6 " >> d0lin${1}
		cat tmp_res0_$1_${j} >> d0lin${1}
		rm tmp_res0_$1_${j}
	fi
	td1=$((tdur1+td1))
	tstart2=`date "+%s%3N"`
	timeout 30m java -Xss1024m -Xmx20g -cp . ticketingsystem/VeriLin $3 trace$1 1 failedHistory1_$1_${j} > tmp_res1_$1_${j}
	if [ $? -eq 124 ]; then
		tend2=`date "+%s%3N"`
		tdur2=$((tend2-tstart2))
		ttpost=$((ttpost+1))
		echo "Posttime test is time out in time ${j} of $1 for $3 $4 $6 "
	else
		tend2=`date "+%s%3N"`
		tdur2=$((tend2-tstart2))
		res1=`cat tmp_res1_$1_${j}`
		res1_1=`cat tmp_res1_$1_${j} | sed -n '2p'`
		if [ "$res1_1" = "Verification Finished." ]; then
			posttrue=1
			postcnt=$((postcnt+1))
		fi
	  if [ "$res1_1" = "Verification Failed." ]; then
			postfalse=1
	  fi
	  echo "Posttime test in time ${j} of $1 for $3 $4 $6 " >> d1lin${1}
		cat tmp_res1_$1_${j} >> d1lin${1}
		rm tmp_res1_$1_${j}
	fi
	if [ $pretrue -eq 1 -a $postfalse -eq 1 ] ||  [ $prefalse -eq 1 -a $posttrue -eq 1 ]; then
		echo "Fatal Error in time ${j} of $1 for $3 $4 $6 "
		echo "$pretrue $posttrue $prefalse $postfalse "
		cat res0
		cat res1
	fi
	td2=$((tdur2+td2))
	if [ $tdur1 -ge $((tdur2*1000)) ] || [ $tdur2 -ge $((tdur1*1000)) ]; then
		echo $res0
		echo "History $1 of time ${j} Pretime finished in $tdur1 for $3 $4 $6 "
		echo $res1
		echo "History $1 of time ${j} Posttime finished in $tdur2 for $3 $4 $6 "
		cp trace$1 trace_$1_${j}_$3_$4_$6
	fi

	if [ $tdur1 -ge $((tdur2*10)) ]; then
		rat=-2
	elif [ $tdur1 -ge $((tdur2*2)) ]; then
		rat=-1
	elif [ $tdur2 -ge $((tdur1*10)) ]; then
		rat=2
	elif [ $tdur2 -ge $((tdur1*2)) ]; then
		rat=1
	else
		rat=0
	fi

	tstart3=`date "+%s%3N"`

	pref=0
	postf=0

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

	durp1=`echo $((tdur1/1000)) | awk '{t=split("60 s 60 m 24 h 999 d",a);for(n=1;n<t;n+=2){if($1==0)break;s=$1%a[n]a[n+1]s;$1=int($1/a[n])}print s}'`
	durp2=`echo $((tdur2/1000)) | awk '{t=split("60 s 60 m 24 h 999 d",a);for(n=1;n<t;n+=2){if($1==0)break;s=$1%a[n]a[n+1]s;$1=int($1/a[n])}print s}'`
	durp3=`echo $((tdur3/1000)) | awk '{t=split("60 s 60 m 24 h 999 d",a);for(n=1;n<t;n+=2){if($1==0)break;s=$1%a[n]a[n+1]s;$1=int($1/a[n])}print s}'`

	if [ ! $durp1 ]; then
		durp1="0s"
	fi
	if [ ! $durp2 ]; then
		durp2="0s"
	fi
	if [ ! $durp3 ]; then
		durp3="0s"
	fi
	echo "PPPR $rat $durp1 $durp2 $durp3 in $1 of time ${j} for ${3} ${4} ${6} " 

	j=$((j+1))
	
done

echo "Total: $((RPT*2)) ; PreFin: $precnt ; PostFin: $postcnt ; Pre timeout: $ttpre ; Post timeout: $ttpost ; Dual timeout: $ttdd "


dur1=`echo $((td1/1000)) | awk '{t=split("60 s 60 m 24 h 999 d",a);for(n=1;n<t;n+=2){if($1==0)break;s=$1%a[n]a[n+1]s;$1=int($1/a[n])}print s}'`
dur2=`echo $((td2/1000)) | awk '{t=split("60 s 60 m 24 h 999 d",a);for(n=1;n<t;n+=2){if($1==0)break;s=$1%a[n]a[n+1]s;$1=int($1/a[n])}print s}'`
dur3=`echo $((td3/1000)) | awk '{t=split("60 s 60 m 24 h 999 d",a);for(n=1;n<t;n+=2){if($1==0)break;s=$1%a[n]a[n+1]s;$1=int($1/a[n])}print s}'`

if [ ! $dur1 ]; then
	dur1="0s"
fi
if [ ! $dur2 ]; then
	dur2="0s"
fi
if [ ! $dur3 ]; then
	dur3="0s"
fi

if [ $td1 -ge $((td2*10)) ]; then
	ratio=-2
elif [ $td1 -ge $((td2*2)) ]; then
	ratio=-1
elif [ $td2 -ge $((td1*10)) ]; then
	ratio=2
elif [ $td2 -ge $((td1*2)) ]; then
	ratio=1
else
	ratio=0
fi

echo "PPR $ratio $dur1 $dur2 $dur3 $ttpre $ttpost $ttdd "
