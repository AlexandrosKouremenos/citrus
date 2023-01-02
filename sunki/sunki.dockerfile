FROM alpine:latest

RUN apk add --no-cache openjdk17-jdk

RUN apk add --no-cache mosquitto mosquitto-clients

COPY scripts/mosquitto.conf /etc/mosquitto/mosquitto.conf
COPY scripts/passwd /etc/mosquitto/

COPY target/*.jar app.jar

EXPOSE 1883
EXPOSE 8081

CMD ["/bin/sh", "-c", "mosquitto -d;java -jar app.jar"]