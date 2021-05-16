#!/bin/sh

git co production && git up && git merge master && git up && git pu && git co master && git merge production
