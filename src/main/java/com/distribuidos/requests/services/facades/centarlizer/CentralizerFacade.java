package com.distribuidos.requests.services.facades.centarlizer;

import com.distribuidos.requests.config.EnvironmentConfig;
import com.distribuidos.requests.exceptions.CentralizerGetOperatorsException;
import com.distribuidos.requests.services.facades.centarlizer.models.OperatorsResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.util.List;
import java.util.stream.Collectors;

import static com.distribuidos.requests.exceptions.ErrorCodes.CENTRALIZER_OPERATORS_EXCEPTION;
import static reactor.core.publisher.Mono.error;

@Slf4j
@Component
@AllArgsConstructor
public class CentralizerFacade {

    private final WebClient webClient;
    private final EnvironmentConfig environmentConfig;

    private static final String OPERATORS_LIST_PATH = "/apis/";

    private static final ParameterizedTypeReference<List<OperatorsResponse>> RESPONSE_TYPE_OPERATORS =
            new ParameterizedTypeReference<>() {
            };

    public Mono<String> getOperatorTransferApi(String operatorId) {

        String resourceUri = environmentConfig.getDomains().getCentralizerDomain()
                + OPERATORS_LIST_PATH;

        return webClient
                .get()
                .uri(resourceUri)
                .exchangeToMono(operatorsResponse -> {
                    HttpStatus httpStatus = HttpStatus.valueOf(operatorsResponse.statusCode().value());

                    if (httpStatus.is2xxSuccessful()) {
                        return operatorsResponse.bodyToMono(RESPONSE_TYPE_OPERATORS)
                                .map(operatorsResponses -> operatorsResponses.stream()
                                        .filter(operator -> operator.get_id().equalsIgnoreCase(operatorId))
                                        .findFirst()
                                        .map(OperatorsResponse::getTransferAPIURL)
                                        .orElse(null));
                    }

                    HttpHeaders responseHeaders = operatorsResponse.headers().asHttpHeaders();
                    return operatorsResponse.bodyToMono(String.class)
                            .flatMap(responseBody -> {
                                log.error("{} - The centralizer service responded with "
                                                + "an unexpected failure response for: {}"
                                                + "\nStatus Code: {}\nResponse Headers: {}\nResponse Body: {}",
                                        CENTRALIZER_OPERATORS_EXCEPTION, resourceUri, httpStatus, responseHeaders,
                                        responseBody);
                                return error(new CentralizerGetOperatorsException(responseBody));
                            });
                })
                .retryWhen(Retry
                        .max(environmentConfig.getServiceRetry().getMaxAttempts())
                        .filter(CentralizerGetOperatorsException.class::isInstance)
                        .onRetryExhaustedThrow((ignore1, ignore2) -> ignore2.failure()));

    }

}
