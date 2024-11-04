package org.learnings.all4football.producer.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ConfigurationProperties(prefix = "app.kafka")
public class KafkaProperties {
    String bootstrapServers;
}
