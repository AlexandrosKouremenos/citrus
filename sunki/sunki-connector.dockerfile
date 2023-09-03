# TODO: Try this https://stackoverflow.com/questions/51679363/multi-module-maven-project-on-dockers
FROM quay.io/strimzi/kafka:0.34.0-kafka-3.4.0

COPY target/sunki-0.0.1-SNAPSHOT-jar-with-dependencies.jar /opt/kafka/plugins/
