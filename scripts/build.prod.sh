#!/bin/sh

git co production
#echo 'branch.service.domain=mehery.com' > lib-common/lib-common-config/src/main/resources/application.branch.properties
#git add lib-common/lib-common-config/src/main/resources/application.branch.properties
#git commit -m "File Upated"

git up && git pu

git co production && git up && git pu && git co "build-prod-${1}" && git up && git pu && git merge production && git pu && git co production
