package com.hevlar.etl.service;

import com.hevlar.etl.domain.RecordEntity;
import com.hevlar.etl.external.ExternalRecord;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Objects;

@Component
public class RecordTransformer {

    public RecordEntity transform(ExternalRecord externalRecord) {
        Objects.requireNonNull(externalRecord, "externalRecord must not be null");
        String normalizedTitle = externalRecord.title() == null ? "" : externalRecord.title().strip().toUpperCase(Locale.ROOT);
        String normalizedBody = externalRecord.body() == null ? "" : externalRecord.body().strip();

        return RecordEntity.builder()
                .id(null)
                .externalId(externalRecord.id())
                .title(normalizedTitle)
                .content(normalizedBody)
                .source("JSON_PLACEHOLDER")
                .build();
    }
}
