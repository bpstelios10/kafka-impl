package org.learnings.all4football.streams.web;

import static org.springframework.http.ResponseEntity.ok;

import org.learnings.all4football.streams.services.KafkaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

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
