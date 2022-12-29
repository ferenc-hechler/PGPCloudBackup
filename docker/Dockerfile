FROM openjdk:8-alpine

RUN adduser -u 1000 -G root -D appuser
USER appuser:root

WORKDIR /app
COPY app/pgpcloudbackup-jar-with-dependencies.jar /app/pgpcloudbackup.jar
COPY app/encrypt-key.pub /app/encrypt-key.pub

VOLUME /input

CMD java -jar pgpcloudbackup.jar 
