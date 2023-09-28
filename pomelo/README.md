# Pomelo

Pomelo's goal is to process incoming data from Kafka using the Streams API. The steps to process data are as follows:

- On startup, connect to the Kafka broker and initialize its Processor Topology.
- For each message received from the Kafka broker, validate and process the Machine message.
- Calculate the mean values of each Sensor for a window of 5 seconds and publish them to the Kafka broker.

## Maven Build

Run from the root directory of the project to build the .jar file.

``` mvn clean install -DskipTests -f pomelo/pom.xml ```

__Note:__ Depends on Protobuf module. Consider building it first.

## Local Run

To run locally, you need to have a Zookeeper and a Kafka broker up and running and then run the Pomelo processor.

### Zookeeper & Kafka Broker

Zookeeper: Run the ``` bash start-zookeeper.sh ``` located at the citrus/pomelo/scripts folder.

Kafka: Run the ``` bash start-kafka-broker.sh ``` located at the citrus/pomelo/scripts folder.

__Note__: To start fresh, delete the Kafka related logs with
```bash rm -rf /tmp/kafka-logs /tmp/zookeeper /tmp/kraft-combined-logs ```

### Pomelo

Run PomeloApplication.java with the following env variables:
1. kafka.bootstrap.servers=localhost:9092
2. kafka.group.id=pomelo-client
3. kafka.topic=machine

## Docker Build

Run from the root directory of the project to build the Docker image.

``` docker compose build pomelo ```

## Kubernetes Deploy

Run ``` bash pomelo-setup.sh``` from the pomelo/cluster-setup folder.

__Note__: By itself, Pomelo cluster does not have a source. Consider Sunki as well.