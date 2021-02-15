#!/bin/sh

#source config/setvars.sh

#bash setup.sh

#mvn clean

# short command
mvn clean package -pl $1 -am -DskipTests

java -jar $1/target/$1-0.0.1-SNAPSHOT.jar $2 $3 $4 $5 $6
