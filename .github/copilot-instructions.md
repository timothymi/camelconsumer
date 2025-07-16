<!-- Use this file to provide workspace-specific custom instructions to Copilot. For more details, visit https://code.visualstudio.com/docs/copilot/copilot-customization#_use-a-githubcopilotinstructionsmd-file -->

# Camel Kafka Consumer Project

This project demonstrates an Apache Camel Kafka consumer with Spring Boot that features:

## Key Features
- **Manual Commits**: Kafka messages are manually committed after successful processing
- **Callback System**: Comprehensive callbacks for tracking topic, partition, offset, and timestamp
- **Lombok Integration**: Using Lombok for clean, boilerplate-free code
- **Structured Logging**: SLF4J with logback for comprehensive logging
- **Error Handling**: Robust error handling with callbacks and retry mechanisms

## Architecture
- `CamelKafkaConsumerApplication`: Main Spring Boot application
- `KafkaProperties`: Configuration properties for Kafka settings
- `KafkaMessage`: Lombok-based model for message representation
- `KafkaCallbackService`: Service for handling callbacks and metadata extraction
- `KafkaMessageProcessor`: Main processor with manual commit logic
- `KafkaConsumerRoute`: Camel route configuration with error handling

## Development Guidelines
- Use Lombok annotations for data classes
- Implement proper error handling with callbacks
- Follow the callback pattern for tracking message metadata
- Use manual commits for reliable message processing
- Include comprehensive logging for monitoring and debugging

## Configuration
- Configure Kafka settings in `application.properties`
- Manual commits are enabled by default
- Error handling includes retry mechanisms and dead letter patterns
