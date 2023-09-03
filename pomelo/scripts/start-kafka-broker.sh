#!/bin/bash

# Use the following command to clear the Kafka logs. This is useful when you want to start fresh.
#trap "rm -rf /tmp/kafka-logs /tmp/zookeeper /tmp/kraft-combined-logs" EXIT

project_dir=$(pwd | grep -o '.*citrus')

if [ -z "${project_dir}" ]; then
    echo "Citrus directory not found."
    exit 1
fi

source "${project_dir}"/citrus-paths.sh

cd "${KAFKA_PATH}" || exit

# Use the following command to start a Kafka broker.
bin/kafka-server-start.sh config/server.properties

# Use the following command to create a Kafka topic.
#bin/kafka-topics.sh --create --topic machine.0 --bootstrap-server localhost:9092
