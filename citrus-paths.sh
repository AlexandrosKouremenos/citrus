#!/bin/bash

CITRUS_PATH=$(pwd | grep -o '.*citrus' || exit)

AURANTIUM_PATH="${CITRUS_PATH}"/aurantium

SUNKI_PATH="${CITRUS_PATH}"/sunki

POMELO_PATH="${CITRUS_PATH}"/pomelo

# You can change the version or download location of Kafka here.
# Also, if you have a custom Kafka build, you can change the path here.
KAFKA_PATH="${CITRUS_PATH}"/kafka_2.13-3.4.0

if [ ! -d "${KAFKA_PATH}" ]; then

    echo "*** Kafka not found. Downloading... ***"
    wget 'https://archive.apache.org/dist/kafka/3.4.0/kafka_2.13-3.4.0.tgz'
    tar -xzf kafka_2.13-3.4.0.tgz -C "${CITRUS_PATH}"
    rm kafka_2.13-3.4.0.tgz

fi

# You can change the version or download location of Strimzi here.
# Also, if you have a custom Strimzi build, you can change the path here.
STRIMZI_PATH="${CITRUS_PATH}"/strimzi-0.34.0

if [ ! -d "${STRIMZI_PATH}" ]; then

    echo "*** Strimzi not found. Downloading... ***"
    wget 'https://github.com/strimzi/strimzi-kafka-operator/releases/download/0.34.0/strimzi-0.34.0.tar.gz'
    tar -xzf strimzi-0.34.0.tar.gz -C "${CITRUS_PATH}"
    rm strimzi-0.34.0.tar.gz

fi