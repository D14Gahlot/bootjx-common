#!/bin/sh

#source config/setvars.sh
source ~/.bash_profile

server=${1:-server-test}
#bash setup.sh

#mvn clean

# short command
mvn clean install test -pl $server -am -U

#java -jar $1/target/$1-0.0.1-SNAPSHOT.jar $2 $3 $4 $5 $6
