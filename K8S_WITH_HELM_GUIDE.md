# K8s with Helm Guide

For k8s deployments, helm is being used, on minikube. In this case, minikube uses Docker for VM host but other options
are welcome. This is a step-by-step guide to set up the environment and deploy all services on k8s.

## Set up minikube

The versions being used are `minikube v1.32.0 on Ubuntu 22.04` and `Kubernetes v1.28.3 on Docker 24.0.7`

```shell
# Create the minikube profile (with docker as host)
minikube start -p kafka-course --memory 8000
# Verify by listing the profiles
minikube profile list
# When finished with any work, we should be stopping the running profile
minikube stop -p kafka-course
```

### Access minikube services

In order to use the deployed services in minikube, we need the right ip to connect to it. Then, the service should
provide an external port (use NodePort for example) in order to expose it. If this is the case:
```shell
# Get this profile's ip
minikube -p kafka-course ip
# the {profile-ip} and external port can then be used in browsers, rest clients etc from host:
wget http://{profile-ip}:{external-port}/{endpoint}
```

## K8s Infrastructure

First we need to create the k8s namespaces etc. For the namespaces to be created we need to install the service in k8s:

```shell
# From root path:
helm install infrastructure helm/infrastructure
# And every time a change is applied, we need to upgrade the `version` in Chart.yaml and run 
helm upgrade infrastructure helm/infrastructure
# Verify by listing the namespaces and a quota
kubectl --context kafka-course get ns
kubectl --context kafka-course -n kafka-cluster-dev get quota
```

[//]: # (Next step is to add the helm repository for the common helm charts This will be needed in the services deployments:)

[//]: # (```shell)

[//]: # (# Add the repo that contains the templates we re going to use later on)

[//]: # (helm repo add helm-plugin 'https://raw.githubusercontent.com/bpstelios10/helm-plugin/master/')

[//]: # (# Verify by listing the repos:)

[//]: # (helm repo list)

[//]: # (```)

## Kafka cluster deployment

Now we are ready to create the k8s resources for kafka cluster and deploy it. This is a 1-zookeper 1-kafka deployment to
start with. The kafka-certs are encrypted and stored as secrets. Some values that can vary (like replicas count) are
stored in values.yaml.

```shell
# From root path:
helm install kafka-cluster helm/kafka-cluster
# And every time a change is applied, we need to upgrade the `version` in Chart.yaml and run 
helm upgrade kafka-cluster helm/kafka-cluster
# Verify by listing services, deployments, pods
kubectl --context kafka-course -n kafka-cluster-dev get svc
kubectl --context kafka-course -n kafka-cluster-dev get deploy
kubectl --context kafka-course -n kafka-cluster-dev get pods
```

## Application deployment

In order to the kafka-impl deployment, we need to first upload the docker image to a repo. For this example I m using my
personal repo, but feel free to use your own, in order to have access. And make the image public so that k8s can
download it and use it in deployment. Some gradle tasks are provided, to help with the image creation and upload.

```shell
./gradlew clean build
# we need to remember to upgrade the version of the image (increase project.version)
./gradlew buildImage
# In order to push, we should set the dockerUsername and dockerPassword in build.gradle
./gradlew buildImage
```

Next, we can deploy the image to k8s. Make sure the correct version of kafka-impl is set in values.yaml.
Similar to previous deployments, we can use helm commands:

```shell
# From root path:
helm install kafka-service helm/service
# And every time a change is applied, we need to upgrade the `version` in Chart.yaml and run 
helm upgrade kafka-service helm/service
# Note that if the change is/includes image version upgrade, we should change also the `appVersion` value to the image value
# Verify by listing services, deployments, pods
kubectl --context kafka-course -n kafka-cluster-dev get svc
kubectl --context kafka-course -n kafka-cluster-dev get deploy
kubectl --context kafka-course -n kafka-cluster-dev get pods
```
This service doesnt provide any external ports, so in order to access it we need to exec into some other pode and try:
```shell
# Access the kafka-broker pod
kubectl --context kafka-course -n kafka-cluster-dev exec -it kafka-cluster-ff4b5bcbf-5n8z8 bash
# Test endpoints with wget
wget kafka-impl:8080/kafka/kafka-producer/status
```
