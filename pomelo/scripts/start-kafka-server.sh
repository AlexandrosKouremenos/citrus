#!/bin/bash

trap "rm -rf /tmp/kafka-logs /tmp/zookeeper /tmp/kraft-combined-logs" EXIT

cd ~/Utilities/kafka-3.4.0-src/ || exit

KAFKA_CLUSTER_ID="$(bin/kafka-storage.sh random-uuid)"

bin/kafka-storage.sh format -t "$KAFKA_CLUSTER_ID" -c config/kraft/server.properties

bin/kafka-server-start.sh config/kraft/server.properties

#bin/kafka-topics.sh --create --topic machine.0 --bootstrap-server localhost:9092
