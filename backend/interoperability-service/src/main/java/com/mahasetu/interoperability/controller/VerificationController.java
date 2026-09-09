package com.mahasetu.interoperability.controller;

import com.mahasetu.interoperability.dto.ScopedInteropRequest;
import com.mahasetu.interoperability.model.DataScope;
import com.mahasetu.interoperability.model.ExternalSystem;
import com.mahasetu.interoperability.model.SourceDataResult;
import com.mahasetu.interoperability.service.ConnectorRegistry;
import com.mahasetu.interoperability.service.ScopeMappingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/interop")
public class VerificationController {

    private final ConnectorRegistry connectorRegistry;
    private final ScopeMappingService scopeMappingService;

    public VerificationController(ConnectorRegistry connectorRegistry, ScopeMappingService scopeMappingService) {
        this.connectorRegistry = connectorRegistry;
        this.scopeMappingService = scopeMappingService;
    }

    @PostMapping("/fetch/scoped")
    public ResponseEntity<List<SourceDataResult>> fetchScopedData(@RequestBody ScopedInteropRequest request) {
        try {
            if (request.getCitizenId() == null || request.getAllowedScopes() == null) {
                return ResponseEntity.badRequest().build();
            }

            // Map allowed scopes to ExternalSystems
            List<ExternalSystem> systemsToFetch = request.getAllowedScopes().stream()
                    .map(scopeStr -> {
                        try {
                            return DataScope.valueOf(scopeStr.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            return null;
                        }
                    })
                    .filter(java.util.Objects::nonNull)
                    .map(scopeMappingService::getSystemForScope)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .distinct()
                    .collect(Collectors.toList());

            if (systemsToFetch.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            List<CompletableFuture<SourceDataResult>> futures = systemsToFetch.stream()
                    .map(system -> connectorRegistry.fetchDataAsync(system, request.getCitizenId()))
                    .collect(Collectors.toList());

            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
            );

            List<SourceDataResult> results = allFutures.thenApply(v ->
                    futures.stream()
                            .map(CompletableFuture::join)
                            .collect(Collectors.toList())
            ).join();

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
