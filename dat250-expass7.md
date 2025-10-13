# DAT250 Exercise Assignment 7 Report

## Introduction

The goal of this exercise was to gain practical experience with **Docker** by containerizing our existing **Spring Boot Poll Application**.
Containerization allows packaging the entire runtime environment including the app, dependencies, and configuration into a portable image that runs consistently across different systems.

## Setup

Before starting, I ensured that Docker was properly installed and running on my system.

To verify installation:

```bash
docker system info
```

This command confirmed that Docker was active and ready to use.

## Building a Dockerized Spring Boot Application

### 1. Selecting a Base Image

Since our project uses **Gradle** and **Java 21**, I chose to use a multi-stage build based on them

---

### 2. Dockerfile

Below is the full `Dockerfile` used for the project:

```dockerfile
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
```

---

### 3. Building and Running the Image

**Build the image:**

```bash
docker build -t poll-app .
```

**Run the container:**

```bash
docker run -p 8080:8080 poll-app
```

---

### 4. Improvements Made

* Used **multi-stage builds** to reduce image size.
* Switched to a **non-root user** for improved security.
* Parameterized the base image for easier upgrades.
* Exposed the default port and simplified startup with `ENTRYPOINT`.

---

### 5. Verifying the Build

To check the image size and configuration:

```bash
docker images poll-app
```

To view logs:

```bash
docker logs <container_id>
```

The output confirmed successful startup of the Spring Boot Poll Application.

---

## Pending Issues

* Integration with RabbitMQ or Redis inside the same container network (planned for future setup).
* Potential need for Docker Compose for managing multi-container environments (app + database + message broker).

---

## Conclusion

This experiment provided hands-on experience with **Docker containerization**.
The Poll Application now runs inside a fully portable container, enabling consistent deployment across environments.
The use of **multi-stage builds** and **non-root users** ensures both security and efficiency.