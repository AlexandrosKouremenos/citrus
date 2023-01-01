FROM ubuntu:22.04

RUN apt-get update && \
      apt-get -y install --no-install-recommends --no-install-suggests openjdk-17-jdk openjdk-17-jre

RUN apt-get -y install --no-install-recommends --no-install-suggests mosquitto mosquitto-clients

COPY scripts/mosquitto.conf /etc/mosquitto/mosquitto.conf
COPY scripts/passwd /etc/mosquitto/

EXPOSE 1883

COPY target/*.jar app.jar

EXPOSE 8081

CMD ["/bin/bash", "-c", "mosquitto -d;java -jar app.jar"]
#CMD ["/bin/bash", "-c", "java -jar app.jar"]