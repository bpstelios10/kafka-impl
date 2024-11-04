package org.learnings.all4football.producer.services;

import org.apache.kafka.common.serialization.StringSerializer;
import org.learnings.all4football.producer.kafka.KafkaProducerClient;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private static final String CUSTOM_TOPIC = "my_custom_topic";

    private final KafkaProducerClient kafkaProducerClient;

    public KafkaProducerService(KafkaProducerClient kafkaProducerClient) {
        this.kafkaProducerClient = kafkaProducerClient;
    }

    public void produceMessages() {
        kafkaProducerClient.produceMessages(StringSerializer.class.getName(), CUSTOM_TOPIC);
    }
}
