#!/bin/sh
##
## gorand.sh
## 
## Made by 
## Login   <mueller@localhost>
## 
## Started on  Thu Dec 21 13:41:53 2006 
## Last update Thu Dec 21 15:11:48 2006 
##
## Requires:
## - R
## - sendmail
## - uuencode
## - biabam


OUTPUT=../results/gorand
echo "source(\"gorand.R\")" | R --vanilla --quiet > $OUTPUT/R.log 2>&1

## create tar file
FILE=../results/gorand.tar.gz
tar czf $FILE  $OUTPUT

## send as email
MAIL_FROM="andre.mueller@uniklinik-ulm.de"
MAIL_TO="jumpingkiwi@gmail.com"
TS=$( date )
SUBJECT="VennMaster/gorand results - $TS"
MESSAGE="simulation results $TS"
echo -e "\n$MESSAGE\n\n" | biabam $FILE $MAIL_TO
