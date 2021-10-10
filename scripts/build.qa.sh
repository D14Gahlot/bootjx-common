#!/bin/sh

git co staging && git up && git pu && git co "build-qa-${1}" && git up && git pu && git merge staging && git pu && git co staging
