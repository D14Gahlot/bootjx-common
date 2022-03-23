#!/bin/bash

input=$1
echo "input  $input"

if [[ $input == "-a" ]]
then
	git up && git co staging && git up && git merge master && git up && git pu && git co master && git merge staging
	for service in 'account' 'admin' 'agent' 'bot' 'postman' 'xms'
	do
		git co staging && git up && git pu && git co "build-xyz-${service}" && git up && git pu && git merge staging && git pu && git co staging
	done
elif [[ $input == "-s" ]]
then
	git up && git co staging && git up && git merge master && git up && git pu && git co master && git merge staging
else	
	git co staging && git up && git pu && git co "build-xyz-${input}" && git up && git pu && git merge staging && git pu
	git co staging && git merge "build-xyz-${input}" && git pu
fi	
