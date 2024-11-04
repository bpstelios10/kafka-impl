#!/bin/bash

if [[ -z $DEPLOYMENT_ENVIRONMENT ]]
then
  echo -n "Error: \$DEPLOYMENT_ENVIRONMENT is not set. The k8-plugin sets this."
  echo " If using plain Docker, specify '--env DEPLOYMENT_ENVIRONMENT=<some-env>'"
  exit 2
fi

export SPRING_PROFILES_ACTIVE=$DEPLOYMENT_ENVIRONMENT

# EXTRA_JAVA_OPTIONS allows user to override some java options
# -XX:MaxMetaspaceSize=256m -XX:CompressedClassSpaceSize=32m -XX:ReservedCodeCacheSize=128m -XX:+UnlockExperimentalVMOptions
# using '-Djava.security.egd=file:/dev/urandom' to make tomcat startup faster, with some security cost, according to: https://www.2uo.de/myths-about-urandom/
export JAVA_OPTIONS="$JAVA_OPTIONS -Djava.security.egd=file:/dev/urandom -XX:+UseStringDeduplication -Xmx512M -Xms512M -Xss256k $EXTRA_JAVA_OPTIONS"
export GC_OPTIONS="-XX:+UseZGC -XX:+ZGenerational"

current_params="$@"
#echo $(ls)
echo "about to exec: java $JAVA_OPTIONS $GC_OPTIONS -jar kafka-impl-producer.jar"
exec java $JAVA_OPTIONS $GC_OPTIONS -jar kafka-impl-producer.jar
