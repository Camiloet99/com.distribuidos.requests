package com.distribuidos.requests.controllers;

import com.distribuidos.requests.config.KafkaConfiguration;
import com.distribuidos.requests.models.ResponseBody;
import com.distribuidos.requests.models.TransferPushRequest;
import com.distribuidos.requests.models.TransferRequest;
import com.distribuidos.requests.services.RequestsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/")
public class RequestsController {

    private final RequestsService service;
    private final KafkaTemplate<String, TransferPushRequest> kafkaTemplate;
    private final KafkaConfiguration kafkaConfiguration;

    @PostMapping("/transfer")
    public Mono<ResponseEntity<ResponseBody<Boolean>>> requestGuestTransfer(
            @RequestBody TransferRequest request) {

        return service.handleGuestsRequest(request)
                .map(ControllerUtils::ok);
    }

    @PostMapping("/api/transfer")
    public Mono<ResponseEntity<ResponseBody<Boolean>>> handleGuestPushToKafka(
            @RequestBody TransferPushRequest transferPushRequest) {

        log.info("Pushing info from user " + transferPushRequest.getId() + " to kafka queue");
        kafkaTemplate.send(kafkaConfiguration.getKafkaTopic(), transferPushRequest);

        return Mono.just(ControllerUtils.ok(true));
    }

}
