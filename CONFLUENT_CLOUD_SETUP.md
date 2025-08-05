# Configuration Comparison: Apache Kafka vs Confluent Cloud

This document compares the configuration differences between Apache Kafka with ACLs/SSL and Confluent Cloud, and provides migration guidance.

## 🔄 Configuration Comparison Table

| Feature | Apache Kafka with ACLs | Confluent Cloud |
|---------|------------------------|----------------|
| **Security Protocol** | `SASL_PLAINTEXT` or `SASL_SSL` | `SASL_SSL` |
| **SASL Mechanism** | `SCRAM-SHA-256` or `SCRAM-SHA-512` | `PLAIN` |
| **SSL Certificates** | Java Keystores (JKS) | Managed by Confluent |
| **Authentication** | SCRAM credentials | API Keys |
| **ACL Management** | Manual via Kafka CLI | Confluent Cloud Console |
| **Schema Registry** | Optional (self-managed) | Managed service |
| **Monitoring** | Self-managed (JMX, etc.) | Built-in Confluent Cloud metrics |

## 📋 Apache Kafka with ACLs Configuration

### SASL/PLAINTEXT (Development)
```properties
kafka.bootstrapServers=localhost:9092
kafka.securityProtocol=SASL_PLAINTEXT
kafka.saslMechanism=SCRAM-SHA-256
kafka.saslJaasConfig=org.apache.kafka.common.security.scram.ScramLoginModule required username='kafka-consumer-user' password='dev-password';
```

### SASL/SSL with Java Keystores (Production)
```properties
kafka.bootstrapServers=kafka-broker1.example.com:9093,kafka-broker2.example.com:9093
kafka.securityProtocol=SASL_SSL
kafka.saslMechanism=SCRAM-SHA-256
kafka.saslJaasConfig=org.apache.kafka.common.security.scram.ScramLoginModule required username='kafka-consumer-user' password='prod-password';

# SSL Configuration
kafka.sslTruststoreLocation=/opt/kafka/config/ssl/kafka.client.truststore.jks
kafka.sslTruststorePassword=truststore-password
kafka.sslTruststoreType=JKS
kafka.sslKeystoreLocation=/opt/kafka/config/ssl/kafka.client.keystore.jks
kafka.sslKeystorePassword=keystore-password
kafka.sslKeystoreType=JKS
kafka.sslKeyPassword=key-password
kafka.sslEndpointIdentificationAlgorithm=https
```

## 🌐 Confluent Cloud Configuration

```properties
# Replace with your actual Confluent Cloud bootstrap servers
kafka.bootstrapServers=pkc-e8mp5.eu-west-1.aws.confluent.cloud:9092

# Replace with your actual API credentials
kafka.saslJaasConfig=org.apache.kafka.common.security.plain.PlainLoginModule required username='YOUR_API_KEY' password='YOUR_API_SECRET';

# Configure your actual topics
kafka.topic1=your-first-topic
kafka.topic2=your-second-topic
# or use the list approach:
# kafka.topics=topic1,topic2,topic3
```

### 4. Optional: Schema Registry (for Avro topics)

If you're consuming Avro messages, add these configurations:

```properties
kafka.schemaRegistryUrl=https://psrc-xxxxx.us-east-2.aws.confluent.cloud
kafka.schemaRegistryBasicAuthUserInfo=SR_API_KEY:SR_API_SECRET
kafka.specificAvroReader=true
kafka.valueDeserializer=io.confluent.kafka.serializers.KafkaAvroDeserializer
```

### 5. Run with Confluent Cloud Profile

You can use the provided `application-confluent.properties` file:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=confluent
```

Or simply update the main `application.properties` file with your credentials.

## 🎯 Multi-Topic Consumer Features

### Automatic Route Creation
The application automatically creates separate Camel routes for each configured topic:
- `kafka-consumer-route-json-purchase`
- `kafka-consumer-route-avro-purchase`
- `kafka-consumer-route-user-analytics`

### Dashboard Features
- **Real-time monitoring** of messages from all topics
- **Topic-specific filtering** via REST API
- **Multi-topic statistics** and message counts
- **Responsive web interface** accessible at `http://localhost:8081/dashboard`

### REST API Endpoints
- `GET /dashboard/api/messages` - All recent messages from all topics
- `GET /dashboard/api/messages/topic/{topicName}` - Messages from specific topic
- `GET /dashboard/api/stats` - Overall consumption statistics

### Manual Commit Processing
Each topic consumer uses manual commits for reliable message processing:
- Messages are only committed after successful processing
- Comprehensive error handling and retry mechanisms
- Topic, partition, offset, and timestamp tracking

## 🔧 Troubleshooting

### Common Issues:

