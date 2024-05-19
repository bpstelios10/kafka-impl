package com.all4football.services;

import com.all4football.kafka.KafkaConsumerClient;
import com.all4football.kafka.KafkaProducerClient;
import com.all4football.kafka.KafkaProperties;
import com.all4football.kafka.KafkaStreamClient;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * These tests need to connect to actual kafka brokers. A cluster needs to be manually set up or else comment them out.
 * <p>
 * NOTE: To avoid flakyness we can consume before test starts but that is redundant for the scope of this project,
 * because polling takes a long time right now. Later, it could become configurable and add beforeEach/afterEach to clear.
 */
public class KafkaServiceIntegrationTest {

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
            List<String> messagesSubstringOfStableCharacters = consumedMessages.stream().map(e -> e.substring(0, 60)).toList();
            assertThat(messagesSubstringOfStableCharacters).contains(
                    "Consumer Record:([1], [My message from kafka producer, numbe",
                    "Consumer Record:([9], [My message from kafka producer, numbe");
        }
    }

    @Test
    void testStreams() throws Exception {
        KafkaProperties kafkaProperties = new KafkaProperties();
        kafkaProperties.setBootstrapServers("localhost:9093,localhost:9094");
        KafkaConsumerClient kafkaConsumerClient = new KafkaConsumerClient(kafkaProperties);
        KafkaProducerClient kafkaProducerClient = new KafkaProducerClient(kafkaProperties);
        KafkaStreamClient kafkaStreamClient = new KafkaStreamClient(kafkaProperties);
        String testStreamsTopic = "my_test_streams_topic";
        String outputTopic = "outputTopic";

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() -> kafkaProducerClient.produceMessages(StringSerializer.class.getName(), testStreamsTopic));
            Future<?> streamThread = executor.submit(() -> kafkaStreamClient.processMessages(testStreamsTopic, outputTopic));
            executor.submit(() -> kafkaProducerClient.produceMessages(StringSerializer.class.getName(), testStreamsTopic));
            streamThread.get(16, SECONDS);

            CompletableFuture<List<String>> consumedMessagesFuture = CompletableFuture.supplyAsync(
                    () -> kafkaConsumerClient.subscribeAndConsume(LongDeserializer.class.getName(), outputTopic),
                    executor);

            consumedMessagesFuture.join();
            List<String> consumedMessages = consumedMessagesFuture.get();
            assertThat(consumedMessages).hasSize(15);
            List<String> messagesSubstringOfStableCharacters = consumedMessages.stream().map(e -> e.substring(0, 26)).toList();
            assertThat(messagesSubstringOfStableCharacters).contains("Consumer Record:([message]", "Consumer Record:([from], [");
        }
    }
}
