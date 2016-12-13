#!/bin/bash
if [ -z "${1+x}" ]; then
	altfile="";
else
	altfile=-$1;
fi
docker-compose -f docker-compose$altfile.yml -f docker-compose-kafka.yml up -d
docker-compose -f docker-compose$altfile.yml -f docker-compose-kafka.yml scale kafka=2