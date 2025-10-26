package com.hevlar.etl.repository;

import com.hevlar.etl.domain.RecordEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface RecordRepository extends ReactiveCrudRepository<RecordEntity, Long> {
    Mono<RecordEntity> findByExternalId(Long externalId);
}
