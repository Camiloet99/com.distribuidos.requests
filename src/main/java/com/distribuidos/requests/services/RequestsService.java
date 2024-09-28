package com.distribuidos.requests.services;

import com.distribuidos.requests.services.facades.centarlizer.CentralizerFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestsService {

    private CentralizerFacade centralizerFacade;

    public Mono<Boolean> handleGuestsRequest(String userId, String operatorId) {

        log.info("Trying to transfer user " + userId + " to operatorId " + operatorId);

        return centralizerFacade.getOperatorTransferApi(operatorId)
                .map(operatorTransferUri -> )

    }


}
