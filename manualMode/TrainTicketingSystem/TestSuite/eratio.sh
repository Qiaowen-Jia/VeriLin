function er() {
for f in `ls elin*`
do
	grep -w "PPR" $f | awk '{print $2}'
done
}

ER0=` er | awk '{if($1 == 0) print $1}' | wc | awk '{print $1}'`
ER1=` er | awk '{if($1 == 1) print $1}' | wc | awk '{print $1}'`
ER2=` er | awk '{if($1 == 2) print $1}' | wc | awk '{print $1}'`
ERm1=` er | awk '{if($1 == -1) print $1}' | wc | awk '{print $1}'`
ERm2=` er | awk '{if($1 == -2) print $1}' | wc | awk '{print $1}'`
TOT=$((ERm2+ERm1+ER0+ER1+ER2))

EPRE=0
EPOST=0
EDUAL=0
for f in `ls elin*`
do
	CNT=`grep -w "PPR" $f | awk '{print $6}'`
	EPRE=$((EPRE+CNT))
	CNT=`grep -w "PPR" $f | awk '{print $7}'`
	EPOST=$((EPOST+CNT))
	CNT=`grep -w "PPR" $f | awk '{print $8}'`
	EDUAL=$((EDUAL+CNT))
done

echo "Completed: $TOT ; Ratio: $ERm2 $ERm1 $ER0 $ER1 $ER2 ; Timeout: $EPRE $EPOST $EDUAL "
