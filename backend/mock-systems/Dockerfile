FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /app
COPY backend/mock-systems/pom.xml backend/mock-systems/pom.xml
RUN mvn -f backend/mock-systems/pom.xml dependency:go-offline

COPY backend/mock-systems/src backend/mock-systems/src
RUN mvn -f backend/mock-systems/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/backend/mock-systems/target/mock-systems-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8091
ENTRYPOINT ["java", "-jar", "app.jar"]
