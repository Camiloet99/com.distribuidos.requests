package com.distribuidos.requests.services;

import com.distribuidos.requests.config.KafkaConfiguration;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@AllArgsConstructor
public class KafkaListenerService {

    private final RequestsService requestsService;
    private final KafkaConfiguration kafkaConfiguration;

    @KafkaListener(topics = "#{kafkaConfiguration.getKafkaTopic()}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenToKafka(Map<String, Object> message) {
        requestsService.test(message);
    }
}