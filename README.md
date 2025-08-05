# Apache Camel Kafka Consumer with Manual Commits and Callbacks

A Spring Boot application demonstrating Apache Camel Kafka consumer with manual commits, comprehensive callbacks for tracking message metadata (topic, partition, offset, timestamp), and efficient message processing using Lombok and structured logging.

## 🔐 Security Support

This application supports multiple Kafka security configurations:

- **🌐 Confluent Cloud**: SASL_SSL with PLAIN mechanism
- **🔒 Apache Kafka with ACLs**: SASL_PLAINTEXT and SASL_SSL with SCRAM-SHA-256
- **🔑 Java Keystores**: Full SSL/TLS support with client certificates
- **📊 Multi-topic Consumption**: Consume from multiple topics simultaneously
- **🌐 Web Dashboard**: Real-time message monitoring and statistics

## Features

- ✅ **Manual Kafka Commits**: Messages are committed manually after successful processing
- ✅ **Comprehensive Callbacks**: Track topic, partition, offset, and timestamp for each message
- ✅ **Multi-topic Support**: Consume from multiple topics with dynamic routing
- ✅ **Security Configurations**: Support for SASL, SSL, ACLs, and Java keystores
- ✅ **Web Dashboard**: Real-time monitoring of consumed messages
- ✅ **Lombok Integration**: Clean, boilerplate-free code using Lombok annotations
- ✅ **Structured Logging**: SLF4J with logback for monitoring and debugging
- ✅ **Error Handling**: Robust error handling with retry mechanisms
- ✅ **Spring Boot Integration**: Full Spring Boot configuration and management
- ✅ **Metrics and Monitoring**: Built-in statistics tracking and health endpoints
- ✅ **Docker Support**: Containerized deployment with SSL keystore mounting

## Architecture

```
┌─────────────────────┐    ┌──────────────────────┐    ┌─────────────────────┐
│   Kafka Broker     │    │  Camel Route         │    │  Message Processor  │
│                     │───▶│  - Consumer          │───▶│  - Business Logic   │
│  Topic: test-topic  │    │  - Error Handling    │    │  - Manual Commit    │
└─────────────────────┘    └──────────────────────┘    └─────────────────────┘
                                       │                          │
                                       ▼                          ▼
                           ┌──────────────────────┐    ┌─────────────────────┐
                           │  Callback Service    │    │  Message Model      │
                           │  - Metadata Extract  │    │  - Lombok Entities  │
                           │  - Statistics        │    │  - Validation       │
                           └──────────────────────┘    └─────────────────────┘
```

## Prerequisites

- Java 17 or later
- Apache Kafka 2.8+
- Maven 3.6+

## 🚀 Configuration Options

### 1. Apache Kafka with ACLs (Recommended for Production)

For secure production deployment with Apache Kafka and ACLs:

```bash
# Use Apache Kafka with SASL/PLAINTEXT (development)
java -jar target/camel-kafka-consumer-1.0-SNAPSHOT.jar --spring.profiles.active=apache-kafka

# Use Apache Kafka with SASL/SSL and Java keystores (production)
java -jar target/camel-kafka-consumer-1.0-SNAPSHOT.jar --spring.profiles.active=apache-kafka-ssl

# Use environment variables configuration
java -jar target/camel-kafka-consumer-1.0-SNAPSHOT.jar --spring.profiles.active=apache-kafka-env
```

### 2. Confluent Cloud

For Confluent Cloud deployment:

```bash
java -jar target/camel-kafka-consumer-1.0-SNAPSHOT.jar --spring.profiles.active=confluent
```

### 3. Environment Variables Configuration

Copy the template and configure your environment:

```bash
# Copy environment template
cp .env.template .env

# Edit .env with your actual configuration
# Set passwords and keystore locations

# Export environment variables
set -a; source .env; set +a

# Run with environment configuration
java -jar target/camel-kafka-consumer-1.0-SNAPSHOT.jar --spring.profiles.active=apache-kafka-env
```

### 4. Docker Deployment with SSL

```bash
# Build Docker image
docker build -t camel-kafka-consumer .

# Run with Docker Compose (includes SSL keystore mounting)
docker-compose -f docker-compose-ssl.yml up
```

## Quick Start

### 1. Start Kafka (Local Development)

```bash
# Start Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka
bin/kafka-server-start.sh config/server.properties

# Create test topic
bin/kafka-topics.sh --create --topic test-topic --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
```

### 2. Build and Run the Application

```bash
# Build the project
mvn clean compile

# Run the application
mvn spring-boot:run
```

### 3. Send Test Messages

```bash
# Send messages to test the consumer
bin/kafka-console-producer.sh --topic test-topic --bootstrap-server localhost:9092

# Type messages and press Enter (Ctrl+C to exit)
Hello World
Test message with error  # This will trigger error handling
Another message
```

