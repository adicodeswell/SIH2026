FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/interoperability-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
