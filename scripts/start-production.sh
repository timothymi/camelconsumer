#!/bin/bash

# Production Deployment Script for Apache Kafka Consumer with ACLs and SSL
# This script demonstrates how to run the application with secure configuration

echo "🚀 Starting Apache Camel Kafka Consumer with SSL/ACL configuration..."

# ==================================================
# Environment Variable Configuration
# ==================================================

# Kafka Configuration
export KAFKA_BOOTSTRAP_SERVERS="kafka-broker1.example.com:9093,kafka-broker2.example.com:9093,kafka-broker3.example.com:9093"
export KAFKA_GROUP_ID="camel-kafka-consumer-group-prod"
export KAFKA_TOPICS="ecs.AccessLogJsonV1,logs.application,events.user,metrics.system,alerts.critical"

# Security Configuration
export KAFKA_SECURITY_PROTOCOL="SASL_SSL"
export KAFKA_SASL_MECHANISM="SCRAM-SHA-256"
export KAFKA_SASL_JAAS_CONFIG="org.apache.kafka.common.security.scram.ScramLoginModule required username='kafka-consumer-user' password='${KAFKA_CONSUMER_PASSWORD}';"

# SSL Keystore Configuration
export KAFKA_SSL_TRUSTSTORE_LOCATION="/opt/kafka/config/ssl/kafka.client.truststore.jks"
export KAFKA_SSL_TRUSTSTORE_PASSWORD="${KAFKA_TRUSTSTORE_PASSWORD}"
export KAFKA_SSL_TRUSTSTORE_TYPE="JKS"
export KAFKA_SSL_KEYSTORE_LOCATION="/opt/kafka/config/ssl/kafka.client.keystore.jks"
export KAFKA_SSL_KEYSTORE_PASSWORD="${KAFKA_KEYSTORE_PASSWORD}"
export KAFKA_SSL_KEYSTORE_TYPE="JKS"
export KAFKA_SSL_KEY_PASSWORD="${KAFKA_KEY_PASSWORD}"
export KAFKA_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM="https"

# Consumer Configuration
export KAFKA_AUTO_OFFSET_RESET="earliest"
export KAFKA_ENABLE_AUTO_COMMIT="false"
export KAFKA_SESSION_TIMEOUT_MS="45000"
export KAFKA_MAX_POLL_RECORDS="10"
export KAFKA_MAX_POLL_INTERVAL_MS="300000"
export KAFKA_CLIENT_ID="camel-kafka-consumer-prod-client"
export KAFKA_REQUEST_TIMEOUT_MS="60000"

# Application Configuration
export SPRING_APPLICATION_NAME="camel-kafka-consumer"
export SERVER_PORT="8086"
export LOG_LEVEL_ROOT="INFO"
export LOG_LEVEL_CAMEL="INFO"
export LOG_LEVEL_KAFKA="WARN"
export LOG_FILE_PATH="/var/log/camel-kafka-consumer/application.log"

# Health and Monitoring
export MANAGEMENT_ENDPOINTS="health,info,metrics"
export MANAGEMENT_HEALTH_DETAILS="when-authorized"
export PROMETHEUS_ENABLED="true"

# ==================================================
# Pre-flight Checks
# ==================================================

echo "🔍 Running pre-flight checks..."

# Check if keystore files exist
if [ ! -f "$KAFKA_SSL_KEYSTORE_LOCATION" ]; then
    echo "❌ ERROR: Keystore file not found at $KAFKA_SSL_KEYSTORE_LOCATION"
    exit 1
fi

if [ ! -f "$KAFKA_SSL_TRUSTSTORE_LOCATION" ]; then
    echo "❌ ERROR: Truststore file not found at $KAFKA_SSL_TRUSTSTORE_LOCATION"
    exit 1
fi

# Check if required environment variables are set
if [ -z "$KAFKA_CONSUMER_PASSWORD" ]; then
    echo "❌ ERROR: KAFKA_CONSUMER_PASSWORD environment variable is not set"
    exit 1
fi

if [ -z "$KAFKA_TRUSTSTORE_PASSWORD" ]; then
    echo "❌ ERROR: KAFKA_TRUSTSTORE_PASSWORD environment variable is not set"
    exit 1
fi

if [ -z "$KAFKA_KEYSTORE_PASSWORD" ]; then
    echo "❌ ERROR: KAFKA_KEYSTORE_PASSWORD environment variable is not set"
    exit 1
fi

if [ -z "$KAFKA_KEY_PASSWORD" ]; then
    echo "❌ ERROR: KAFKA_KEY_PASSWORD environment variable is not set"
    exit 1
fi

echo "✅ Pre-flight checks passed"

# ==================================================
# Create Log Directory
# ==================================================

LOG_DIR=$(dirname "$LOG_FILE_PATH")
mkdir -p "$LOG_DIR"
echo "📁 Created log directory: $LOG_DIR"

# ==================================================
# Start Application
# ==================================================

echo "🚀 Starting application with Apache Kafka SSL/ACL configuration..."

# Option 1: Using environment variables configuration
java -jar target/camel-kafka-consumer-1.0-SNAPSHOT.jar \
    --spring.profiles.active=apache-kafka-env

# Option 2: Using properties file configuration (alternative)
# java -jar target/camel-kafka-consumer-1.0-SNAPSHOT.jar \
#     --spring.profiles.active=apache-kafka-ssl

echo "✅ Application started successfully"
echo "🌐 Dashboard available at: http://localhost:${SERVER_PORT}/dashboard"
echo "📊 Health endpoint: http://localhost:${SERVER_PORT}/actuator/health"
echo "📈 Metrics endpoint: http://localhost:${SERVER_PORT}/actuator/metrics"
