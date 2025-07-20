package com.dhi.camelkafka.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Configuration properties for Kafka consumer settings.
 */
@Data
@Component
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {
    
    private String bootstrapServers = "localhost:9092";
    private String groupId = "camel-kafka-consumer-group";
    private String topic = "test-topic"; // Deprecated - use topics instead
    private List<String> topics = List.of("test-topic"); // Multiple topics support
    private String topic1; // Individual topic properties for flexibility
    private String topic2;
    private String topic3;
    private String topic4;
    private String topic5;
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
    
    /**
     * Get all configured topics, combining individual and list properties.
     */
    public List<String> getAllTopics() {
        java.util.List<String> allTopics = new java.util.ArrayList<>();
        
        // Add topics from list
        if (topics != null && !topics.isEmpty()) {
            allTopics.addAll(topics);
        }
        
        // Add individual topics if they exist and aren't already in the list
        if (topic1 != null && !topic1.trim().isEmpty() && !allTopics.contains(topic1)) {
            allTopics.add(topic1);
        }
        if (topic2 != null && !topic2.trim().isEmpty() && !allTopics.contains(topic2)) {
            allTopics.add(topic2);
        }
        if (topic3 != null && !topic3.trim().isEmpty() && !allTopics.contains(topic3)) {
            allTopics.add(topic3);
        }
        if (topic4 != null && !topic4.trim().isEmpty() && !allTopics.contains(topic4)) {
            allTopics.add(topic4);
        }
        if (topic5 != null && !topic5.trim().isEmpty() && !allTopics.contains(topic5)) {
            allTopics.add(topic5);
        }
        
        // Fallback to single topic if no topics configured
        if (allTopics.isEmpty() && topic != null && !topic.trim().isEmpty()) {
            allTopics.add(topic);
        }
        
        return allTopics;
    }
}
