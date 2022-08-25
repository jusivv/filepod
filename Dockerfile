ARG FILE_REPOSITORY_VERSION=2.0.3
# dependency
FROM bitnami/git:2.37.1 as dep
WORKDIR /
RUN git clone --depth 1 --branch 2.0.3 https://github.com/jusivv/file-repository

# build stage
FROM maven:3.8.4-openjdk-11 AS build
WORKDIR /dep
COPY --from=dep /file-repository ./file-repository
WORKDIR /src/filepod
COPY ./ ./
RUN cd /dep/file-repository && mvn install -U -am -pl file-repository-sample -Dmaven.test.skip=true \
&& cd /src/filepod && mvn clean package war:exploded -U -am -pl boot -Dmaven.test.skip=true

# package stage
FROM tomcat:9.0.65-jre11-openjdk
WORKDIR /usr/local/tomcat/webapps/ROOT
RUN rm -rf *
COPY --from=build /src/filepod/boot/target/filepod ./

