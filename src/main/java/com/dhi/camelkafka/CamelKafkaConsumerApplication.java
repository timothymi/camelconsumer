package com.dhi.camelkafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Apache Camel Kafka Consumer with manual commits and callbacks.
 */
@SpringBootApplication
public class CamelKafkaConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CamelKafkaConsumerApplication.class, args);
    }
}
