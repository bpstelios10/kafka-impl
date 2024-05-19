package com.all4football.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static java.util.Collections.singletonList;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

@Slf4j
@Component
public class KafkaConsumerClient extends KafkaClient {

    public KafkaConsumerClient(KafkaProperties kafkaProperties) {
        super(kafkaProperties);
    }

    public List<String> subscribeAndConsume(String valueDeserializerClassName, String topic) {
        Properties kafkaConsumerProperties = getKafkaConsumerProperties(valueDeserializerClassName, kafkaProperties);
        configureSSL(kafkaConsumerProperties);

        try (KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(kafkaConsumerProperties)) {
            log.info("checking connection to kafka");
            Map<String, List<PartitionInfo>> listTopics = kafkaConsumer.listTopics();
            log.info("Topics list:");
            listTopics.forEach((k, v) -> log.info("key: " + k + ", value: " + v));
            log.info("connection to kafka was succeeded");

            subscribeToTopic(kafkaConsumer, topic);
            // assignPartitions(kafkaxConsumer);

            return runConsumer(kafkaConsumer);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    private static Properties getKafkaConsumerProperties(String valueDeserializerClassName, KafkaProperties kafkaProperties) {
        Properties properties = new Properties();
        properties.put(BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        properties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializerClassName);
        properties.put(GROUP_ID_CONFIG, "KafkaExampleConsumer");
        properties.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
        //        properties.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 10);
        //        properties.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 1000);
        //        properties.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, (50 * 1024 * 1024));

        return properties;
    }

    private static void subscribeToTopic(KafkaConsumer<String, ?> kafkaConsumer, String topic) {
        log.info("kafka consumer subscribing to topic: [{}]", topic);
        List<String> topics = singletonList(topic);
        kafkaConsumer.subscribe(topics);
    }

    private static void assignPartitions(KafkaConsumer<String, String> kafkaConsumer) {
        TopicPartition topicPartition = new TopicPartition("my_test_topic", 0);
        List<TopicPartition> topicPartitions = singletonList(topicPartition);
        kafkaConsumer.assign(topicPartitions);
    }

    private static List<String> runConsumer(Consumer<String, ?> consumer) {
        log.info("kafka consumer start");
        List<String> messages = new ArrayList<>();

        long end = System.currentTimeMillis() + 20000;
        while (System.currentTimeMillis() < end) {
            final ConsumerRecords<String, ?> consumerRecords = consumer.poll(Duration.ofMillis(500));

            consumerRecords.forEach(record -> {
                log.info("Consumer Record:([{}], [{}], [{}], [{}]", record.key(), record.value(), record.partition(), record.offset());
                messages.add(String.format("Consumer Record:([%s], [%s], [%s], [%s]", record.key(), record.value(), record.partition(), record.offset()));
            });
            consumer.commitAsync();
        }

        log.info("DONE");
        return messages;
    }
}
