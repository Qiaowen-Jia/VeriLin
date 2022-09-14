timer_start=`date "+%Y-%m-%d %H:%M:%S"`
#./clear.sh
rm elin* 2> /dev/null
rm trace* 2> /dev/null
rm fail* 2> /dev/null
rm tmp_res* 2> /dev/null

./compile.sh
#repeatNum threadNum traceNum msec
./ddwork.sh $1 $2 $3 $4

timer_end=`date "+%Y-%m-%d %H:%M:%S"`
duration=`echo $(($(date +%s -d "${timer_end}") - $(date +%s -d "${timer_start}"))) | awk '{t=split("60 s 60 m 24 h 999 d",a);for(n=1;n<t;n+=2){if($1==0)break;s=$1%a[n]a[n+1]s;$1=int($1/a[n])}print s}'`
echo "Time Eslaped: $duration"

