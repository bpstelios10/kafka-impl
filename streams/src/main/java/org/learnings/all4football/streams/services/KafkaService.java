package org.learnings.all4football.streams.services;

import org.learnings.all4football.streams.kafka.KafkaStreamClient;
import org.springframework.stereotype.Service;

@Service
public class KafkaService {

    private static final String CUSTOM_TOPIC = "my_custom_topic";

    private final KafkaStreamClient kafkaStreamClient;

    public KafkaService(KafkaStreamClient kafkaStreamClient) {
        this.kafkaStreamClient = kafkaStreamClient;
    }

    public String processMessages() {
        String outputTopic = "outputTopic";

        kafkaStreamClient.processMessages(CUSTOM_TOPIC, outputTopic);

        return outputTopic;
    }
}
