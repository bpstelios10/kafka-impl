# Spring Web application interacting with KAFKA

This is an implementation of a spring web mvc app that produces, consumes and streams simple Kafka Messages, when SSL
authentication is involved.

Also, containers were added since in today's world almost everything lives in containers.
So, a Dockerfile with security and following docker best practises for java applications, is provided.

## Environments

There are 3 environments available, to test the connectivity of spring application with kafka:

1) Local (not tested yet)
2) Docker-compose
3) Kubernetes with Helm (not tested yet)

### Local

In order to test the functionality locally, main() methods are provided. This means that kafka cluster has to be set up
manually (or by using docker-compose, without the kafka-producer service).

### Docker-compose

Docker-compose is set up, to make life easier. It provides a working cluster of 3 zookeepers and 2 kafka brokers. The
service is also set to use the brokers from docker-compose.

### K8s with Helm (wip)

For K8s environments, we use minikube setup to test. Also,
a [custom helm-plugin](https://github.com/bpstelios10/helm-plugin) that provides templates needed.
In [build.gradle](/build.gradle) you can find tasks to publish the images to a docker repo, so that are available for
k8s to download. You need to configure your own repo, so that you have access. Also, tasks to interact with k8s using Helm.
