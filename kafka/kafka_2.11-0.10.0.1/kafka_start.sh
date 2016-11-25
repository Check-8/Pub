#!/bin/bash

/opt/bin/kafka-server-start.sh -daemon /opt/config/server.properties
/opt/bin/kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 2 --partition 10 --topic TAB_COMMAND
/opt/bin/kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 2 --partition 10 --topic TAB_EVENT
tail -f /opt/logs/server.log