1. **Authentication Failed**: Verify your API key and secret are correct
2. **Topic Not Found**: Ensure topics exist in your Confluent Cloud cluster
3. **Connection Timeout**: Check your bootstrap servers URL
4. **Schema Registry Issues**: Verify Schema Registry credentials for Avro topics

### Logs to Monitor:
```
INFO  c.d.c.route.KafkaConsumerRoute - Configuring Kafka consumers for X topics: [topic1, topic2, ...]
INFO  o.a.c.i.engine.AbstractCamelContext - Started kafka-consumer-route-{topic} 
```

## 📊 Example Usage

1. Start the application: `mvn spring-boot:run`
2. Open dashboard: http://localhost:8081/dashboard
3. Monitor messages from all your Confluent Cloud topics
4. Use REST API for programmatic access to message data

The application will automatically consume from all configured topics simultaneously, providing a unified view of your multi-topic Kafka consumption in the web dashboard.

# Migration Guide: Apache Kafka to Confluent Cloud

This guide provides steps to migrate your configuration from Apache Kafka with ACLs to Confluent Cloud.

## 🔧 Migration Guide: Apache Kafka to Confluent Cloud

### 1. Update Connection Settings
```diff
- kafka.bootstrapServers=localhost:9092
+ kafka.bootstrapServers=pkc-xxxxx.us-west-2.aws.confluent.cloud:9092

- kafka.securityProtocol=SASL_PLAINTEXT
+ kafka.securityProtocol=SASL_SSL

- kafka.saslMechanism=SCRAM-SHA-256
+ kafka.saslMechanism=PLAIN
```

### 2. Update Authentication
```diff
- kafka.saslJaasConfig=org.apache.kafka.common.security.scram.ScramLoginModule required username='kafka-consumer-user' password='user-password';
+ kafka.saslJaasConfig=org.apache.kafka.common.security.plain.PlainLoginModule required username='<API_KEY>' password='<API_SECRET>';
```

### 3. Remove SSL Keystore Configuration
```diff
- kafka.sslTruststoreLocation=/opt/kafka/config/ssl/kafka.client.truststore.jks
- kafka.sslTruststorePassword=truststore-password
- kafka.sslKeystoreLocation=/opt/kafka/config/ssl/kafka.client.keystore.jks
- kafka.sslKeystorePassword=keystore-password
- kafka.sslKeyPassword=key-password
```

### 4. Add Confluent Cloud Optimizations
```properties
# Add these for Confluent Cloud
kafka.sslEndpointIdentificationAlgorithm=https
kafka.clientDnsLookup=use_all_dns_ips
kafka.acks=all
kafka.retries=2147483647
kafka.maxInFlightRequestsPerConnection=5
kafka.enableIdempotence=true
```

## 🏃‍♂️ Running with Different Configurations

### Apache Kafka with ACLs
```bash
# Development (SASL/PLAINTEXT)
java -jar target/camel-kafka-consumer-1.0-SNAPSHOT.jar --spring.profiles.active=apache-kafka

# Production (SASL/SSL with keystores)
java -jar target/camel-kafka-consumer-1.0-SNAPSHOT.jar --spring.profiles.active=apache-kafka-ssl

# Using environment variables
java -jar target/camel-kafka-consumer-1.0-SNAPSHOT.jar --spring.profiles.active=apache-kafka-env
```

### Confluent Cloud
```bash
java -jar target/camel-kafka-consumer-1.0-SNAPSHOT.jar --spring.profiles.active=confluent
```

## 🔒 Security Considerations

### Apache Kafka with ACLs
- ✅ Full control over SSL certificates and keystores
- ✅ Granular ACL management via CLI
- ✅ On-premises or private cloud deployment
- ⚠️ Requires SSL certificate management
- ⚠️ Manual ACL configuration

### Confluent Cloud
- ✅ Managed SSL certificates
- ✅ Web-based ACL management
- ✅ Built-in monitoring and alerting
- ✅ No infrastructure management required
- ⚠️ API key management required
- ⚠️ Vendor lock-in considerations

## 🎯 Recommendations

### Use Apache Kafka with ACLs when:
- You need full control over infrastructure
- Compliance requires on-premises deployment
- You have existing Kafka infrastructure
- Cost optimization for high-volume scenarios

### Use Confluent Cloud when:
- You want managed infrastructure
- Rapid development and deployment is prioritized
- Built-in monitoring and alerting is desired
- Team lacks Kafka operational expertise

## 📚 Additional Resources

- [Apache Kafka ACL Setup Guide](APACHE_KAFKA_ACL_SETUP.md)
- [SSL Keystore Setup Guide](SSL_KEYSTORE_SETUP.md)
- [Confluent Cloud Documentation](https://docs.confluent.io/cloud/current/overview.html)
- [Apache Kafka Security Guide](https://kafka.apache.org/documentation/#security)
