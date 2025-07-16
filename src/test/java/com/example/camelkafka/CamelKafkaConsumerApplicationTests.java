package com.example.camelkafka;

import com.example.camelkafka.service.KafkaCallbackService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test class for the Camel Kafka Consumer application.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "kafka.bootstrap-servers=localhost:9092",
        "kafka.topic=test-topic"
})
class CamelKafkaConsumerApplicationTests {

    @Autowired
    private KafkaCallbackService callbackService;

    @Test
    void contextLoads() {
        assertNotNull(callbackService);
    }

    @Test
    void callbackServiceBeansAreInitialized() {
        assertNotNull(callbackService);
        // Verify initial state
        assert(callbackService.getProcessedCount() == 0);
        assert(callbackService.getErrorCount() == 0);
    }
}
