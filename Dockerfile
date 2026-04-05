# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Cache Maven dependencies first for faster rebuilds.
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline

# Build the executable Spring Boot jar.
COPY src ./src
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

# Use a non-root user in production containers.
RUN useradd --system --create-home --uid 1001 appuser
USER appuser

COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
