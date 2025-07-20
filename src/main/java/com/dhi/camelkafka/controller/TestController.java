package com.dhi.camelkafka.controller;

import com.dhi.camelkafka.model.KafkaMessage;
import com.dhi.camelkafka.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Test controller for adding sample messages to demonstrate the dashboard.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestController {

    private final DashboardService dashboardService;

    /**
     * Add sample messages for testing the dashboard.
     */
    @PostMapping("/add-sample-messages")
    public ResponseEntity<String> addSampleMessages() {
        // Add some sample messages to demonstrate the dashboard
        for (int i = 1; i <= 5; i++) {
            KafkaMessage message = KafkaMessage.builder()
                    .id("test-message-" + i)
                    .content("This is sample message number " + i + " for testing the dashboard functionality.")
                    .topic("json-purchase")
                    .partition(0)
                    .offset((long) (100 + i))
                    .timestamp(System.currentTimeMillis() - (i * 60000)) // Messages from last 5 minutes
                    .messageKey("key-" + i)
                    .processedAt(LocalDateTime.now().minusMinutes(i))
                    .build();
            
            dashboardService.addMessage(message);
        }
        
        // Add some messages from different topic and partition
        for (int i = 1; i <= 3; i++) {
            KafkaMessage message = KafkaMessage.builder()
                    .id("test-analytics-" + i)
                    .content("{\"event\": \"user_click\", \"user_id\": " + (1000 + i) + ", \"timestamp\": \"" + LocalDateTime.now() + "\"}")
                    .topic("user-analytics")
                    .partition(i % 2)
                    .offset((long) (200 + i))
                    .timestamp(System.currentTimeMillis() - (i * 30000)) // Messages from last 3 minutes
                    .messageKey("user-" + (1000 + i))
                    .processedAt(LocalDateTime.now().minusMinutes(i / 2))
                    .build();
            
            dashboardService.addMessage(message);
        }
        
        log.info("Added 8 sample messages to the dashboard");
        return ResponseEntity.ok("Successfully added 8 sample messages to the dashboard");
    }
}
