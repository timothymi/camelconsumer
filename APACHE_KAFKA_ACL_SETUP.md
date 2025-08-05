# Apache Kafka with ACLs Setup Guide

## 🔐 Apache Kafka ACL Configuration

This guide shows how to set up Apache Kafka with ACLs (Access Control Lists) for secure multi-topic consumption.

## 📋 Prerequisites

1. **Apache Kafka Cluster** with SASL/SCRAM authentication enabled
2. **Kafka ACLs** configured for your consumer user
3. **Network access** to your Kafka brokers

## 🛠️ Kafka Broker Configuration

### 1. Enable SASL/SCRAM in Kafka Broker

Add these configurations to your Kafka `server.properties`:

```properties
# Enable SASL/SCRAM authentication
listeners=SASL_PLAINTEXT://localhost:9092
security.inter.broker.protocol=SASL_PLAINTEXT
sasl.mechanism.inter.broker.protocol=SCRAM-SHA-256
sasl.enabled.mechanisms=SCRAM-SHA-256

# Enable ACLs
authorizer.class.name=kafka.security.authorizer.AclAuthorizer
super.users=User:admin

# JAAS configuration file
java.security.auth.login.config=/path/to/kafka_server_jaas.conf
```

### 2. Create JAAS Configuration File

Create `/path/to/kafka_server_jaas.conf`:

```
KafkaServer {
    org.apache.kafka.common.security.scram.ScramLoginModule required
    username="admin"
    password="admin-password";
};
```

## 👤 User Management

### 1. Create SCRAM Users

```bash
# Create admin user
kafka-configs.sh --zookeeper localhost:2181 --alter --add-config 'SCRAM-SHA-256=[password=admin-password]' --entity-type users --entity-name admin

# Create consumer user
kafka-configs.sh --zookeeper localhost:2181 --alter --add-config 'SCRAM-SHA-256=[password=consumer-password]' --entity-type users --entity-name kafka-consumer-user
```

### 2. Configure ACLs for Consumer

```bash
# Allow consumer to read from topics
kafka-acls.sh --authorizer-properties zookeeper.connect=localhost:2181 \
    --add --allow-principal User:kafka-consumer-user \
    --operation Read --topic ecs.AccessLogJsonV1

# Allow consumer to join consumer group
kafka-acls.sh --authorizer-properties zookeeper.connect=localhost:2181 \
    --add --allow-principal User:kafka-consumer-user \
    --operation Read --group camel-kafka-consumer-group2

# Allow consumer to describe topics (optional, for metadata)
kafka-acls.sh --authorizer-properties zookeeper.connect=localhost:2181 \
    --add --allow-principal User:kafka-consumer-user \
    --operation Describe --topic ecs.AccessLogJsonV1
```

### 3. For Multiple Topics (if needed)

```bash
# Add more topics as needed
kafka-acls.sh --authorizer-properties zookeeper.connect=localhost:2181 \
    --add --allow-principal User:kafka-consumer-user \
    --operation Read --topic another-topic

kafka-acls.sh --authorizer-properties zookeeper.connect=localhost:2181 \
    --add --allow-principal User:kafka-consumer-user \
    --operation Describe --topic another-topic
```

## 🔧 Application Configuration

### Basic SASL/PLAINTEXT Configuration

```properties
# Kafka Configuration - Apache Kafka with ACLs
kafka.bootstrapServers=localhost:9092
kafka.groupId=camel-kafka-consumer-group2
kafka.topics=ecs.AccessLogJsonV1

# Security Configuration
kafka.securityProtocol=SASL_PLAINTEXT
kafka.saslMechanism=SCRAM-SHA-256
kafka.saslJaasConfig=org.apache.kafka.common.security.scram.ScramLoginModule required username='kafka-consumer-user' password='consumer-password';
```

### Secure SASL/SSL Configuration (Production)

