#!/bin/sh
bash scripts/sync.master_production.sh
bash scripts/build.prod.sh account
bash scripts/build.prod.sh admin
bash scripts/build.prod.sh bot
bash scripts/build.prod.sh postman
bash scripts/build.prod.sh agent