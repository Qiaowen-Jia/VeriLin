#!/bin/bash

rm ./ticketingsystem/*.class
rm elin*
rm trace*
rm fail*
rm tmp_res*

cd ../TicketingSystem

CNT=103
i=1
while [ "$i" -le $CNT ]
do
	if [ "$i" -lt 10 ]	
	then
		stu=00$i
	else if [ "$i" -lt 100 ]	
		then
			stu=0$i
		else
			stu=$i
		fi
	fi
	rm ../TicketingSystem/$stu/myproject/trace*
	rm ../TicketingSystem/$stu/myproject/ticketingsystem/*.class
	rm ../TicketingSystem/$stu/myproject/ticketingsystem/*/*.class
	rm ../TicketingSystem/$stu/myproject/ticketingsystem/*/*/*.class
	i=$((i+1))
done
		
