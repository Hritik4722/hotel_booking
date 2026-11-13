# Stage 1: Build application with Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependency downloads
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source and build executable JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal, secure JRE runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Install curl for container health checks
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

# Run as non-root user
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Copy built jar from builder stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
