package com.example.camelkafka.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for Kafka consumer settings.
 */
@Data
@Component
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {
    
    private String bootstrapServers = "localhost:9092";
    private String groupId = "camel-kafka-consumer-group";
    private String topic = "test-topic";
    private String autoOffsetReset = "earliest";
    private boolean enableAutoCommit = false;
    private int sessionTimeoutMs = 30000;
    private int maxPollRecords = 10;
    private int maxPollIntervalMs = 300000;
    private String keyDeserializer = "org.apache.kafka.common.serialization.StringDeserializer";
    private String valueDeserializer = "org.apache.kafka.common.serialization.StringDeserializer";
    
    // Confluent Cloud / Security Configuration
    private String securityProtocol;
    private String saslMechanism;
    private String saslJaasConfig;
    private String sslEndpointIdentificationAlgorithm;
    private String clientDnsLookup;
    private String acks;
    private Integer retries;
    private Integer maxInFlightRequestsPerConnection;
    private Boolean enableIdempotence;
    
    // Schema Registry Configuration
    private String schemaRegistryUrl;
    private String schemaRegistryBasicAuthUserInfo;
    private Boolean specificAvroReader;
}
