dirname="DDTSE-${1}-${2}-${3}-${4}"
errname="ratio-${1}-${2}-${3}-${4}"
cd $dirname/TestSuite/
./eratio.sh > $errname
cat $errname
cd ../..
