#!/bin/sh

git co production && git up && git co staging && git up && git merge production && git pu

git co production && git up && git merge staging && git up && git pu && git co staging && git merge production
