# TODO: Try this https://stackoverflow.com/questions/51679363/multi-module-maven-project-on-dockers
FROM alpine:latest

RUN apk add --no-cache mosquitto mosquitto-clients

COPY scripts/passwd /etc/mosquitto/

EXPOSE 1883

CMD ["/bin/sh", "-c", "mosquitto -c /mosquitto/config/mosquitto.conf"]