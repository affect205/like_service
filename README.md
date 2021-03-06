# like_service
Demo Java Application, exposing gaming like service. Used technologies: Java 8 SE, Gradle, Guice, Cassandra.

1. Docker Cassandra Image<br>
https://medium.com/@michaeljpr/five-minute-guide-getting-started-with-cassandra-on-docker-4ef69c710d84

2. Download Image<br>
docker pull datastax/dse-server:latest

3. Run and Usage commands<br>
```
The -g flag starts a Node with Graph Model enabled
The -s flag starts a Node with Search Engine enabled
The -k flag starts a Node with Spark Analytics enabled

docker run -e DS_LICENSE=accept --memory 4g --name app-dse -p 9042:9042 -d datastax/dse-server -g -s -k
docker start app-dse
docker stop app-dse

docker run -e DS_LICENSE=accept --link app-dse -p 9091:9091 --memory 1g --name app-studio -d datastax/dse-studio
docker start app-studio
docker stop app-studio
docker inspect app-dse | grep IPAddress
docker container ls -a
docker rm <cid>
```
4. Web Console<br>
http://localhost:9091/