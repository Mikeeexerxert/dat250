# ---------------------------------------
# Stage 1: Build the Spring Boot JAR
# ---------------------------------------
FROM gradle:8.10.2-jdk21 AS builder
WORKDIR /home/gradle/project

# Copy source code
COPY --chown=gradle:gradle . .

# Build application
RUN gradle clean bootJar --no-daemon

# ---------------------------------------
# Stage 2: Run the application
# ---------------------------------------
FROM eclipse-temurin:21-jdk AS runner
WORKDIR /app

# Copy the built jar from the previous stage
COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar

# Create a non-root user for better security
RUN useradd -m springuser
USER springuser

# Expose the default Spring Boot port
EXPOSE 8080

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]