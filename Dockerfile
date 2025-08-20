# Build stage
FROM --platform=${BUILDPLATFORM:-linux/amd64} maven:3.8.4-openjdk-17-slim AS builder

WORKDIR /build

# Copy pom.xml and download dependencies to leverage Docker cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code
COPY src ./src

# Build the application
ARG GITHUB_PACKAGES_USERNAME
ARG GITHUB_PACKAGES_TOKEN
RUN mvn clean package -DskipTests \
    -DGITHUB_PACKAGES_USERNAME=${GITHUB_PACKAGES_USERNAME} \
    -DGITHUB_PACKAGES_TOKEN=${GITHUB_PACKAGES_TOKEN}

# Runtime stage
FROM --platform=${TARGETPLATFORM:-linux/amd64} eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the application JAR from the builder stage
COPY --from=builder /build/target/am-processor-*.jar app.jar

# Install curl for health check and set timezone
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/* && \
    ln -sf /usr/share/zoneinfo/Asia/Kolkata /etc/localtime

# Environment setup
ENV SPRING_PROFILES_ACTIVE=docker \
    TZ=Asia/Kolkata \
    JAVA_OPTS="-Xms512m -Xmx1024m"

EXPOSE 8080

# Health check for the application
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
