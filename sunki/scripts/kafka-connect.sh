#!/bin/bash

# Before running this script, make sure that:
# 1. Mosquitto is up and running.
#   Runs with mosquitto -c mosquitto.conf
# 2. Zookeeper is up and running. (See pomelo/scripts/start-zookeeper.sh)
#   Runs with kafka_2.13-3.4.0/bin/zookeeper-server-start.sh config/zookeeper.properties
# 3. Kafka Broker is up and running. (See pomelo/scripts/start-kafka-broker.sh)
#   Runs with kafka_2.13-3.4.0/bin/kafka-server-start.sh config/server.properties

project_dir=$(pwd | grep -o '.*citrus')

if [ -z "${project_dir}" ]; then
    echo "Citrus directory not found."
    exit 1
fi

source "${project_dir}"/citrus-paths.sh

KAFKA_CONNECT_CONFIGS="${SUNKI_PATH}"/scripts/kafka-configs

# Use the following command to clear the Kafka logs. This is useful when you want to start fresh.
#trap "rm -rf /tmp/kafka-logs /tmp/zookeeper /tmp/kraft-combined-logs" EXIT

cd "${KAFKA_PATH}" || exit

sed -i "s|plugin.path=.*|plugin.path=${SUNKI_PATH}/target/sunki-0.0.1-SNAPSHOT-jar-with-dependencies.jar|" "${KAFKA_CONNECT_CONFIGS}"/connect-standalone.properties

bin/connect-standalone.sh "${KAFKA_CONNECT_CONFIGS}"/connect-standalone.properties \
                          "${KAFKA_CONNECT_CONFIGS}"/connect-mqtt-source.properties
