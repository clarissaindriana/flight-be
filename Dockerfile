# Stage 1: This Dockerfile assumes the JAR is already built locally
# The GitLab CI pipeline builds the JAR first, then uses Docker to package it

# Production stage - Just run the pre-built JAR
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the pre-built jar from CI/CD artifacts
COPY app.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
