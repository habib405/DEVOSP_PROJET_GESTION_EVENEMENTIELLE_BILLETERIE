# ── Stage 1 : Build ──────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Cache dependencies first (layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Build the application
COPY src ./src
RUN mvn package -DskipTests -q

# ── Stage 2 : Run ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/back-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
