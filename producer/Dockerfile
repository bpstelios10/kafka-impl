# use this intermediate image as base-image. this is good for security, since it makes u keep images structured and minimal.
# also if more java images are needed, this could be extracted to a separate file, build it and use it as common base
FROM openjdk:21-slim as java21-slim-base

MAINTAINER Stelios <bpstelios10@hotmail.com>

# exposing port here is mostly for documentation purposes. not really doing much
EXPOSE 8080
# create a group and user (no uid) to run as non-root. create home dir for this user
RUN groupadd app_executors_group && useradd -m -g app_executors_group app_executor

# set workdir of the container to the created user's home folder
WORKDIR /home/app_executor

# --- end base image here --- #
FROM java21-slim-base

# copy everything needed in user's home folder to have access to run (avoid giving privileges)
# things that change more frequently should be kept down in dockerfiles (to take advantage of docker build layers),
# so the jar that is probably the one changing should go bottom
COPY scripts/startup.sh /home/app_executor
COPY build/libs/kafka-impl-producer.jar /home/app_executor

# set user of the container
USER app_executor

CMD /home/app_executor/startup.sh
