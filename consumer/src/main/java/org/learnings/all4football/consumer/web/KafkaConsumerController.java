package org.learnings.all4football.consumer.web;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.ResponseEntity.ok;

import java.util.List;

import org.learnings.all4football.consumer.services.KafkaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/kafka-consumer")
public class KafkaConsumerController {

    private final KafkaService kafkaService;
    private final ObjectMapper objectMapper;

    public KafkaConsumerController(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
        objectMapper = new ObjectMapper();
    }

    @GetMapping("/status")
    public ResponseEntity<String> consumerStatus() {
        return ok().body("ok");
    }

    @GetMapping("/consume-messages")
    public ResponseEntity<String> consumeMessages() {
        try {
            List<String> messages = kafkaService.consumeMessages();

            return ok().body(objectMapper.writeValueAsString(messages));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(ex.getLocalizedMessage());
        }
    }

    @GetMapping("/consume-streamed-messages")
    public ResponseEntity<String> consumeStreamedMessages() {
        try {
            List<String> messages = kafkaService.consumeStreamedMessages();

            return ok().body(objectMapper.writeValueAsString(messages));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(ex.getLocalizedMessage());
        }
    }
}
