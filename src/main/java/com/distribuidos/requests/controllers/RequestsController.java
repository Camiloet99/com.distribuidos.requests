package com.distribuidos.requests.controllers;

import com.distribuidos.requests.models.ResponseBody;
import com.distribuidos.requests.services.RequestsService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
@RequestMapping("/")
public class RequestsController {

    private final RequestsService service;

    @PostMapping("/transfer/user/{userId}/operator/{operatorId}")
    public Mono<ResponseEntity<ResponseBody<?>>> requestGuestTransfer(
            @PathVariable String userId,
            @PathVariable String operatorId) {

        return service.handleGuestsRequest(userId, operatorId)
                .map(ControllerUtils::ok);
    }

}
