#!/bin/sh

#source config/setvars.sh
source ~/.bash_profile

ROOT_FOLDER="$(pwd)"


echo "======ROOT_FOLDER:${ROOT_FOLDER}"

#bash setup.sh

#mvn clean

# short command
mvn clean package -pl $1 -am -DskipTests -U

java -Djavax.net.ssl.trustStore=${ROOT_FOLDER}/certs/cacerts \
	-jar $1/target/$1-0.0.1-SNAPSHOT.jar $2 $3 $4 $5 $6  --spring.config.additional-location=${ROOT_FOLDER}/config/
