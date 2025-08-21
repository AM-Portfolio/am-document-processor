# Build stage - Dependencies
FROM maven:3.8.4-openjdk-17-slim AS dependencies

# Set working directory
WORKDIR /app

# Set up GitHub credentials first
ARG GITHUB_PACKAGES_USERNAME
ARG GITHUB_PACKAGES_TOKEN
ENV GITHUB_PACKAGES_USERNAME=${GITHUB_PACKAGES_USERNAME}
ENV GITHUB_PACKAGES_TOKEN=${GITHUB_PACKAGES_TOKEN}

# Copy only the files needed for dependency resolution
COPY settings.xml /root/.m2/settings.xml
COPY pom.xml .

# Download dependencies in a separate layer
# Use -T 2C to use 2 threads per core
RUN mvn dependency:go-offline -B -T 2C

# Build stage - Compilation
FROM dependencies AS builder

# Copy source code
COPY src ./src

# Build the application with parallel compilation
# Skip tests and use 2 threads per core
RUN mvn package -DskipTests -T 2C

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the built artifact from builder stage
COPY --from=builder /app/target/am-processor-*.jar app.jar

# Install curl for healthcheck
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/* && \
    # Set timezone
    ln -sf /usr/share/zoneinfo/Asia/Kolkata /etc/localtime

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=docker
ENV TZ=Asia/Kolkata

# Expose the application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
