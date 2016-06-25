#!/bin/bash
# Change this to your netid
netid=$2
#
# Root directory of your project
PROJDIR=$(pwd)
#
# This assumes your config file is named "config.txt"
# and is located in your project directory
#
CONFIG=$1
#
# Directory your java classes are in
#
BINDIR=$PROJDIR/bin
#
# Your main project class
#
PROG=Node

#Compile the source files
COMPILE="javac *.java"
$COMPILE

cat $CONFIG | sed -e "s/#.*//" | sed -e "/^\s*$/d" | awk '{ print $1,$2 }' | \
while read i j;
do
if [ "$j" != "$netid" ] && [[ "$j" == *"dc"* ]]; then
ssh $netid@$j.utdallas.edu "cd $PROJDIR;java $PROG $i $CONFIG" &
fi
done