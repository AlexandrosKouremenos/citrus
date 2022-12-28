FROM ubuntu:22.04

RUN apt-get update && \
      apt-get -y install --no-install-recommends --no-install-suggests mosquitto mosquitto-clients && \
      apt-get -y install --no-install-recommends --no-install-suggests openjdk-17-jdk openjdk-17-jre

COPY scripts/mosquitto.conf /etc/mosquitto/mosquitto.conf

EXPOSE 1883

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app/app.jar

WORKDIR /app

EXPOSE 8081

CMD ["/bin/bash", "-c", "mosquitto -d;java -jar app.jar"]
#CMD ["/bin/bash", "-c", "java -jar app.jar"]