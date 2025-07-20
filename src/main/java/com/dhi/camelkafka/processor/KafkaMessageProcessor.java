package com.dhi.camelkafka.processor;

import com.dhi.camelkafka.model.KafkaMessage;
import com.dhi.camelkafka.service.KafkaCallbackService;
import com.dhi.camelkafka.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.kafka.KafkaConstants;
import org.springframework.stereotype.Component;

/**
 * Processor for handling Kafka messages with manual commits and callbacks.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMessageProcessor implements Processor {

    private final KafkaCallbackService callbackService;
    private final DashboardService dashboardService;

    /**
     * Process the Kafka message from the exchange.
     * 
     * @param exchange The Camel exchange containing the Kafka message
     * @throws Exception If processing fails
     */
    @Override
    public void process(Exchange exchange) throws Exception {
        try {
            log.debug("Starting to process Kafka message");
            
            // Create callback to extract metadata and build KafkaMessage
            KafkaMessage kafkaMessage = callbackService.extractMessageMetadata(exchange);
            
            // Process the message
            processMessage(kafkaMessage);
            
            // Perform manual commit after successful processing
            performManualCommit(exchange, kafkaMessage);
            
            log.debug("Successfully completed processing for message: {}", kafkaMessage.getId());
            
        } catch (Exception e) {
            log.error("Error processing Kafka message", e);
            // Try to extract basic message info for error reporting
            try {
                KafkaMessage errorMessage = callbackService.extractMessageMetadata(exchange);
                callbackService.onMessageError(errorMessage, e);
            } catch (Exception extractionError) {
                log.error("Failed to extract message metadata for error reporting", extractionError);
            }
            throw e;
        }
    }

    /**
     * Performs manual commit for the Kafka message.
     * In Camel 4.x, manual commit is handled by setting exchange properties.
     * 
     * @param exchange The Camel exchange
     * @param kafkaMessage The processed message
     */
    private void performManualCommit(Exchange exchange, KafkaMessage kafkaMessage) {
        try {
            // Set manual commit property to trigger commit after processing
            exchange.setProperty(KafkaConstants.MANUAL_COMMIT, true);
            callbackService.onManualCommit(kafkaMessage);
            log.debug("Manual commit scheduled for message: {}", kafkaMessage.getId());
        } catch (Exception e) {
            log.error("Error during manual commit for message: {}", kafkaMessage.getId(), e);
            callbackService.onCommitError(kafkaMessage, e);
            throw new RuntimeException("Manual commit failed", e);
        }
    }

    /**
     * Process the business logic for the Kafka message.
     * 
     * @param kafkaMessage The message to process
     */
    private void processMessage(KafkaMessage kafkaMessage) {
        log.info("Processing message with ID: {}", kafkaMessage.getId());
        
        // Add message to dashboard
        dashboardService.addMessage(kafkaMessage);
        
        // Simulate message processing
        try {
            // Example business logic - you can replace this with actual processing
            if (kafkaMessage.getContent() != null && kafkaMessage.getContent().trim().isEmpty()) {
                throw new IllegalArgumentException("Message content cannot be empty");
            }
            
            // Simulate processing time
            Thread.sleep(100);
            
            log.info("Successfully processed message: {} with content length: {}", 
                    kafkaMessage.getId(), kafkaMessage.getContent().length());
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Processing interrupted", e);
        }
    }
}
