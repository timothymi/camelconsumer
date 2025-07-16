package com.example.camelkafka.route;

import com.example.camelkafka.config.KafkaProperties;
import com.example.camelkafka.processor.KafkaMessageProcessor;
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
        
        // Main Kafka consumer route with manual commits
        from(buildKafkaUri())
                .routeId("kafka-consumer-route")
                .log("Starting to process message from topic: " + kafkaProperties.getTopic())
                .process(messageProcessor)
                .log("Successfully processed message with manual commit");
        
        // Error handling route
        from("direct:error-handler")
                .routeId("error-handler-route")
                .log("Processing error: ${body}")
                .choice()
                    .when(header("CamelKafkaPartition").isNotNull())
                        .log("Error occurred for message from partition ${header.CamelKafkaPartition}, offset ${header.CamelKafkaOffset}")
                    .otherwise()
                        .log("Error occurred processing message");
    }
    
    /**
     * Builds the Kafka URI with manual commit configuration.
     * Supports both local and Confluent Cloud configurations.
     * 
     * @return The configured Kafka URI
     */
    private String buildKafkaUri() {
        StringBuilder uriBuilder = new StringBuilder();
        uriBuilder.append("kafka:").append(kafkaProperties.getTopic());
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
        log.info("Configured Kafka URI: {}", kafkaUri);
        
        return kafkaUri;
    }
}
