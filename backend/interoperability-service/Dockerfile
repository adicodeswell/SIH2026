FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /app
COPY pom.xml ./
COPY backend/interoperability-service/pom.xml backend/interoperability-service/pom.xml
COPY backend/application-service/pom.xml backend/application-service/pom.xml
COPY backend/security-workflow-service/pom.xml backend/security-workflow-service/pom.xml
COPY mock-systems/education-system/pom.xml mock-systems/education-system/pom.xml
COPY mock-systems/employment-system/pom.xml mock-systems/employment-system/pom.xml
RUN mvn -pl backend/interoperability-service dependency:go-offline

COPY backend/interoperability-service/src backend/interoperability-service/src
RUN mvn -pl backend/interoperability-service clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/backend/interoperability-service/target/interoperability-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
