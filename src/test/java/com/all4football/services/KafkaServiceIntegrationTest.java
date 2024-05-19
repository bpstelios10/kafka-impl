package com.all4football.services;

import com.all4football.kafka.KafkaConsumerClient;
import com.all4football.kafka.KafkaProducerClient;
import com.all4football.kafka.KafkaProperties;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

public class KafkaServiceIntegrationTest {

    // To avoid flakyness we can consume before test starts but that is redundant for the scope of this project
    @Test
    public void testConsumeMessages() throws Exception {
        KafkaProperties kafkaProperties = new KafkaProperties();
        kafkaProperties.setBootstrapServers("localhost:9093,localhost:9094");
        KafkaConsumerClient kafkaConsumerClient = new KafkaConsumerClient(kafkaProperties);
        KafkaProducerClient kafkaProducerClient = new KafkaProducerClient(kafkaProperties);
        String testTopic = "my_test_topic";

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() -> kafkaProducerClient.produceMessages(StringSerializer.class.getName(), testTopic));

            CompletableFuture<List<String>> consumedMessagesFuture = CompletableFuture.supplyAsync(
                    () -> kafkaConsumerClient.subscribeAndConsume(StringDeserializer.class.getName(), testTopic),
                    executor);

            executor.submit(() -> kafkaProducerClient.produceMessages(StringSerializer.class.getName(), testTopic));

            consumedMessagesFuture.join();
            List<String> consumedMessages = consumedMessagesFuture.get();

            assertThat(consumedMessages).hasSize(20);
//            assertThat(consumedMessages).contains(
//                    "Consumer Record:([1], [My message from kafka producer, number: 1], [0], [11]",
//                    "Consumer Record:([9], [My message from kafka producer, number: 9], [0], [19]");
        }
    }
}