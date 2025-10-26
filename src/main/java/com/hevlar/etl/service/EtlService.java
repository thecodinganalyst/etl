package com.hevlar.etl.service;

import com.hevlar.etl.domain.RecordEntity;
import com.hevlar.etl.external.ExternalRecord;
import com.hevlar.etl.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class EtlService {

    private final ExternalApiClient externalApiClient;
    private final RecordTransformer recordTransformer;
    private final RecordRepository recordRepository;

    public Mono<Long> runEtl() {
        return externalApiClient.fetchRecords()
                .map(this::transform)
                .flatMap(this::upsertRecord)
                .count()
                .doOnSubscribe(subscription -> log.info("Starting ETL job"))
                .doOnSuccess(count -> log.info("ETL job completed. Persisted {} records", count))
                .doOnError(throwable -> log.error("ETL job failed", throwable));
    }

    private RecordEntity transform(ExternalRecord record) {
        return recordTransformer.transform(record);
    }

    private Mono<RecordEntity> upsertRecord(RecordEntity candidate) {
        if (candidate.getExternalId() == null) {
            return recordRepository.save(candidate);
        }

        return recordRepository.findByExternalId(candidate.getExternalId())
                .map(existing -> {
                    existing.setTitle(candidate.getTitle());
                    existing.setContent(candidate.getContent());
                    existing.setSource(candidate.getSource());
                    return existing;
                })
                .defaultIfEmpty(candidate)
                .flatMap(recordRepository::save);
    }
}
