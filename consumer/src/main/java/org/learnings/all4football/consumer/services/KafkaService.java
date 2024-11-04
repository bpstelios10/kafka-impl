package org.learnings.all4football.consumer.services;

import java.util.List;

import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.learnings.all4football.consumer.kafka.KafkaConsumerClient;
import org.springframework.stereotype.Service;

@Service
public class KafkaService {

    private static final String CUSTOM_TOPIC = "my_custom_topic";

    private final KafkaConsumerClient kafkaConsumerClient;

    public KafkaService(KafkaConsumerClient kafkaConsumerClient) {
        this.kafkaConsumerClient = kafkaConsumerClient;
    }

    public List<String> consumeMessages() {
        return kafkaConsumerClient.subscribeAndConsume(StringDeserializer.class.getName(), CUSTOM_TOPIC);
    }

    public List<String> consumeStreamedMessages() {
        return kafkaConsumerClient.subscribeAndConsume(LongDeserializer.class.getName(), "outputTopic");
    }
}
