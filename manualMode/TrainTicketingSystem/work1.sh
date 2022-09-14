dirname="DDTSE-${1}-${2}-${3}-${4}"
resname="res-${1}-${2}-${3}-${4}"
mkdir $dirname
cp -rf TestSuite $dirname
cp -rf TicketingSystem $dirname
cd $dirname/TestSuite/
./workdd.sh $1 $2 $3 $4 > $resname &
cd ../..
