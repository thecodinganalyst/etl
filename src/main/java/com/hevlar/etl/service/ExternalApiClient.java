package com.hevlar.etl.service;

import com.hevlar.etl.external.ExternalRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExternalApiClient {
    private final WebClient webClient;

    public Flux<ExternalRecord> fetchRecords() {
        return webClient
                .get()
                .uri("/posts")
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("No body")
                                .flatMap(body -> {
                                    HttpStatusCode statusCode = clientResponse.statusCode();
                                    String reasonPhrase = statusCode instanceof HttpStatus httpStatus
                                            ? httpStatus.getReasonPhrase()
                                            : statusCode.toString();
                                    return Mono.error(new WebClientResponseException(
                                            "Failed to fetch records: " + statusCode + " - " + body,
                                            statusCode.value(),
                                            reasonPhrase,
                                            clientResponse.headers().asHttpHeaders(),
                                            null,
                                            null));
                                }))
                .bodyToFlux(ExternalRecord.class)
                .doOnSubscribe(subscription -> log.info("Fetching records from external API"))
                .doOnNext(record -> log.debug("Received record {}", record))
                .doOnError(throwable -> log.error("Error fetching records", throwable));
    }
}
