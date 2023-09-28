# Sunki

Sunki's goal is to be the connection between MQTT and Kafka. The steps to convert data are as follows:

- On startup, connect to the Kafka broker, connect and subscribe to the MQTT broker.
- For each message received from the MQTT broker, convert and publish a message to the Kafka broker.

## Maven Build

Run from the root directory of the project to build the .jar file.

``` mvn clean install -DskipTests -f sunki/pom.xml ```

__Note:__ Depends on Protobuf module. Consider building it first.

## Local Run

To run locally, you need to have a Mosquitto, a Zookeeper and a Kafka broker up and running and then run the Sunki connector.

### Mosquitto Broker

1. Create a .conf file with some simple configuration: ``` echo -e 'listener 1883 \nallow_anonymous true' >> mosquitto.conf ```.
2. Install it locally with ```  apt install mosquitto ```.
    1. Run it with ``` mosquitto -c {FULL_PATH_OF_CONF_FILE} ```.
3. Use a Docker image. Pull it with ``` docker pull eclipse-mosquitto:2.0.11 ```.
    1. Run it with ``` docker run -it -p 1883:1883 -v {FULL_PATH_OF_CONF_FILE}:/mosquitto/config/mosquitto.conf eclipse-mosquitto:2.0.11 ```.

### Kafka Broker

Run from the root directory of the project to start the Zookeeper and the Kafka broker.

Zookeeper: ``` bash kafka_2.13-3.4.0/bin/zookeeper-server-start.sh config/zookeeper.properties ```

Kafka: ``` bash kafka_2.13-3.4.0/bin/kafka-server-start.sh config/server.properties ```

__Note__: To start fresh, delete the Kafka related logs with 
``` bash rm -rf /tmp/kafka-logs /tmp/zookeeper /tmp/kraft-combined-logs ```.
Also, see the Pomelo README for more ways to run.

### Sunki

Run the ``` bash kafka-connect.sh ``` located at the citrus/sunki/scripts folder.

## Docker Build

For a Sunki instance to work, it needs a Mosquitto instance to connect to.

Run from the root directory of the project to build the Docker image.

``` docker compose build sunki-mosquitto ```

``` docker compose build sunki-connector ```

## Kubernetes Deploy

For a Sunki Kubernetes cluster to work, it needs a Kafka broker instance to connect to.

If you want to run it with an already created Pomelo cluster, then run ``` bash sunki-setup.sh``` from the sunki/cluster-setup folder.

If you want to run it standalone, then run ``` bash sunki-setup.sh standalone``` from the sunki/cluster-setup folder.