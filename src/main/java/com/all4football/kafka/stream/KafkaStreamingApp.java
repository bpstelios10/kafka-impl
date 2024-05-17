package com.all4football.kafka.stream;

import com.all4football.kafka.KafkaProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.common.config.SslConfigs.*;
import static org.apache.kafka.common.config.SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.*;
import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@RequestMapping("/kafka-stream")
public class KafkaStreamingApp {
    public static final Pattern WORDS_PATTERN = Pattern.compile("\\W+", Pattern.UNICODE_CHARACTER_CLASS);
    private static final String CUSTOM_TOPIC = "my_custom_topic"; //TODO add topic as header param

    private final KafkaProperties kafkaProperties;

    public KafkaStreamingApp(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    public static void main(String... args) {
        KafkaProperties kafkaProperties = new KafkaProperties();
        kafkaProperties.setBootstrapServers("localhost:9093,localhost:9094");
        KafkaStreamingApp kafkaStreamingApp = new KafkaStreamingApp(kafkaProperties);

        kafkaStreamingApp.processMessages();
    }

    @GetMapping("/status")
    public ResponseEntity<String> getStatus() {
        return ok().body("ok");
    }

    @GetMapping("/process-messages")
    public ResponseEntity<String> processMessages() {
        String outputTopic = "outputTopic";
        Properties streamsConfiguration = getProperties();

        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, Long> wordCounts = builder
                .stream(CUSTOM_TOPIC, Consumed.with(Serdes.String(), Serdes.String()))
                .flatMapValues(value -> Arrays.asList(WORDS_PATTERN.split(value.toLowerCase())))
                .groupBy((key, word) -> word)
                .count(Materialized.as("counts-store"))
                .toStream();

        wordCounts.foreach((w, c) -> log.info("word: [{}] -> [{}]", w, c));

        publishResults(wordCounts, outputTopic);

        KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfiguration);
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

        closeStreamAfterMillis(streams, 30000);

        return ok().body("messages processed. Results published at topic: [" + outputTopic + "]");
    }

    private void closeStreamAfterMillis(KafkaStreams streams, int millis) {
        try {
            sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            Thread.currentThread().interrupt();
        }
        streams.close(Duration.ofSeconds(30));
    }

    private Properties getProperties() {
        Properties streamsConfiguration = new Properties();
        streamsConfiguration.put(BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        streamsConfiguration.put(APPLICATION_ID_CONFIG, "wordcount-live-test");
        streamsConfiguration.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(STATE_DIR_CONFIG, "/tmp/my-kafka-streams");
        // -------- SSL --------- //
        streamsConfiguration.put(SSL_KEYSTORE_LOCATION_CONFIG, "/client-certs/client.keystore.jks");
        streamsConfiguration.put(SSL_KEYSTORE_PASSWORD_CONFIG, "password");
        streamsConfiguration.put(SSL_TRUSTSTORE_LOCATION_CONFIG, "/client-certs/client.truststore.jks");
        streamsConfiguration.put(SSL_TRUSTSTORE_PASSWORD_CONFIG, "password");
        streamsConfiguration.put(SSL_KEY_PASSWORD_CONFIG, "password");
        streamsConfiguration.put(SECURITY_PROTOCOL_CONFIG, "SSL");
        streamsConfiguration.put(SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");

        return streamsConfiguration;
    }

    private void publishResults(KStream<String, Long> wordCounts, String outputTopic) {
        wordCounts.to(outputTopic, Produced.with(Serdes.String(), Serdes.Long()));
    }
}
