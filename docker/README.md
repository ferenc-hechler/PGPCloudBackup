# how to build

```
cd ..
mvn install
cd docker
copy ../target/pgpcloudbackup-jar-with-dependencies.jar app
docker build -t feridock/pgpcloudbackup:0.1 .
docker push
```


