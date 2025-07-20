package com.dhi.camelkafka.service;

import com.dhi.camelkafka.model.KafkaMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.component.kafka.KafkaConstants;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Callback service for processing Kafka message metadata and tracking statistics.
 */
@Slf4j
@Service
public class KafkaCallbackService {
    
    private final AtomicLong processedCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    
    /**
     * Callback method to extract and log Kafka message metadata.
     * 
     * @param exchange The Camel exchange containing the message
     * @return KafkaMessage with extracted metadata
     */
    public KafkaMessage extractMessageMetadata(Exchange exchange) {
        try {
            String topic = exchange.getIn().getHeader(KafkaConstants.TOPIC, String.class);
            Integer partition = exchange.getIn().getHeader(KafkaConstants.PARTITION, Integer.class);
            Long offset = exchange.getIn().getHeader(KafkaConstants.OFFSET, Long.class);
            Long timestamp = exchange.getIn().getHeader(KafkaConstants.TIMESTAMP, Long.class);
            String messageKey = exchange.getIn().getHeader(KafkaConstants.KEY, String.class);
            String content = exchange.getIn().getBody(String.class);
            
            KafkaMessage kafkaMessage = KafkaMessage.builder()
                    .id(String.format("%s-%d-%d", topic, partition, offset))
                    .content(content)
                    .topic(topic)
                    .partition(partition)
                    .offset(offset)
                    .timestamp(timestamp)
                    .messageKey(messageKey)
                    .processedAt(LocalDateTime.now())
                    .build();
            
            logMessageInfo(kafkaMessage);
            processedCount.incrementAndGet();
            
            return kafkaMessage;
        } catch (Exception e) {
            log.error("Error extracting message metadata", e);
            errorCount.incrementAndGet();
            throw e;
        }
    }
    
    /**
     * Success callback after message processing.
     * 
     * @param kafkaMessage The processed message
     */
    public void onMessageProcessed(KafkaMessage kafkaMessage) {
        log.info("Successfully processed message: {}", kafkaMessage.getFormattedInfo());
        logStatistics();
    }
    
    /**
     * Error callback when message processing fails.
     * 
     * @param kafkaMessage The message that failed processing
     * @param error The error that occurred
     */
    public void onMessageError(KafkaMessage kafkaMessage, Throwable error) {
        log.error("Failed to process message: {} - Error: {}", 
                kafkaMessage.getFormattedInfo(), error.getMessage());
        errorCount.incrementAndGet();
        logStatistics();
    }
    
    /**
     * Callback for manual commit confirmation.
     * 
     * @param kafkaMessage The message that was committed
     */
    public void onManualCommit(KafkaMessage kafkaMessage) {
        log.debug("Manual commit confirmed for message: {}", kafkaMessage.getFormattedInfo());
    }
    
    /**
     * Called when a manual commit fails.
     * 
     * @param kafkaMessage The message for which commit failed
     * @param error The commit error
     */
    public void onCommitError(KafkaMessage kafkaMessage, Exception error) {
        errorCount.incrementAndGet();
        log.error("Manual commit failed for message: {} from topic: {}, partition: {}, offset: {}. Error: {}", 
                kafkaMessage.getId(), kafkaMessage.getTopic(), kafkaMessage.getPartition(), 
                kafkaMessage.getOffset(), error.getMessage(), error);
    }
    
    private void logMessageInfo(KafkaMessage kafkaMessage) {
        log.info("Received message - ID: {}, {}, Content length: {}", 
                kafkaMessage.getId(),
                kafkaMessage.getFormattedInfo(),
                kafkaMessage.getContent() != null ? kafkaMessage.getContent().length() : 0);
    }
    
    private void logStatistics() {
        if (processedCount.get() % 100 == 0) { // Log stats every 100 messages
            log.info("Processing statistics - Processed: {}, Errors: {}", 
                    processedCount.get(), errorCount.get());
        }
    }
    
    public long getProcessedCount() {
        return processedCount.get();
    }
    
    public long getErrorCount() {
        return errorCount.get();
    }
}
