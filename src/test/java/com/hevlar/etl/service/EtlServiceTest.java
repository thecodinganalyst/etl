package com.hevlar.etl.service;

import com.hevlar.etl.domain.RecordEntity;
import com.hevlar.etl.repository.RecordRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EtlServiceTest {

    private static MockWebServer mockWebServer;

    @Autowired
    private EtlService etlService;

    @Autowired
    private RecordRepository recordRepository;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @AfterEach
    void cleanUp() {
        recordRepository.deleteAll().block();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("etl.source.base-url", () -> mockWebServer.url("/").toString());
        registry.add("spring.r2dbc.url", () -> "r2dbc:h2:mem:///testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
    }

    @Test
    void shouldFetchTransformAndPersistRecords() {
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody("[{'id':1,'title':'hello world','body':'sample body'}]".replace('\'', '"')));

        Long persisted = etlService.runEtl().block();

        assertThat(persisted).isEqualTo(1L);

        RecordEntity stored = recordRepository.findByExternalId(1L).block();
        assertThat(stored).isNotNull();
        assertThat(stored.getTitle()).isEqualTo("HELLO WORLD");
        assertThat(stored.getContent()).isEqualTo("sample body");
        assertThat(stored.getSource()).isEqualTo("JSON_PLACEHOLDER");
    }

    @Test
    void shouldHandleEmptyResponse() {
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody("[]"));

        Long persisted = etlService.runEtl().block();

        assertThat(persisted).isZero();
        Mono<Long> count = recordRepository.count();
        assertThat(count.block()).isZero();
    }
}
