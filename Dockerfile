# Dockerfile for the GlitchDex Spring Boot Application
# This uses a multi-stage build for a smaller, more secure final image.

# --- Stage 1: Build the application with Maven ---
FROM maven:3.9-eclipse-temurin-21 AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and download dependencies. This is cached by Docker.
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Package the application, skipping the tests for a faster build
RUN mvn package -DskipTests


# --- Stage 2: Create the final, lightweight image ---
FROM eclipse-temurin:21-jre-alpine

# Set the working directory
WORKDIR /app

# Copy the executable .jar file from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# The command to run the application when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]