dirname="MTCDU-${1}-${2}-${3}-${4}"
resname="res-${1}-${2}-${3}-${4}"
mkdir $dirname
cp *.sh $dirname
cp *.jar $dirname
cd $dirname/
rm elin* 2> /dev/null
rm trace* 2> /dev/null
rm fail* 2> /dev/null
rm tmp_res* 2> /dev/null
./verilindu.sh $1 $2 $3 $4 1> $resname 2> /dev/null &
sleep 1
cd ..
