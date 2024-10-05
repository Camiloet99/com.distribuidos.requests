package com.distribuidos.requests.services;

import com.distribuidos.requests.config.EnvironmentConfig;
import com.distribuidos.requests.exceptions.CentralizerGetOperatorsException;
import com.distribuidos.requests.exceptions.ExternalPushUserException;
import com.distribuidos.requests.models.TransferPushRequest;
import com.distribuidos.requests.models.TransferRequest;
import com.distribuidos.requests.services.facades.centarlizer.CentralizerFacade;
import com.distribuidos.requests.services.facades.documents.DocumentsFacade;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.distribuidos.requests.exceptions.ErrorCodes.EXTERNAL_UPSTREAM_PUSH_ERROR;
import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.just;

@Slf4j
@Service
@AllArgsConstructor
public class RequestsService {

    private CentralizerFacade centralizerFacade;
    private final EnvironmentConfig environmentConfig;
    private DocumentsFacade documentsFacade;
    private static final String DELETE_DOCUMENTS_ENDPOINT = "/delete/all/%s";
    private final WebClient webClient;
    private static final Integer MAX_EXTERNAL_RETRIES = 2;

    private Map<String, List<String>> mapListToDocumentsMap(List<String> documents) {
        Map<String, List<String>> documentsMap = new HashMap<>();

        for (int i = 0; i < documents.size(); i++) {
            String documentKey = "Document" + (i + 1);
            documentsMap.put(documentKey, List.of(documents.get(i)));
        }

        return documentsMap;
    }

    private Mono<Boolean> pushElementToOperatorUri(String operatorUri, TransferPushRequest request) {

        log.info("pushing documents " + request.toString() + " to operatorUri " + operatorUri);

        return webClient
                .post()
                .uri(operatorUri)
                .bodyValue(request)
                .exchangeToMono(transferResponse -> {
                    HttpStatus httpStatus = HttpStatus.valueOf(transferResponse.statusCode().value());
                    if (HttpStatus.OK.equals(httpStatus)) {
                        log.info("Ok status received from external operator");
                        return Mono.just(true);
                    }

                    HttpHeaders responseHeaders = transferResponse.headers().asHttpHeaders();
                    return transferResponse.bodyToMono(String.class)
                            .flatMap(responseBody -> {
                                log.error("{} - The extenrnal transfer endpoint for operator {}"
                                                + " service responded with "
                                                + "an unexpected failure response for: {}"
                                                + "\nStatus Code: {}\nResponse Headers: {}\nResponse Body: {}",
                                        operatorUri,
                                        EXTERNAL_UPSTREAM_PUSH_ERROR, operatorUri, httpStatus, responseHeaders,
                                        responseBody);
                                return error(new ExternalPushUserException(responseBody));
                            });
                }).retryWhen(Retry
                        .max(MAX_EXTERNAL_RETRIES)
                        .filter(CentralizerGetOperatorsException.class::isInstance)
                        .onRetryExhaustedThrow((ignore1, ignore2) -> ignore2.failure()));
    }

    public Mono<Boolean> handleGuestsRequest(TransferRequest request) {
        Long userId = request.getUserId();
        String operatorId = request.getOperatorId();

        log.info("Trying to transfer user " + userId + " to operatorId " + operatorId);

        Mono<String> centralizerApiEndpointMono = centralizerFacade.getOperatorTransferApi(operatorId);
        Mono<List<String>> userDocumentsMono = documentsFacade.getAllDocumentsFromUser(userId);

        return Mono.zip(centralizerApiEndpointMono, userDocumentsMono)
                .flatMap(data -> {
                    String apiEndpoint = data.getT1();
                    List<String> documents = data.getT2();

                    log.info("Transfer endpoint found for operator: " + apiEndpoint);

                    TransferPushRequest pushRequest = TransferPushRequest.builder()
                            .id(userId)
                            .documents(mapListToDocumentsMap(documents))
                            .citizenEmail(request.getCitizenEmail())
                            .citizenName(request.getCitizenName())
                            .confirmationURL(environmentConfig.getDomains().getDocumentsDomain()
                                    + String.format(DELETE_DOCUMENTS_ENDPOINT, request.getUserId()))
                            .build();
                    return pushElementToOperatorUri(apiEndpoint, pushRequest)
                            .flatMap(aBoolean -> just(true));
                });
    }

}
