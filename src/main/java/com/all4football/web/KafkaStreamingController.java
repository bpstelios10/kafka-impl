package com.all4football.web;

import com.all4football.services.KafkaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@RequestMapping("/kafka-stream")
public class KafkaStreamingController {

    private final KafkaService kafkaService;

    public KafkaStreamingController(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    @GetMapping("/status")
    public ResponseEntity<String> getStatus() {
        return ok().body("ok");
    }

    @GetMapping("/process-messages")
    public ResponseEntity<String> processMessages() {
        String outputTopic = kafkaService.processMessages();

        return ok().body("messages processed. Results published at topic: [" + outputTopic + "]");
    }
}
