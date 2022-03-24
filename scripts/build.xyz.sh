#!/bin/bash

input=$1
echo "input  $input"

if [[ $input == "-a" ]]
then
	git up && git co xyz && git up && git merge master && git up && git pu && git co master && git merge xyz
	for service in 'account' 'admin' 'agent' 'bot' 'postman' 'xms'
	do
		git co xyz && git up && git pu && git co "build-xyz-${service}" && git up && git pu && git merge xyz && git pu && git co xyz
	done
elif [[ $input == "-s" ]]
then
	git up && git co xyz && git up && git merge master && git up && git pu && git co master && git merge xyz
else	
	git co xyz && git up && git pu && git co "build-xyz-${input}" && git up && git pu && git merge xyz && git pu
	git co xyz && git merge "build-xyz-${input}" && git pu
fi	
