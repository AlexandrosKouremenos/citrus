# TODO: Try this https://stackoverflow.com/questions/51679363/multi-module-maven-project-on-dockers
FROM alpine:latest

RUN apk add --no-cache openjdk17-jdk

COPY target/*.jar aurantium.jar

RUN mkdir -p "data"

ARG data_folder
COPY ${data_folder} data

ENV filePath="/data"

CMD ["/bin/sh", "-c", "java -jar aurantium.jar"]