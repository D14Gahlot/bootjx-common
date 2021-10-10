#!/bin/sh

git co production && git up && git pu && git co "build-prod-${1}" && git up && git pu && git merge production && git pu && git co production
