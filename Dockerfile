# Stage 1: Build
FROM gradle:8.5-jdk21 AS build

WORKDIR /app

# Copy only gradle wrapper and build config to leverage Docker layer caching
COPY gradlew .
COPY gradle gradle/
COPY build.gradle settings.gradle ./

# Pre-download dependencies (this layer will be cached)
RUN ./gradlew build -x test --dry-run 2>/dev/null || true

# Copy source code
COPY src ./src

# Build the application
RUN ./gradlew clean build -x test --no-daemon

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
