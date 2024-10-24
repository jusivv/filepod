FROM openjdk:17.0.2-jdk
ARG FILEPOD_VERSION=1.3.3
WORKDIR /app
COPY ./boot/target/filepod-standalone-${FILEPOD_VERSION}.jar ./filepod-standalone.jar
CMD ["java", "-jar", "/app/filepod-standalone.jar"]