package com.all4football.web;

import static org.springframework.http.ResponseEntity.internalServerError;
import static org.springframework.http.ResponseEntity.ok;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.all4football.services.KafkaService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/kafka-producer")
public class KafkaProducerController {

    private final KafkaService kafkaService;

    public KafkaProducerController(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    @GetMapping("/status")
    public ResponseEntity<String> getStatus() {
        return ok().body("ok");
    }

    @GetMapping("/produce-messages")
    public ResponseEntity<String> produceMessages() {
        try {
            kafkaService.produceMessages();

            return ok().body("success!!");
        } catch (Exception ex) {
            log.error("error :o", ex);

            return internalServerError().body("failed to produce messages");
        }
    }
}
