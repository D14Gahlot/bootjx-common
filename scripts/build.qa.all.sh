#!/bin/sh
bash scripts/sync.master_production.sh
bash scripts/build.xyz.sh account
bash scripts/build.xyz.sh admin
bash scripts/build.xyz.sh bot
bash scripts/build.xyz.sh postman
bash scripts/build.xyz.sh agent
bash scripts/build.xyz.sh xms
