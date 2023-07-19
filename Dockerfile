# wait for optimizing

# dependency
FROM bitnami/git:2.37.1 as dep
ARG FILE_REPOSITORY_VERSION=2.0.4
WORKDIR /
RUN git clone --depth 1 --branch $FILE_REPOSITORY_VERSION https://github.com/jusivv/file-repository

# build stage
FROM maven:3.9.0-ibm-semeru-17-focal AS build
WORKDIR /dep
COPY --from=dep /file-repository ./file-repository
WORKDIR /src/filepod
COPY ./ ./
RUN cd /dep/file-repository && mvn install -U -am -pl file-repository-sample -Dmaven.test.skip=true \
&& cd /src/filepod && mvn clean package -U -am -pl boot -Dmaven.test.skip=true

# package stage
FROM openjdk:17.0.2-slim
WORKDIR /app
COPY --from=build /src/filepod/boot/target/filepod-standalone.jar ./
CMD ["java", "-jar", "/app/filepod-standalone.jar"]

