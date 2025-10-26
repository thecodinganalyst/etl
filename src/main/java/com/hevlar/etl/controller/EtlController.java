package com.hevlar.etl.controller;

import com.hevlar.etl.controller.dto.EtlResponse;
import com.hevlar.etl.service.EtlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/etl", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class EtlController {

    private final EtlService etlService;

    @PostMapping("/run")
    public Mono<EtlResponse> runEtlJob() {
        return etlService.runEtl()
                .map(EtlResponse::new);
    }
}
