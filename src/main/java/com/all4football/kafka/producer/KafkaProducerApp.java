package com.all4football.kafka.producer;

import com.all4football.kafka.KafkaProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.Properties;

import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.*;
import static org.apache.kafka.common.config.SslConfigs.*;
import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@RequestMapping("/kafka-producer")
public class KafkaProducerApp {
    private static final String CUSTOM_TOPIC = "my_custom_topic";

    private final KafkaProperties kafkaProperties;

    public KafkaProducerApp(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    public static void main(String... args) {
        KafkaProperties kafkaProperties = new KafkaProperties();
        kafkaProperties.setBootstrapServers("localhost:9093,localhost:9094");
        KafkaProducerApp kafkaProducer = new KafkaProducerApp(kafkaProperties);

        kafkaProducer.produceMessages();
    }

    @GetMapping("/status")
    public ResponseEntity<String> getStatus() {
        return ok().body("ok");
    }

    @GetMapping("/produce-messages")
    public ResponseEntity<String> produceMessages() {
        log.info("kafka properties: [{}]", kafkaProperties);
        log.info("kafka boot-strapers: [{}]", kafkaProperties.getBootstrapServers());

        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.RETRIES_CONFIG, "1");
        properties.put(DELIVERY_TIMEOUT_MS_CONFIG, "10000");
        properties.put(REQUEST_TIMEOUT_MS_CONFIG, "5000");
        properties.put(TRANSACTION_TIMEOUT_CONFIG, "5000");
        properties.put(CLIENT_ID_CONFIG, "External-Producer");
//        properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432L);
//        properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 600000L);
//        properties.put(ProducerConfig.BATCH_SIZE_CONFIG, 3276800);
        // -------- SSL --------- //
        properties.put(SSL_KEYSTORE_LOCATION_CONFIG, "/client-certs/client.keystore.jks");
        properties.put(SSL_KEYSTORE_PASSWORD_CONFIG, "password");
        properties.put(SSL_TRUSTSTORE_LOCATION_CONFIG, "/client-certs/client.truststore.jks");
        properties.put(SSL_TRUSTSTORE_PASSWORD_CONFIG, "password");
        properties.put(SSL_KEY_PASSWORD_CONFIG, "password");
        properties.put(SECURITY_PROTOCOL_CONFIG, "SSL");
        properties.put(SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
//        debugFileExists(properties);

        try (KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(properties)) {
            log.trace("kafka producer start");

            ProducerRecord<String, String> producerRecord = new ProducerRecord<>(CUSTOM_TOPIC, "-----------------------");
            log.trace("before send");
            kafkaProducer.send(producerRecord);
            for (int i = 1; i < 10; i++) {
                log.debug("start producing message number [{}]", i);
                RecordMetadata metadata = kafkaProducer
                        .send(
                                new ProducerRecord<>(CUSTOM_TOPIC, String.valueOf(i), "My message from kafka producer, number: " + i)
                                , (metadata1, exception) -> {
                                    log.debug("metatdata: [{}]", metadata1);
                                    if (exception != null)
                                        log.error("exception: [{}]", exception.getMessage(), exception);
                                }
                        )
                        .get();
                log.info("Record sent with key [{}] and details [{}]", i, metadata.toString());
            }
        } catch (Exception ex) {
            log.error("error :o", ex);
        }

        return ok().body("success!!");
    }

    private void debugFileExists(Properties properties) {
        File file = new File(properties.getProperty(SSL_KEYSTORE_LOCATION_CONFIG));
        log.info("keystore exists? " + file.exists());
        file = new File(properties.getProperty(SSL_TRUSTSTORE_LOCATION_CONFIG));
        log.info("truststore exists? " + file.exists());
    }
}
