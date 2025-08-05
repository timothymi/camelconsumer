# Multi-stage build for Apache Camel Kafka Consumer with SSL support
FROM maven:3.9.4-eclipse-temurin-17 AS builder

# Set working directory
WORKDIR /build

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Production stage
FROM eclipse-temurin:17-jre-alpine

# Install curl for health checks
RUN apk add --no-cache curl

# Create application user for security
RUN addgroup -g 1000 appgroup && \
    adduser -u 1000 -G appgroup -s /bin/sh -D appuser

# Set working directory
WORKDIR /app

# Create necessary directories
RUN mkdir -p /app/logs /opt/kafka/ssl /app/config && \
    chown -R appuser:appgroup /app /opt/kafka

# Copy JAR from builder stage
COPY --from=builder /build/target/camel-kafka-consumer-*.jar app.jar

# Copy configuration files
COPY src/main/resources/application*.properties /app/config/

# Set ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 8086

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8086/actuator/health || exit 1

# Set JVM options for production
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -Djava.security.egd=file:/dev/./urandom"

# Default profile
ENV SPRING_PROFILES_ACTIVE=apache-kafka-env

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
