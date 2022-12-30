call mvn install
docker build -t feridock/pgpcloudbackup:0.1 .
docker push feridock/pgpcloudbackup:0.1