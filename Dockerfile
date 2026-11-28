FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/target/gym-1.0-SNAPSHOT.jar app.jar
USER 10001:10001
EXPOSE 8080 8081
ENTRYPOINT ["java", "-jar", "app.jar"]