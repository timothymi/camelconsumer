# Apache Kafka with ACLs and Java Keystores Setup Guide

## 🔐 Complete Apache Kafka SSL/TLS Setup with ACLs

This guide provides a complete setup for Apache Kafka with SASL/SSL authentication, ACLs, and Java keystores for production-ready security.

## 📋 Prerequisites

1. **Apache Kafka Cluster** (version 2.8+ recommended)
2. **Java 8+** for keystore management
3. **OpenSSL** for certificate generation
4. **Administrative access** to Kafka brokers

## 🛠️ Step 1: Generate SSL Certificates and Keystores

### 1.1 Create Certificate Authority (CA)

```bash
# Create CA private key
openssl req -new -x509 -keyout ca-key -out ca-cert -days 365 \
    -subj "/CN=kafka-ca/O=Your Organization/C=US" \
    -nodes

# Create CA truststore
keytool -keystore kafka.ca.truststore.jks -alias CARoot \
    -import -file ca-cert -storepass ca-truststore-password \
    -keypass ca-truststore-password -noprompt
```

### 1.2 Create Server Keystores (for each broker)

```bash
# Create server keystore
keytool -keystore kafka.server.keystore.jks -alias kafka-server \
    -validity 365 -genkey -keyalg RSA \
    -dname "CN=kafka-broker1.example.com,O=Your Organization,C=US" \
    -storepass server-keystore-password \
    -keypass server-key-password

# Create certificate signing request
keytool -keystore kafka.server.keystore.jks -alias kafka-server \
    -certreq -file cert-file \
    -storepass server-keystore-password

# Sign the certificate with CA
openssl x509 -req -CA ca-cert -CAkey ca-key -in cert-file \
    -out cert-signed -days 365 -CAcreateserial

# Import CA certificate to server keystore
keytool -keystore kafka.server.keystore.jks -alias CARoot \
    -import -file ca-cert \
    -storepass server-keystore-password -noprompt

# Import signed certificate to server keystore
keytool -keystore kafka.server.keystore.jks -alias kafka-server \
    -import -file cert-signed \
    -storepass server-keystore-password
```

### 1.3 Create Client Keystores

```bash
# Create client keystore
keytool -keystore kafka.client.keystore.jks -alias kafka-client \
    -validity 365 -genkey -keyalg RSA \
    -dname "CN=kafka-client,O=Your Organization,C=US" \
    -storepass keystore-password \
    -keypass key-password

# Create certificate signing request
keytool -keystore kafka.client.keystore.jks -alias kafka-client \
    -certreq -file client-cert-file \
    -storepass keystore-password

# Sign the certificate with CA
openssl x509 -req -CA ca-cert -CAkey ca-key -in client-cert-file \
    -out client-cert-signed -days 365 -CAcreateserial

# Import CA certificate to client keystore
keytool -keystore kafka.client.keystore.jks -alias CARoot \
    -import -file ca-cert \
    -storepass keystore-password -noprompt

# Import signed certificate to client keystore
keytool -keystore kafka.client.keystore.jks -alias kafka-client \
    -import -file client-cert-signed \
    -storepass keystore-password

# Create client truststore (copy of CA truststore)
cp kafka.ca.truststore.jks kafka.client.truststore.jks
```

## 🔧 Step 2: Configure Kafka Brokers

### 2.1 Update server.properties

```properties
# Basic Configuration
broker.id=1
listeners=SASL_SSL://kafka-broker1:9093
advertised.listeners=SASL_SSL://kafka-broker1.example.com:9093
security.inter.broker.protocol=SASL_SSL
sasl.mechanism.inter.broker.protocol=SCRAM-SHA-256
sasl.enabled.mechanisms=SCRAM-SHA-256

# SSL Configuration
ssl.keystore.location=/opt/kafka/config/ssl/kafka.server.keystore.jks
ssl.keystore.password=server-keystore-password
ssl.key.password=server-key-password
ssl.truststore.location=/opt/kafka/config/ssl/kafka.ca.truststore.jks
ssl.truststore.password=ca-truststore-password
ssl.endpoint.identification.algorithm=https
ssl.client.auth=required

# ACL Configuration
authorizer.class.name=kafka.security.authorizer.AclAuthorizer
super.users=User:admin

# JAAS Configuration
java.security.auth.login.config=/opt/kafka/config/kafka_server_jaas.conf
```

