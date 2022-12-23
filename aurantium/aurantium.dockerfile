FROM ubuntu:22.04
#FROM amazoncorretto:17
#FROM eclipse-mosquitto

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

RUN mkdir -p "/data"
ARG data_folder
COPY ${data_folder} /data
ENV filePath="/data"

RUN apt-get update && \
  apt-get -y install mosquitto mosquitto-clients && \
  apt-get -y install openjdk-17-jdk openjdk-17-jre
#    apt-get -y install sudo

#RUN adduser --disabled-password --gecos '' docker
#RUN adduser docker sudo
#RUN echo '%sudo ALL=(ALL) NOPASSWD:ALL' >> /etc/sudoers
#
#USER docker

#    wget -O- https://apt.corretto.aws/corretto.key | sudo apt-key add - && \
#    sudo add-apt-repository 'deb https://apt.corretto.aws stable main' && \
#    sudo apt-get update; && \
#    sudo apt-get install -y java-17-amazon-corretto-jdk
CMD java -jar app.jar
#RUN /bin/sh