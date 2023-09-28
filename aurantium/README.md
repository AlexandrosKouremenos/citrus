# Aurantium

Aurantium's goal is to be an MQTT publisher. The steps to publish data are as follows:

- On startup, connect to the MQTT broker.
- Read files one by one from a given folder path.
- In each contained file, read the data line by line skipping the first line (contains column names).
- For each line read, create a Machine object and schedule a Publish message to the MQTT broker.

## Maven Build

Run from the root directory of the project to build the .jar file.

``` mvn clean install -DskipTests -f aurantium/pom.xml ```

__Note:__ Depends on Protobuf module. Consider building it first.

## Local Run

To run locally, you need to have a Mosquitto broker up and running and then run Aurantium. 

### Mosquitto Broker

1. Create a .conf file with some simple configuration: ``` echo -e 'listener 1883 \nallow_anonymous true' >> mosquitto.conf ```.
2. Install it locally with ```  apt install mosquitto ```.
   1. Run it with ``` mosquitto -c {FULL_PATH_OF_CONF_FILE} ```.
3. Use a Docker image. Pull it with ``` docker pull eclipse-mosquitto:2.0.11 ```. 
   1. Run it with ``` docker run -it -p 1883:1883 -v {FULL_PATH_OF_CONF_FILE}:/mosquitto/config/mosquitto.conf eclipse-mosquitto:2.0.11 ```. 

### Aurantium

Run AurantiumApplication.java with the following env variables:
   1. filePath={FULL_PARENT_PATH}/citrus/aurantium/src/main/resources/iot-data/machine
   2. machine.id=0
   3. mqtt.host=127.0.0.1
   4. mqtt.port=1883
   5. publish.delay=1


## Docker Build

Run from the root directory of the project to build the Docker image. Consider having a Mosquitto broker already up and running.

``` docker compose build aurantium ```

## Docker Run

Run from the root directory of the project to run the Docker image(s).

``` bash aurantium/scripts/start-aurantium.sh {NUMBER_OF_INSTANCES} ```