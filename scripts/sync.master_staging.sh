#!/bin/sh

git up && git co staging && git up && git merge master && git up && git pu && git co master && git merge staging
