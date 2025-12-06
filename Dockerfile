# Single stage runtime image - uses pre-built JAR
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the pre-built JAR file
COPY target/am-document-processor-*.jar app.jar

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
EXPOSE 8070

# Health check (port 8070 is correct, not 8080)
HEALTHCHECK --interval=15s --timeout=10s --start-period=60s --retries=5 \
  CMD curl -f http://localhost:8070/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