### 2.2 Create kafka_server_jaas.conf

```
KafkaServer {
    org.apache.kafka.common.security.scram.ScramLoginModule required
    username="admin"
    password="admin-password";
};
```

## 👤 Step 3: User and ACL Management

### 3.1 Create SCRAM Users

```bash
# Create admin user
kafka-configs.sh --zookeeper localhost:2181 --alter \
    --add-config 'SCRAM-SHA-256=[password=admin-password]' \
    --entity-type users --entity-name admin

# Create consumer user
kafka-configs.sh --zookeeper localhost:2181 --alter \
    --add-config 'SCRAM-SHA-256=[password=consumer-password]' \
    --entity-type users --entity-name kafka-consumer-user
```

### 3.2 Configure ACLs

```bash
# Allow consumer to read from topics
kafka-acls.sh --authorizer-properties zookeeper.connect=localhost:2181 \
    --add --allow-principal User:kafka-consumer-user \
    --operation Read --topic ecs.AccessLogJsonV1

# Allow consumer to join consumer group
kafka-acls.sh --authorizer-properties zookeeper.connect=localhost:2181 \
    --add --allow-principal User:kafka-consumer-user \
    --operation Read --group camel-kafka-consumer-group2

# Allow consumer to describe topics
kafka-acls.sh --authorizer-properties zookeeper.connect=localhost:2181 \
    --add --allow-principal User:kafka-consumer-user \
    --operation Describe --topic ecs.AccessLogJsonV1

# For multiple topics
kafka-acls.sh --authorizer-properties zookeeper.connect=localhost:2181 \
    --add --allow-principal User:kafka-consumer-user \
    --operation Read --topic logs.application

kafka-acls.sh --authorizer-properties zookeeper.connect=localhost:2181 \
    --add --allow-principal User:kafka-consumer-user \
    --operation Read --topic events.user
```

## 🚀 Step 4: Application Configuration

### 4.1 Update application.properties

```properties
# Kafka Configuration - Apache Kafka with ACLs and SSL Keystores
kafka.bootstrapServers=kafka-broker1:9093,kafka-broker2:9093,kafka-broker3:9093
kafka.groupId=camel-kafka-consumer-group2
kafka.topics=ecs.AccessLogJsonV1,logs.application,events.user

# Security Configuration
kafka.securityProtocol=SASL_SSL
kafka.saslMechanism=SCRAM-SHA-256
kafka.saslJaasConfig=org.apache.kafka.common.security.scram.ScramLoginModule required username='kafka-consumer-user' password='consumer-password';

# SSL Configuration with Java Keystores
kafka.sslTruststoreLocation=/opt/kafka/config/ssl/kafka.client.truststore.jks
kafka.sslTruststorePassword=truststore-password
kafka.sslTruststoreType=JKS
kafka.sslKeystoreLocation=/opt/kafka/config/ssl/kafka.client.keystore.jks
kafka.sslKeystorePassword=keystore-password
kafka.sslKeystoreType=JKS
kafka.sslKeyPassword=key-password
kafka.sslEndpointIdentificationAlgorithm=https

# Consumer Configuration
kafka.autoOffsetReset=earliest
kafka.enableAutoCommit=false
kafka.clientId=camel-kafka-consumer-client
```

### 4.2 Directory Structure

```
/opt/kafka/config/ssl/
├── kafka.client.keystore.jks      # Client keystore
├── kafka.client.truststore.jks    # Client truststore  
├── kafka.server.keystore.jks      # Server keystore (on brokers)
└── kafka.ca.truststore.jks        # CA truststore (on brokers)
```

