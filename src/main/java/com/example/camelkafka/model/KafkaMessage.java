package com.example.camelkafka.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Message model for Kafka consumer processing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KafkaMessage {
    private String id;
    private String content;
    private String topic;
    private Integer partition;
    private Long offset;
    private Long timestamp;
    private LocalDateTime processedAt;
    
    public String getFormattedInfo() {
        return String.format("Topic: %s, Partition: %d, Offset: %d, Timestamp: %d", 
                           topic, partition, offset, timestamp);
    }
}
