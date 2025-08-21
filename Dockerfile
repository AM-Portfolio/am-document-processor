# Build stage
FROM maven:3.8.4-openjdk-17-slim AS builder

# Set working directory
WORKDIR /app

# Set up GitHub credentials first
ARG GITHUB_PACKAGES_USERNAME
ARG GITHUB_PACKAGES_TOKEN
ENV GITHUB_PACKAGES_USERNAME=${GITHUB_PACKAGES_USERNAME}
ENV GITHUB_PACKAGES_TOKEN=${GITHUB_PACKAGES_TOKEN}

# Copy Maven settings first
COPY settings.xml /root/.m2/settings.xml

# Copy pom.xml and download dependencies to leverage Docker cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code
COPY src ./src
COPY mvnw mvnw.cmd ./
COPY .mvn ./.mvn

# Build the application
RUN mvn clean package -DskipTests

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
