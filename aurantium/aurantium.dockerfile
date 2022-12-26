FROM ubuntu:22.04

RUN apt-get update && \
      apt-get -y install --no-install-recommends --no-install-suggests mosquitto mosquitto-clients && \
      apt-get -y install --no-install-recommends --no-install-suggests openjdk-17-jdk openjdk-17-jre

COPY scripts/mosquitto.conf /etc/mosquitto/mosquitto.conf

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app/app.jar

RUN mkdir -p "app/data"
ARG data_folder
COPY ${data_folder} app/data
ENV filePath="/app/data"

WORKDIR /app

CMD ["/bin/bash", "-c", "mosquitto -d;java -jar app.jar"]