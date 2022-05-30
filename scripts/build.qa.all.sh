#!/bin/sh
bash scripts/sync.master_production.sh
bash scripts/build.qa.sh account
bash scripts/build.qa.sh admin
bash scripts/build.qa.sh bot
bash scripts/build.qa.sh postman
bash scripts/build.qa.sh agent
bash scripts/build.qa.sh xms
