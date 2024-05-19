package com.all4football.services;

import com.all4football.kafka.KafkaConsumerClient;
import com.all4football.kafka.KafkaProducerClient;
import com.all4football.kafka.KafkaStreamClient;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KafkaService {

    private static final String CUSTOM_TOPIC = "my_custom_topic";

    private final KafkaConsumerClient kafkaConsumerClient;
    private final KafkaProducerClient kafkaProducerClient;
    private final KafkaStreamClient kafkaStreamClient;

    public KafkaService(KafkaConsumerClient kafkaConsumerClient, KafkaProducerClient kafkaProducerClient, KafkaStreamClient kafkaStreamClient) {
        this.kafkaConsumerClient = kafkaConsumerClient;
        this.kafkaProducerClient = kafkaProducerClient;
        this.kafkaStreamClient = kafkaStreamClient;
    }

    public List<String> consumeMessages() {
        return kafkaConsumerClient.subscribeAndConsume(StringDeserializer.class.getName(), CUSTOM_TOPIC);
    }

    public List<String> consumeStreamedMessages() {
        return kafkaConsumerClient.subscribeAndConsume(LongDeserializer.class.getName(), "outputTopic");
    }

    public void produceMessages() {
        kafkaProducerClient.produceMessages(StringSerializer.class.getName(), CUSTOM_TOPIC);
    }

    public String processMessages() {
        String outputTopic = "outputTopic";

        kafkaStreamClient.processMessages(CUSTOM_TOPIC, outputTopic);

        return outputTopic;
    }
}