```properties
# Kafka Configuration - Apache Kafka with ACLs + SSL
kafka.bootstrapServers=your-kafka-broker:9093
kafka.groupId=camel-kafka-consumer-group2
kafka.topics=ecs.AccessLogJsonV1

# Security Configuration
kafka.securityProtocol=SASL_SSL
kafka.saslMechanism=SCRAM-SHA-256
kafka.saslJaasConfig=org.apache.kafka.common.security.scram.ScramLoginModule required username='kafka-consumer-user' password='consumer-password';

# SSL Configuration
kafka.sslTruststoreLocation=/path/to/kafka.client.truststore.jks
kafka.sslTruststorePassword=truststore-password
kafka.sslKeystoreLocation=/path/to/kafka.client.keystore.jks
kafka.sslKeystorePassword=keystore-password
kafka.sslKeyPassword=key-password
```

## 🚀 Running the Application

### 1. Start Kafka with ACLs

```bash
# Start Zookeeper
zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka with JAAS config
KAFKA_OPTS="-Djava.security.auth.login.config=/path/to/kafka_server_jaas.conf" \
kafka-server-start.sh config/server.properties
```

### 2. Create Topic (if needed)

```bash
kafka-topics.sh --create --topic ecs.AccessLogJsonV1 \
    --bootstrap-server localhost:9092 \
    --replication-factor 1 --partitions 3 \
    --command-config client.properties
```

Where `client.properties` contains:
```properties
security.protocol=SASL_PLAINTEXT
sasl.mechanism=SCRAM-SHA-256
sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required username="admin" password="admin-password";
```

### 3. Start the Camel Application

```bash
mvn spring-boot:run
```

## 🔍 Verification

### 1. Check ACLs

```bash
kafka-acls.sh --authorizer-properties zookeeper.connect=localhost:2181 --list
```

### 2. Test Consumer Access

```bash
kafka-console-consumer.sh --bootstrap-server localhost:9092 \
    --topic ecs.AccessLogJsonV1 \
    --group test-group \
    --consumer.config client.properties
```

### 3. Monitor Application Logs

Look for successful connection logs:
```
INFO  c.d.c.route.KafkaConsumerRoute - Configuring Kafka consumers for 1 topics: [ecs.AccessLogJsonV1]
INFO  o.a.c.i.engine.AbstractCamelContext - Started kafka-consumer-route-ecs-AccessLogJsonV1
```

## 🐛 Troubleshooting

### Common Issues:

1. **Authentication Failed**: 
   - Verify username/password in JAAS config
   - Check SCRAM user exists: `kafka-configs.sh --describe --entity-type users --entity-name kafka-consumer-user`

2. **Authorization Failed**:
   - Verify ACLs: `kafka-acls.sh --list`
   - Check topic names match exactly (case-sensitive)

3. **Connection Refused**:
   - Verify broker is running with SASL enabled
   - Check listeners configuration in server.properties

4. **SSL Errors**:
   - Verify keystore/truststore paths and passwords
   - Check certificate validity

### Debug Configuration

Add to application.properties for debugging:
```properties
logging.level.org.apache.kafka=DEBUG
logging.level.org.apache.kafka.clients.consumer=DEBUG
```

## 📊 Features Enabled

- ✅ **Multi-topic consumption** with ACL permissions
- ✅ **SASL/SCRAM authentication** 
- ✅ **Manual commit processing** for reliability
- ✅ **Web dashboard** at http://localhost:8086/dashboard
- ✅ **REST API** for message monitoring
- ✅ **Topic-specific filtering** and statistics
- ✅ **SSL support** for production environments

## 🔒 Security Best Practices

1. **Use strong passwords** for SCRAM users
2. **Principle of least privilege** - only grant necessary ACLs
3. **Use SSL in production** (SASL_SSL)
4. **Rotate credentials** regularly
5. **Monitor ACL violations** in Kafka logs
6. **Separate admin and application users**
