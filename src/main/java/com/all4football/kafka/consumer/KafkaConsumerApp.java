package com.all4football.kafka.consumer;

import com.all4football.kafka.KafkaProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static java.util.Collections.singletonList;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;
import static org.apache.kafka.common.config.SslConfigs.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@RequestMapping("/kafka-consumer")
public class KafkaConsumerApp {
    private static final String CUSTOM_TOPIC = "my_custom_topic";

    private final KafkaProperties kafkaProperties;

    public KafkaConsumerApp(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    public static void main(String... args) {
        KafkaProperties kafkaProperties = new KafkaProperties();
        kafkaProperties.setBootstrapServers("localhost:9093,localhost:9094");
        KafkaConsumerApp kafkaConsumer = new KafkaConsumerApp(kafkaProperties);

        kafkaConsumer.consumeMessages();
    }

    @GetMapping("/status")
    public ResponseEntity<String> consumerStatus() {
        return ok().body("ok");
    }

    @GetMapping("/consume-messages")
    public ResponseEntity<String> consumeMessages() {
        try (KafkaConsumer<String, String> kafkaConsumer =
                     new KafkaConsumer<String, String>(getKafkaConsumerProperties(StringDeserializer.class.getName()))) {
            log.info("checking connection to kafka");
            Map<String, List<PartitionInfo>> listTopics = kafkaConsumer.listTopics();
            log.info("Topics list:");
            listTopics.forEach((k, v) -> log.info("key: " + k + ", value: " + v));
            log.info("connection to kafka was succeeded");

            subscribeToTopic(kafkaConsumer, CUSTOM_TOPIC);
//            assignPartitions(kafkaxConsumer);

            runConsumer(kafkaConsumer);

            return ok().body("check logs");
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(ex.getLocalizedMessage());
        }
    }

    @GetMapping("/consume-streamed-messages")
    public ResponseEntity<String> consumeStreamedMessages() {
        try (KafkaConsumer<String, Long> kafkaConsumer =
                     new KafkaConsumer<String, Long>(getKafkaConsumerProperties(LongDeserializer.class.getName()))) {
            log.info("checking connection to kafka");
            Map<String, List<PartitionInfo>> listTopics = kafkaConsumer.listTopics();
            log.info("Topics list:");
            listTopics.forEach((k, v) -> log.info("key: " + k + ", value: " + v));
            log.info("connection to kafka was succeeded");

            subscribeToTopic(kafkaConsumer, "outputTopic");
//            assignPartitions(kafkaxConsumer);

            runConsumer(kafkaConsumer);

            return ok().body("check logs");
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(ex.getLocalizedMessage());
        }
    }

    private Properties getKafkaConsumerProperties(String deserializerClassName) {
        Properties properties = new Properties();
        properties.put(BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        properties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(VALUE_DESERIALIZER_CLASS_CONFIG, deserializerClassName);
        properties.put(GROUP_ID_CONFIG, "KafkaExampleConsumer");
        properties.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
//        properties.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 10);
//        properties.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 1000);
//        properties.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, (50 * 1024 * 1024));
        //-------- SSL ------------//
        properties.put(SSL_KEYSTORE_LOCATION_CONFIG, "/client-certs/client.keystore.jks");
        properties.put(SSL_KEYSTORE_PASSWORD_CONFIG, "password");
        properties.put(SSL_TRUSTSTORE_LOCATION_CONFIG, "/client-certs/client.truststore.jks");
        properties.put(SSL_TRUSTSTORE_PASSWORD_CONFIG, "password");
        properties.put(SSL_KEY_PASSWORD_CONFIG, "password");
        properties.put(SECURITY_PROTOCOL_CONFIG, "SSL");
        properties.put(SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");

        return properties;
    }

    private static void subscribeToTopic(KafkaConsumer<String, ?> kafkaConsumer, String topic) {
        log.info("kafka consumer subscribing to topic");
        List<String> topics = singletonList(topic);
        kafkaConsumer.subscribe(topics);
    }

    private static void assignPartitions(KafkaConsumer<String, String> kafkaConsumer) {
        TopicPartition topicPartition = new TopicPartition("my_test_topic", 0);
        List<TopicPartition> topicPartitions = singletonList(topicPartition);
        kafkaConsumer.assign(topicPartitions);
    }

    private static void runConsumer(Consumer<String, ?> consumer) {
        log.info("kafka consumer start");

        long end = System.currentTimeMillis() + 30000;
        while (System.currentTimeMillis() < end) {
            final ConsumerRecords<String, ?> consumerRecords = consumer.poll(Duration.ofMillis(200));

            consumerRecords.forEach(record ->
                    log.info("Consumer Record:([{}], [{}], [{}], [{}]",
                            record.key(), record.value(), record.partition(), record.offset())
            );
            consumer.commitAsync();
        }

        log.info("DONE");
    }
}