## Configuration

### Application Properties

```properties
# Kafka Configuration
kafka.bootstrap-servers=localhost:9092
kafka.group-id=camel-kafka-consumer-group
kafka.topic=test-topic
kafka.enable-auto-commit=false  # Manual commits enabled
kafka.max-poll-records=10       # Batch size for efficient processing

# Logging
logging.level.com.example.camelkafka=INFO
```

### Manual Commit Configuration

The application uses manual commits for reliable message processing:

```java
@Component
public class KafkaMessageProcessor implements Processor {
    
    @Override
    public void process(Exchange exchange) throws Exception {
        // Process message
        KafkaMessage message = callbackService.extractMessageMetadata(exchange);
        
        // Manual commit after successful processing
        KafkaManualCommit manualCommit = exchange.getIn()
            .getHeader(KafkaConstants.MANUAL_COMMIT, KafkaManualCommit.class);
        manualCommit.commit();
    }
}
```

## Callback System

### Message Metadata Extraction

```java
public KafkaMessage extractMessageMetadata(Exchange exchange) {
    String topic = exchange.getIn().getHeader(KafkaConstants.TOPIC, String.class);
    Integer partition = exchange.getIn().getHeader(KafkaConstants.PARTITION, Integer.class);
    Long offset = exchange.getIn().getHeader(KafkaConstants.OFFSET, Long.class);
    Long timestamp = exchange.getIn().getHeader(KafkaConstants.TIMESTAMP, Long.class);
    
    return KafkaMessage.builder()
            .topic(topic)
            .partition(partition)
            .offset(offset)
            .timestamp(timestamp)
            .processedAt(LocalDateTime.now())
            .build();
}
```

### Available Callbacks

- **`onMessageProcessed()`**: Called after successful message processing
- **`onMessageError()`**: Called when message processing fails
- **`onManualCommit()`**: Called after successful manual commit
- **`extractMessageMetadata()`**: Extracts topic, partition, offset, timestamp

## Monitoring and Metrics

### Statistics Tracking

```bash
# View processing statistics in logs
2025-07-08 10:58:22 [main] INFO  c.e.c.s.KafkaCallbackService - Processing statistics - Processed: 100, Errors: 2
```

### Management Endpoints

Access monitoring endpoints:

- Health: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/metrics`
- Camel Routes: `http://localhost:8080/actuator/camelroutes`

## Example Output

```
2025-07-08 10:58:22 [Camel (camel-1) thread #1 - KafkaConsumer[test-topic]] INFO  c.e.c.s.KafkaCallbackService - Received message - ID: test-topic-0-123, Topic: test-topic, Partition: 0, Offset: 123, Timestamp: 1731234567890, Content length: 11
2025-07-08 10:58:22 [Camel (camel-1) thread #1 - KafkaConsumer[test-topic]] INFO  c.e.c.p.KafkaMessageProcessor - Processed message content: 11 characters from Topic: test-topic, Partition: 0, Offset: 123, Timestamp: 1731234567890
2025-07-08 10:58:22 [Camel (camel-1) thread #1 - KafkaConsumer[test-topic]] INFO  c.e.c.s.KafkaCallbackService - Successfully processed message: Topic: test-topic, Partition: 0, Offset: 123, Timestamp: 1731234567890
```

## Error Handling

The application includes comprehensive error handling:

- **Route-level error handling**: Catches and logs exceptions
- **Manual commit failures**: Proper rollback mechanisms
- **Dead letter queues**: For messages that fail repeatedly
- **Statistics tracking**: Monitor error rates and patterns

## Testing

```bash
# Run unit tests
mvn test

# Run integration tests (requires Kafka)
mvn verify
```

## Project Structure

```
src/
├── main/java/com/example/camelkafka/
│   ├── CamelKafkaConsumerApplication.java
│   ├── config/
│   │   └── KafkaProperties.java
│   ├── model/
│   │   └── KafkaMessage.java
│   ├── processor/
│   │   └── KafkaMessageProcessor.java
│   ├── route/
│   │   └── KafkaConsumerRoute.java
│   └── service/
│       └── KafkaCallbackService.java
└── test/java/com/example/camelkafka/
    └── CamelKafkaConsumerApplicationTests.java
```

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/new-feature`
3. Commit changes: `git commit -am 'Add new feature'`
4. Push to branch: `git push origin feature/new-feature`
5. Submit a pull request

## References

- [Apache Camel Kafka Component](https://camel.apache.org/components/3.21.x/kafka-component.html)
- [Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Project Lombok](https://projectlombok.org/)

## Confluent Cloud Configuration

This project supports connecting to Confluent Cloud for managed Kafka services. Follow these steps to configure:

### 1. Create Confluent Cloud Resources

1. **Sign up for Confluent Cloud**: Visit [confluent.cloud](https://confluent.cloud) and create an account
2. **Create a Kafka Cluster**: Set up a Basic or Standard cluster in your preferred region
3. **Create API Keys**: Generate API key and secret for your cluster
4. **Create a Topic**: Create the topic you want to consume from

### 2. Configure Application

#### Option A: Using Profile Configuration
1. Copy the example configuration:
   ```bash
   cp src/main/resources/application-confluent.properties.example src/main/resources/application-confluent.properties
   ```

2. Update the configuration with your Confluent Cloud details:
   ```properties
   kafka.bootstrap-servers=pkc-xxxxx.us-west-2.aws.confluent.cloud:9092
   kafka.sasl-jaas-config=org.apache.kafka.common.security.plain.PlainLoginModule required username='YOUR_API_KEY' password='YOUR_API_SECRET';
   kafka.topic=your-topic-name
   ```

3. Run with the Confluent profile:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=confluent
   ```

