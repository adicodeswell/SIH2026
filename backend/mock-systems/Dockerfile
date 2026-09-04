FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/mock-systems-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8091
ENTRYPOINT ["java", "-jar", "app.jar"]
