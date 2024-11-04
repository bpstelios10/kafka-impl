package org.learnings.all4football.streams.kafka;

import static java.lang.Thread.sleep;
import static org.apache.kafka.streams.StreamsConfig.*;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KafkaStreamClient extends KafkaClient {

    public static final Pattern WORDS_PATTERN = Pattern.compile("\\W+", Pattern.UNICODE_CHARACTER_CLASS);

    public KafkaStreamClient(KafkaProperties kafkaProperties) {
        super(kafkaProperties);
    }

    public void processMessages(String topicToBeProcessed, String outputTopic) {
        Properties kafkaConsumerProperties = getKafkaStreamProperties(Serdes.String().getClass().getName(), kafkaProperties);
        configureSSL(kafkaConsumerProperties);

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Long> wordCounts = builder
                .stream(topicToBeProcessed, Consumed.with(Serdes.String(), Serdes.String()))
                .flatMapValues(value -> Arrays.asList(WORDS_PATTERN.split(value.toLowerCase())))
                .groupBy((key, word) -> word)
                .count(Materialized.as("counts-store"))
                .toStream();

        wordCounts.foreach((w, c) -> log.info("word: [{}] -> [{}]", w, c));

        publishResults(wordCounts, outputTopic);

        KafkaStreams streams = new KafkaStreams(builder.build(), kafkaConsumerProperties);
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

        closeStreamAfter(streams, Duration.ofSeconds(15));
    }

    private static Properties getKafkaStreamProperties(String valueSerdeClassName, KafkaProperties kafkaProperties) {
        Properties streamsConfiguration = new Properties();
        streamsConfiguration.put(BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        streamsConfiguration.put(APPLICATION_ID_CONFIG, "wordcount-live-test");
        streamsConfiguration.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, valueSerdeClassName);
        streamsConfiguration.put(STATE_DIR_CONFIG, "/tmp/my-kafka-streams");

        return streamsConfiguration;
    }

    private void closeStreamAfter(KafkaStreams streams, Duration duration) {
        try {
            sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            Thread.currentThread().interrupt();
        }
        streams.close(duration);
    }

    private void publishResults(KStream<String, Long> wordCounts, String outputTopic) {
        wordCounts.to(outputTopic, Produced.with(Serdes.String(), Serdes.Long()));
    }
}
