# TODO: Try this https://stackoverflow.com/questions/51679363/multi-module-maven-project-on-dockers
FROM alpine:latest

RUN apk add --no-cache openjdk17-jdk \
    --no-cache libstdc++6 \
    --no-cache libstdc++

COPY target/*.jar pomelo.jar

EXPOSE 8085
EXPOSE 9092

CMD ["/bin/sh", "-c", "java -jar pomelo.jar"]