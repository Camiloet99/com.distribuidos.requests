package com.distribuidos.requests.services.facades.documents;

import com.distribuidos.requests.config.EnvironmentConfig;
import com.distribuidos.requests.exceptions.CentralizerGetOperatorsException;
import com.distribuidos.requests.exceptions.DocumentsDownloadUpstreamException;
import com.distribuidos.requests.models.DocumentEntity;
import com.distribuidos.requests.models.ResponseBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.util.List;

import static com.distribuidos.requests.exceptions.ErrorCodes.DOCUMENTS_DOWNLOAD_EXCEPTION;
import static reactor.core.publisher.Mono.error;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentsFacade {

    private final EnvironmentConfig environmentConfig;
    private final WebClient webClient;

    private static final String GET_ALL_DOCUMENTS_URI = "/list/%s";
    private static final ParameterizedTypeReference<ResponseBody<List<DocumentEntity>>> RESPONSE_TYPE_DOCUMENTS =
            new ParameterizedTypeReference<>() {
            };

    public Mono<List<String>> getAllDocumentsFromUser(Long documentId) {

        String resourceUri = environmentConfig.getDomains().getDocumentsDomain()
                + String.format(GET_ALL_DOCUMENTS_URI, documentId);

        return webClient
                .get()
                .uri(resourceUri)
                .exchangeToMono(documentsResponse -> {

                    HttpStatus httpStatus = HttpStatus.valueOf(documentsResponse.statusCode().value());
                    if (HttpStatus.OK.equals(httpStatus)) {
                        return documentsResponse.bodyToMono(RESPONSE_TYPE_DOCUMENTS)
                                .map(ResponseBody::getResult)
                                .map(documentEntities -> documentEntities.stream()
                                        .map(DocumentEntity::getDownloadLink)
                                        .toList());
                    }

                    HttpHeaders responseHeaders = documentsResponse.headers().asHttpHeaders();
                    return documentsResponse.bodyToMono(String.class)
                            .flatMap(responseBody -> {
                                log.error("{} - The centralizer service responded with "
                                                + "an unexpected failure response for: {}"
                                                + "\nStatus Code: {}\nResponse Headers: {}\nResponse Body: {}",
                                        DOCUMENTS_DOWNLOAD_EXCEPTION, resourceUri, httpStatus, responseHeaders,
                                        responseBody);
                                return error(new DocumentsDownloadUpstreamException(responseBody));
                            });
                }).retryWhen(Retry
                        .max(environmentConfig.getServiceRetry().getMaxAttempts())
                        .filter(CentralizerGetOperatorsException.class::isInstance)
                        .onRetryExhaustedThrow((ignore1, ignore2) -> ignore2.failure()));
    }


}
