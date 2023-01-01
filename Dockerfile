FROM openjdk:8-alpine

RUN adduser -u 1000 -G root -D appuser
USER appuser:root

WORKDIR /app
COPY --chown=appuser:root docker/app/* /app/
RUN chmod a+x /app/*.sh
COPY target/pgpcloudbackup-jar-with-dependencies.jar /app/pgpcloudbackup.jar

VOLUME /backup

CMD /app/pgp-cloud-backup.sh 