#### Option B: Using Environment Variables
Set the following environment variables:
```bash
export KAFKA_BOOTSTRAP_SERVERS=pkc-xxxxx.us-west-2.aws.confluent.cloud:9092
export KAFKA_SECURITY_PROTOCOL=SASL_SSL
export KAFKA_SASL_MECHANISM=PLAIN
export KAFKA_SASL_JAAS_CONFIG="org.apache.kafka.common.security.plain.PlainLoginModule required username='YOUR_API_KEY' password='YOUR_API_SECRET';"
export KAFKA_TOPIC=your-topic-name
```

### 3. Schema Registry (Optional)

If using Avro with Schema Registry:

1. **Enable Schema Registry** in Confluent Cloud
2. **Create Schema Registry API Keys**
3. **Update configuration**:
   ```properties
   kafka.schema-registry-url=https://psrc-xxxxx.us-west-2.aws.confluent.cloud
   kafka.schema-registry-basic-auth-user-info=SR_API_KEY:SR_API_SECRET
   kafka.value-deserializer=io.confluent.kafka.serializers.KafkaAvroDeserializer
   kafka.specific-avro-reader=true
   ```

### 4. Security Best Practices

- **Never commit credentials**: Use environment variables or encrypted configuration
- **Use least privilege**: Create API keys with minimal required permissions
- **Rotate credentials**: Regularly rotate API keys
- **Monitor usage**: Use Confluent Cloud metrics to monitor consumption

### 5. Testing Connection

Run the application and check logs for successful connection:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=confluent
```

Look for log messages indicating successful Kafka consumer startup and topic subscription.

## 🔒 Security Setup

### Apache Kafka with ACLs and SSL Setup

For detailed setup instructions, see:
- **[APACHE_KAFKA_ACL_SETUP.md](APACHE_KAFKA_ACL_SETUP.md)** - Complete guide for setting up Apache Kafka with ACLs
- **[SSL_KEYSTORE_SETUP.md](SSL_KEYSTORE_SETUP.md)** - Step-by-step SSL certificate and keystore setup

### Quick SSL Setup Summary

1. **Generate certificates and keystores**:
   ```bash
   # Create CA certificate
   openssl req -new -x509 -keyout ca-key -out ca-cert -days 365
   
   # Create client keystore
   keytool -keystore kafka.client.keystore.jks -alias kafka-client -validity 365 -genkey
   
   # Create truststore
   keytool -keystore kafka.client.truststore.jks -alias CARoot -import -file ca-cert
   ```

2. **Configure application properties**:
   ```properties
   kafka.securityProtocol=SASL_SSL
   kafka.saslMechanism=SCRAM-SHA-256
   kafka.sslKeystoreLocation=/path/to/kafka.client.keystore.jks
   kafka.sslTruststoreLocation=/path/to/kafka.client.truststore.jks
   ```

3. **Set up ACL permissions**:
   ```bash
   # Create consumer user
   kafka-configs.sh --zookeeper localhost:2181 --alter --add-config 'SCRAM-SHA-256=[password=user-password]' --entity-type users --entity-name kafka-consumer-user
   
   # Grant consumer permissions
   kafka-acls.sh --authorizer-properties zookeeper.connect=localhost:2181 --add --allow-principal User:kafka-consumer-user --operation Read --topic test-topic --group camel-kafka-consumer-group
   ```

### Configuration Files Overview

| File | Purpose | Environment |
|------|---------|-------------|
| `application.properties` | Default local configuration | Development |
| `application-apache-kafka.properties` | Apache Kafka with ACLs | Development/Production |
| `application-apache-kafka-ssl.properties` | Apache Kafka with SSL keystores | Production |
| `application-apache-kafka-env.properties` | Environment variables configuration | Production |
| `application-confluent.properties` | Confluent Cloud configuration | Production |
| `.env.template` | Environment variables template | Production |

## Configuration
