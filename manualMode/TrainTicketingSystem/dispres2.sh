dirname="DUTSE-${1}-${2}-${3}-${4}"
errname="p12-cnt-${1}-${2}-${3}-${4}"
cd $dirname/TestSuite/
./count.sh > $errname
cat $errname
cd ../..
