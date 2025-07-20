package com.dhi.camelkafka.route;

import com.dhi.camelkafka.config.KafkaProperties;
import com.dhi.camelkafka.processor.KafkaMessageProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Apache Camel route configuration for Kafka consumer with manual commits.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumerRoute extends RouteBuilder {
    
    private final KafkaProperties kafkaProperties;
    private final KafkaMessageProcessor messageProcessor;
    
    @Override
    public void configure() throws Exception {
        
        // Error handler for the route
        onException(Exception.class)
                .handled(true)
                .log("Error in Kafka consumer route: ${exception.message}")
                .to("direct:error-handler");
        
        // Create consumer routes for all configured topics
        java.util.List<String> topics = kafkaProperties.getAllTopics();
        log.info("Configuring Kafka consumers for {} topics: {}", topics.size(), topics);
        
        for (String topicName : topics) {
            String routeId = "kafka-consumer-route-" + topicName.replaceAll("[^a-zA-Z0-9]", "-");
            
            // Individual Kafka consumer route for each topic with manual commits
            from(buildKafkaUri(topicName))
                    .routeId(routeId)
                    .log("Starting to process message from topic: " + topicName + " (${header.CamelKafkaPartition}:${header.CamelKafkaOffset})")
                    .process(messageProcessor)
                    .log("Successfully processed message from topic: " + topicName + " with manual commit");
        }
        
        // Error handling route
        from("direct:error-handler")
                .routeId("error-handler-route")
                .log("Processing error: ${body}")
                .choice()
                    .when(header("CamelKafkaPartition").isNotNull())
                        .log("Error occurred for message from topic ${header.CamelKafkaTopic}, partition ${header.CamelKafkaPartition}, offset ${header.CamelKafkaOffset}")
                    .otherwise()
                        .log("Error occurred processing message");
    }
    
    /**
     * Builds the Kafka URI with manual commit configuration.
     * Supports both local and Confluent Cloud configurations.
     * 
     * @param topicName The Kafka topic name
     * @return The configured Kafka URI
     */
    private String buildKafkaUri(String topicName) {
        StringBuilder uriBuilder = new StringBuilder();
        uriBuilder.append("kafka:").append(topicName);
        uriBuilder.append("?brokers=").append(kafkaProperties.getBootstrapServers());
        uriBuilder.append("&groupId=").append(kafkaProperties.getGroupId());
        uriBuilder.append("&autoOffsetReset=").append(kafkaProperties.getAutoOffsetReset());
        uriBuilder.append("&allowManualCommit=true"); // Enable manual commits
        uriBuilder.append("&maxPollRecords=").append(kafkaProperties.getMaxPollRecords());
        uriBuilder.append("&sessionTimeoutMs=").append(kafkaProperties.getSessionTimeoutMs());
        uriBuilder.append("&maxPollIntervalMs=").append(kafkaProperties.getMaxPollIntervalMs());
        uriBuilder.append("&keyDeserializer=").append(kafkaProperties.getKeyDeserializer());
        uriBuilder.append("&valueDeserializer=").append(kafkaProperties.getValueDeserializer());
        
        // Add Confluent Cloud / Security configuration if provided
        // Only include parameters that are directly supported by Camel Kafka component
        if (kafkaProperties.getSecurityProtocol() != null) {
            uriBuilder.append("&securityProtocol=").append(kafkaProperties.getSecurityProtocol());
        }
        if (kafkaProperties.getSaslMechanism() != null) {
            uriBuilder.append("&saslMechanism=").append(kafkaProperties.getSaslMechanism());
        }
        if (kafkaProperties.getSaslJaasConfig() != null) {
            uriBuilder.append("&saslJaasConfig=").append(kafkaProperties.getSaslJaasConfig());
        }
        
        // Note: Additional properties like ssl.endpoint.identification.algorithm, 
        // client.dns.lookup, acks, max.in.flight.requests.per.connection, etc.
        // will be handled by the underlying Kafka client with default values
        // or can be configured via Spring Boot Kafka auto-configuration
        
        // Add Schema Registry configuration if provided
        if (kafkaProperties.getSchemaRegistryUrl() != null) {
            uriBuilder.append("&schemaRegistryUrl=").append(kafkaProperties.getSchemaRegistryUrl());
        }
        if (kafkaProperties.getSchemaRegistryBasicAuthUserInfo() != null) {
            uriBuilder.append("&schemaRegistryBasicAuthUserInfo=").append(kafkaProperties.getSchemaRegistryBasicAuthUserInfo());
        }
        if (kafkaProperties.getSpecificAvroReader() != null) {
            uriBuilder.append("&specificAvroReader=").append(kafkaProperties.getSpecificAvroReader());
        }
        
        String kafkaUri = uriBuilder.toString();
        log.info("Configured Kafka URI for topic {}: {}", topicName, kafkaUri);
        
        return kafkaUri;
    }
}
