#!/bin/bash

input=$1
echo "input  $input"

git co staging

#echo 'branch.service.domain=mehery.io' > lib-common/lib-common-config/src/main/resources/application.branch.properties
#git add lib-common/lib-common-config/src/main/resources/application.branch.properties
#git commit -m "File Upated"

git up && git pu

if [[ $input == "-a" ]]
then
	git up && git co staging && git up && git merge master && git up && git pu && git co master && git merge staging
	for service in 'account' 'admin' 'agent' 'bot' 'postman'
	do
		git co staging && git up && git pu && git co "build-qa-${service}" && git up && git pu && git merge staging && git pu && git co staging
	done
elif [[ $input == "-s" ]]
then
	git up && git co staging && git up && git merge master && git up && git pu && git co master && git merge staging
else	
	git co staging && git up && git pu && git co "build-qa-${input}" && git up && git pu && git merge staging && git pu
	git co staging && git merge "build-qa-${input}" && git pu
fi	
