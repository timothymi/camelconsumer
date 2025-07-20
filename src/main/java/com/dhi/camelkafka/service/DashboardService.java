package com.dhi.camelkafka.service;

import com.dhi.camelkafka.model.KafkaMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service to manage and provide access to recent Kafka messages for the dashboard.
 */
@Slf4j
@Service
public class DashboardService {
    
    private static final int MAX_MESSAGES = 100; // Keep last 100 messages
    
    // Thread-safe list to store recent messages
    private final CopyOnWriteArrayList<KafkaMessage> recentMessages = new CopyOnWriteArrayList<>();
    
    /**
     * Add a new message to the dashboard.
     * Maintains only the most recent MAX_MESSAGES.
     * 
     * @param message The Kafka message to add
     */
    public void addMessage(KafkaMessage message) {
        recentMessages.add(0, message); // Add to beginning of list
        
        // Remove old messages if we exceed the limit
        while (recentMessages.size() > MAX_MESSAGES) {
            recentMessages.remove(recentMessages.size() - 1);
        }
        
        log.debug("Added message to dashboard: {}", message.getId());
    }
    
    /**
     * Get all recent messages for the dashboard.
     * 
     * @return List of recent Kafka messages (most recent first)
     */
    public List<KafkaMessage> getRecentMessages() {
        return new ArrayList<>(recentMessages);
    }
    
    /**
     * Get messages for a specific topic.
     * 
     * @param topic The topic to filter by
     * @return List of messages for the specified topic
     */
    public List<KafkaMessage> getMessagesByTopic(String topic) {
        return recentMessages.stream()
                .filter(message -> topic.equals(message.getTopic()))
                .toList();
    }
    
    /**
     * Get the count of messages processed.
     * 
     * @return Total number of messages in the dashboard
     */
    public int getMessageCount() {
        return recentMessages.size();
    }
    
    /**
     * Clear all messages from the dashboard.
     */
    public void clearMessages() {
        recentMessages.clear();
        log.info("Cleared all messages from dashboard");
    }
    
    /**
     * Get dashboard statistics.
     * 
     * @return Dashboard statistics
     */
    public DashboardStats getStats() {
        return DashboardStats.builder()
                .totalMessages(recentMessages.size())
                .topicsCount(recentMessages.stream()
                        .map(KafkaMessage::getTopic)
                        .distinct()
                        .count())
                .partitionsCount(recentMessages.stream()
                        .map(msg -> msg.getTopic() + "-" + msg.getPartition())
                        .distinct()
                        .count())
                .build();
    }
    
    /**
     * Statistics for the dashboard.
     */
    @lombok.Data
    @lombok.Builder
    public static class DashboardStats {
        private int totalMessages;
        private long topicsCount;
        private long partitionsCount;
    }
}
