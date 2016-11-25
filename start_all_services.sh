#!bin/bash

docker-compose -f docker-compose.yml -f docker-compose-kafka.yml up -d
docker-compose -f docker-compose.yml -f docker-compose-kafka.yml scale kafka=2