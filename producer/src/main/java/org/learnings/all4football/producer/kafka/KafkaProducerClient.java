package org.learnings.all4football.producer.kafka;

import static org.apache.kafka.clients.producer.ProducerConfig.*;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KafkaProducerClient extends KafkaClient {

    public KafkaProducerClient(KafkaProperties kafkaProperties) {
        super(kafkaProperties);
    }

    public void produceMessages(String valueSerializerClassName, String topic) {
        Properties kafkaConsumerProperties = getKafkaProducerProperties(valueSerializerClassName, kafkaProperties);
        configureSSL(kafkaConsumerProperties);

        try (KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(kafkaConsumerProperties)) {
            log.debug("kafka producer start");

            ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, "-----------------------");
            log.debug("before send");
            kafkaProducer.send(producerRecord);
            for (int i = 1; i < 10; i++) {
                final String index = String.valueOf(i);
                log.debug("start producing message number [{}]", i);
                kafkaProducer.send(createRecord(topic, index), (metadata, exception) -> {
                    if (metadata != null) log.info("Record sent with key [{}] and details [{}]", index, metadata);
                    if (exception != null) log.error("exception: [{}]", exception.getMessage(), exception);
                });
            }
            log.debug("done");
        } catch (Exception ex) {
            log.error("error :o", ex);
        }
    }

    private static Properties getKafkaProducerProperties(String valueSerializerClassName, KafkaProperties kafkaProperties) {
        log.info("kafka boot-strapers: [{}]", kafkaProperties.getBootstrapServers());

        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializerClassName);
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.RETRIES_CONFIG, "1");
        properties.put(DELIVERY_TIMEOUT_MS_CONFIG, "10000");
        properties.put(REQUEST_TIMEOUT_MS_CONFIG, "5000");
        properties.put(TRANSACTION_TIMEOUT_CONFIG, "5000");
        properties.put(CLIENT_ID_CONFIG, "External-Producer");
        //        properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432L);
        //        properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 600000L);
        //        properties.put(ProducerConfig.BATCH_SIZE_CONFIG, 3276800);}

        return properties;
    }

    private ProducerRecord<String, String> createRecord(String topic, String i) {
        return new ProducerRecord<>(topic, i, "My message from kafka producer, number: " + i);
    }
}
