package org.learnings.all4football.streams.kafka;

import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.common.config.SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG;
import static org.apache.kafka.common.config.SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG;
import static org.apache.kafka.common.config.SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG;
import static org.apache.kafka.common.config.SslConfigs.SSL_KEY_PASSWORD_CONFIG;
import static org.apache.kafka.common.config.SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG;
import static org.apache.kafka.common.config.SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Properties;

import org.springframework.util.ResourceUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaClient {

    protected final KafkaProperties kafkaProperties;

    public KafkaClient(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    protected static void configureSSL(Properties properties) {
        properties.put(SSL_KEYSTORE_LOCATION_CONFIG, "/client-certs/client.keystore.jks");
        properties.put(SSL_KEYSTORE_PASSWORD_CONFIG, "password");
        properties.put(SSL_TRUSTSTORE_LOCATION_CONFIG, "/client-certs/client.truststore.jks");
        properties.put(SSL_TRUSTSTORE_PASSWORD_CONFIG, "password");
        properties.put(SSL_KEY_PASSWORD_CONFIG, "password");
        properties.put(SECURITY_PROTOCOL_CONFIG, "SSL");
        properties.put(SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");

        if (!fileExists(properties)) {
            try {
                properties.put(SSL_KEYSTORE_LOCATION_CONFIG, ResourceUtils.getFile("classpath:kafka/client-certs/client.local.keystore.jks").getAbsolutePath());
                properties.put(SSL_TRUSTSTORE_LOCATION_CONFIG, ResourceUtils.getFile("classpath:kafka/client-certs/client.local.truststore.jks").getAbsolutePath());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static boolean fileExists(Properties properties) {
        boolean bothFilesExist;
        File file = new File(properties.getProperty(SSL_KEYSTORE_LOCATION_CONFIG));
        log.info("keystore exists? " + file.exists());
        bothFilesExist = file.exists();
        file = new File(properties.getProperty(SSL_TRUSTSTORE_LOCATION_CONFIG));
        log.info("truststore exists? " + file.exists());
        bothFilesExist = bothFilesExist && file.exists();

        return bothFilesExist;
    }
}
