#!/bin/bash

directory="../logs"


cd ./application/
pwd
export CLASSPATH=$CLASSPATH:../../../../../junit3.8/junit.jar:../../Aufgabe_2_j
if [ -d $directory ]; then
	echo ""
else
	echo "Log Directory does not exist - create it ..."
	mkdir $directory
	echo "Go into log dir"
	cd $directory	
	echo "Create compileOutput.log file"
	echo "" > compileOutput.log
	echo "Go back to application Dir"
	cd ../application
fi

ant compile -logfile ../logs/compileOutput.log

# javac -encoding ISO-8859-1 testcases/*.java

cd ./build___/classes
pwd
java junit.swingui.TestRunner testcases.Test___Suite
