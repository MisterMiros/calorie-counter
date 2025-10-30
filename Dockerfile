# Build stage
FROM gradle:8.10.2-jdk21 AS build
WORKDIR /workspace
COPY . /workspace
# Build the application (skip tests for container build speed; adjust as needed)
RUN gradle clean bootJar -x test

# Run stage
FROM amazoncorretto:24.0.2-alpine3.22
ENV APP_HOME=/app
WORKDIR ${APP_HOME}
COPY --from=build /workspace/build/libs/*.jar app.jar

# Expose default port
EXPOSE 8080

# JVM opts can be overridden at runtime
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
