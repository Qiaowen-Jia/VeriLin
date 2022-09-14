dirname="MTCDU-${1}-${2}-${3}-${4}"
errname="reg-${1}-${2}-${3}-${4}"
cd $dirname
./regcnt.sh 1> $errname 2> /dev/null
cat $errname
cd ..
