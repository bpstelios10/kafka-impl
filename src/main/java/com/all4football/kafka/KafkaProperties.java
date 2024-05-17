package com.all4football.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "app.kafka")
public class KafkaProperties {
    String bootstrapServers;
}
