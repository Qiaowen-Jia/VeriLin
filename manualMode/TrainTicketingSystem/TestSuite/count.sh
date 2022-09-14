ET=`grep "Fail" elin* |wc | awk '{print $1}'`
CNT=`ls elin* |wc |awk '{print $1}'`

function etime() {
  cnt=`sed -e 's/\([smhd]\)/\1 /g' $1` 
  echo $cnt | awk -F' ' '{ 
		b=0; 
		for(i=1; i<=NF; ++i) { 
			s=substr($i,1,length($i)-1); 
			u=substr($i,length($i)); 
			if (u=="s") {
				b+=s; 
			} else if (u=="m") { 
				b+=s*60; 
			} else if (u=="h") { 
				b+=s*60*60; 
			} else if (u=="d") { 
				b+=s*60*60*24; 
			}
		} 
	  print b;}'
}
tdual=0
for f in `ls elin*`
do
	cnt=0
	dual=`cat $f | grep "Total" | tail -n 1 | awk '{print $9}'`
	cnt=`echo $dual |etime`
	tdual=$((tdual+cnt))
done

DUAL=`echo $tdual | awk '{t=split("60 s 60 m 24 h 999 d",a);for(n=1;n<t;n+=2){if($1==0)break;s=$1%a[n]a[n+1]s;$1=int($1/a[n])}print s}'`
if [ ! $DUAL ]; then
	DUAL="0s"
fi


function ec() {
	for f in `ls elin*`
	do
		grep "Fail" $f | wc | awk '{print $1}'
	done
}

EC=`ec | awk '{if($1 != 0) print $1}' | wc | awk '{print $1}'`

echo "Completed: $CNT ; Non-lin: $ET ; Buggy Imp: $EC ; Time: $DUAL "