## 🔍 Step 5: Testing and Validation

### 5.1 Test SSL Connection

```bash
# Create client.properties for testing
cat > client.properties << EOF
security.protocol=SASL_SSL
sasl.mechanism=SCRAM-SHA-256
sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required username="kafka-consumer-user" password="consumer-password";
ssl.truststore.location=/opt/kafka/config/ssl/kafka.client.truststore.jks
ssl.truststore.password=truststore-password
ssl.keystore.location=/opt/kafka/config/ssl/kafka.client.keystore.jks
ssl.keystore.password=keystore-password
ssl.key.password=key-password
ssl.endpoint.identification.algorithm=https
EOF

# Test consumer
kafka-console-consumer.sh --bootstrap-server kafka-broker1:9093 \
    --topic ecs.AccessLogJsonV1 \
    --group test-group \
    --consumer.config client.properties
```

### 5.2 Verify ACLs

```bash
# List all ACLs
kafka-acls.sh --authorizer-properties zookeeper.connect=localhost:2181 --list

# Test topic access
kafka-topics.sh --bootstrap-server kafka-broker1:9093 \
    --command-config client.properties \
    --list
```

### 5.3 Start the Application

```bash
# Copy keystores to application directory
cp /opt/kafka/config/ssl/kafka.client.* /opt/kafka/config/ssl/

# Start application
mvn spring-boot:run
```

## 📊 Application Features

### Multi-Topic SSL Consumer
- ✅ **SASL/SSL with SCRAM-SHA-256** authentication
- ✅ **Java KeyStore (JKS)** for SSL certificates
- ✅ **ACL-based authorization** for topic access
- ✅ **Manual commit processing** for reliability
- ✅ **Multi-topic consumption** with individual ACL permissions
- ✅ **Web dashboard** at http://localhost:8086/dashboard
- ✅ **REST API** for message monitoring

### Monitoring and APIs
- `GET /dashboard/api/messages` - All messages from authorized topics
- `GET /dashboard/api/messages/topic/{topic}` - Topic-specific messages
- `GET /dashboard/api/stats` - Consumption statistics

## 🔒 Security Best Practices

1. **Strong Passwords**: Use complex passwords for all keystores and users
2. **Certificate Rotation**: Regularly rotate SSL certificates (annually)
3. **Principle of Least Privilege**: Grant minimal required ACL permissions
4. **Secure Storage**: Protect keystore files with appropriate file permissions
5. **Network Security**: Use firewalls to restrict broker access
6. **Monitoring**: Monitor authentication failures and ACL violations
7. **Backup**: Securely backup keystores and certificate files

## 🐛 Troubleshooting

### Common SSL Issues:

1. **Certificate Verification Failed**:
   ```
   ssl.endpoint.identification.algorithm=
   ```
   (Disable hostname verification for testing only)

2. **Keystore Access Errors**:
   - Verify file paths and permissions
   - Check keystore passwords

3. **ACL Denied**:
   - Verify user permissions with `kafka-acls.sh --list`
   - Check topic names match exactly

4. **Authentication Failed**:
   - Verify SCRAM user exists and password is correct
   - Check JAAS configuration syntax

### Debug Configuration:
```properties
logging.level.org.apache.kafka=DEBUG
logging.level.org.apache.kafka.clients.consumer=DEBUG
logging.level.org.apache.kafka.common.security=DEBUG
```

## 📈 Production Deployment

1. **Multiple Brokers**: Configure SSL on all brokers
2. **Load Balancing**: Use multiple bootstrap servers
3. **Monitoring**: Implement SSL certificate monitoring
4. **Backup Strategy**: Regular keystore and configuration backups
5. **Disaster Recovery**: SSL certificate and key recovery procedures

Your Apache Kafka consumer is now configured with enterprise-grade security using SASL/SSL, ACLs, and Java keystores! 🎯
