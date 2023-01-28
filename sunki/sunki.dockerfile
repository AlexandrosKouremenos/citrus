FROM alpine:latest

RUN apk add --no-cache openjdk17-jdk

RUN apk add --no-cache mosquitto mosquitto-clients

COPY scripts/passwd /etc/mosquitto/

COPY target/*.jar sunki.jar

EXPOSE 1883
EXPOSE 8081

CMD ["/bin/sh", "-c", "mosquitto -d -c /mosquitto/config/mosquitto.conf;java -jar sunki.jar"]