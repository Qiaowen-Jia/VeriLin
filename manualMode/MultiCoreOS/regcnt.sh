EFI=0
EFA=0
ETT=0
for f in `ls elin*`
do
	cnt=`grep "Finished" $f |sed -n '1p' | wc | awk '{print $1}'`
	EFI=$((EFI+cnt))
	cnt=`grep "Fail" $f |sed -n '1p' | wc | awk '{print $1}'`
	EFA=$((EFA+cnt))
	cnt=`grep "time out" $f |sed -n '1p' | wc | awk '{print $1}'`
	ETT=$((ETT+cnt))
done
TOT=$((EFI+EFA+ETT))

RG=0
MRG=0
FFN=0
for f in `ls elin*`
do
	FN=`grep "history size =" $f |sed -n '1p' |wc |awk '{print $1}'`
	if [ $FN -ne 0 ]; then
		FFR=`grep "history size =" $f | sed -n '1p' |awk '{print $8}'`
		FFM=`grep "history size =" $f | sed -n '1p' |awk '{print $12}'`
		RG=$((RG+FFR))
		MRG=$((MRG+FFM))
		FFN=$((FFN+1))
	fi
done

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
	dual=`cat $f | grep "DUAL" | tail -n 1 | awk '{print $2}'`
	cnt=`echo $dual |etime`
	tdual=$((tdual+cnt))
done

DUAL=`echo $tdual | awk '{t=split("60 s 60 m 24 h 999 d",a);for(n=1;n<t;n+=2){if($1==0)break;s=$1%a[n]a[n+1]s;$1=int($1/a[n])}print s}'`

if [ ! $DUAL ]; then
	DUAL="0s"
fi

REGNUM="-"
MAXREG="-"
if [ $FFN -gt 0 ]; then
	REGNUM=$((RG/FFN))
	MAXREG=$((MRG/FFN))
fi

echo "Completed: $TOT ; RegionNum: $REGNUM ; MaxRegion: $MAXREG ; Solved: $FFN ; Time: $DUAL "

