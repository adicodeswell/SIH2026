FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml ./
COPY backend/application-service/pom.xml backend/application-service/pom.xml
COPY backend/interoperability-service/pom.xml backend/interoperability-service/pom.xml
COPY backend/security-workflow-service/pom.xml backend/security-workflow-service/pom.xml
COPY mock-systems/education-system/pom.xml mock-systems/education-system/pom.xml
COPY mock-systems/employment-system/pom.xml mock-systems/employment-system/pom.xml
RUN mvn -pl backend/application-service dependency:go-offline

COPY backend/application-service/src backend/application-service/src
RUN mvn -pl backend/application-service clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/backend/application-service/target/application-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
