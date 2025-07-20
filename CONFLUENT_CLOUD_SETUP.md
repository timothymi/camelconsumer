# Confluent Cloud Multi-Topic Kafka Consumer Setup

## 🚀 How to Configure for Confluent Cloud

### 1. Get Your Confluent Cloud Credentials

First, you need to obtain your API credentials from Confluent Cloud:

1. Log into your Confluent Cloud console
2. Go to **Data Integration** > **API Keys**
3. Create a new API Key with appropriate permissions for your topics
4. Note down your **API Key** and **API Secret**

### 2. Configure Your Topics

The application supports multiple ways to configure topics:

#### Option A: Individual Topic Properties
```properties
kafka.topic1=json-purchase
kafka.topic2=avro-purchase
kafka.topic3=user-analytics
kafka.topic4=order-events
kafka.topic5=payment-events
```

#### Option B: List-based Configuration
```properties
kafka.topics=json-purchase,avro-purchase,user-analytics,order-events,payment-events
```

#### Option C: Mixed Configuration
You can use both approaches simultaneously. The application will merge all unique topics.

### 3. Update application.properties

Replace the placeholder values in your `application.properties`:

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
