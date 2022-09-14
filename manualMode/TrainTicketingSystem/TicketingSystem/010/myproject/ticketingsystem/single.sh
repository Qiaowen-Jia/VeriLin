#!/usr/bin/env bash

rm -rf ticketingsystem
echo "--- Removed ticketingsystem ---"
mkdir ticketingsystem
echo "--- mkdir ticketingsystem success ---"
cp *.java ticketingsystem/
echo "--- copy *.java to ticketingsystem success ---"
javac -cp . ticketingsystem/SingleTrace.java
echo "--- Compile  Completed---"
java -cp . ticketingsystem/SingleTrace >& trace
echo "--- Run  Completed---"
java -jar verify.jar
