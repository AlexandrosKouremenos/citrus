FROM ubuntu:22.04

RUN apt-get update && \
      apt-get -y install --no-install-recommends --no-install-suggests openjdk-17-jdk openjdk-17-jre

COPY target/*.jar app.jar

RUN mkdir -p "data"

ARG data_folder
COPY ${data_folder} data

ENV filePath="/data"

CMD ["/bin/bash", "-c", "java -jar app.jar"]