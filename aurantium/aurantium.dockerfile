FROM alpine:latest

RUN apk add --no-cache openjdk17-jdk

COPY target/*.jar app.jar

RUN mkdir -p "data"

ARG data_folder
COPY ${data_folder} data

ENV filePath="/data"

CMD ["/bin/sh", "-c", "java -jar app.jar"]