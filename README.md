# Spring Web application interacting with KAFKA

This is an implementation of a spring web mvc app that produces, consumes and streams simple Kafka Messages, when SSL
authentication is involved.

Also, containers were added since in today's world almost everything lives in containers.
So, a Dockerfile with security and following docker best practises for java applications, is provided.

## Environments

There are 3 environments available, to test the connectivity of spring application with kafka:

1) Local (is now done using integration junit tests)
2) Docker-compose
3) Kubernetes with Helm (not tested yet)

### Local

In order to test the functionality locally, integration unit tests are provided, using JUnit. They can be found
in [KafkaServiceIntegrationTest](src/test/java/com/all4football/services/KafkaServiceIntegrationTest.java)
Note that the kafka cluster has to be set up manually. One easy way to do it is by using docker-compose, without the
kafka-producer service. Or else, the user can manually set it up.

### Docker-compose

Docker-compose is set up, to make life easier. It provides a working cluster of 3 zookeepers and 2 kafka brokers. The
service is also set to use the brokers from docker-compose.
Note: The test kafka certs are copied in the docker filesystem using docker-compose configuration, rather than doing it
in Dockerfile

```shell
./gradlew clean build -x test
docker-compose up --build
```

### K8s with Helm

For K8s environments, we use minikube setup to test. Also,
a [custom helm-plugin](https://github.com/bpstelios10/helm-plugin) that provides templates needed.
In [build.gradle](build.gradle) you can find tasks to publish the images to a docker repo, so that are available for
k8s to download. You need to configure your own repo, so that you have access. Also, tasks to interact with k8s using
Helm.
You can find more info regarding using minikube with helm and how to deploy the current services in [the guide provided](K8S_WITH_HELM_GUIDE.md).

## API

List of requests to use for testing (can be simply used on browsers or any rest-client):

```
http://localhost:8080/kafka/kafka-producer/produce-messages
http://localhost:8080/kafka/kafka-consumer/consume-messages
http://localhost:8080/kafka/kafka-stream/process-messages
http://localhost:8080/kafka/kafka-consumer/consume-streamed-messages
```
