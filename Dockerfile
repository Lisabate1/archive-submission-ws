# Build stage
FROM maven:3.3.9-jdk-8-alpine AS build-env

# Create app directory
WORKDIR /archive-submission-ws

COPY src ./src
COPY pom.xml ./
COPY config/application.yml ./application.yml
RUN mvn clean package -DskipTests

# Package stage
FROM maven:3.3.9-jdk-8-alpine
WORKDIR /archive-submission-ws
COPY --from=build-env /archive-submission-ws/target/archive-submission-ws.jar ./
ENTRYPOINT java ${JAVA_OPTS} -jar archive-submission-ws.jar