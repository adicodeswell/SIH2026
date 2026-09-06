package com.mahasetu.interoperability.controller;

import com.mahasetu.interoperability.model.CanonicalCitizenData;
import com.mahasetu.interoperability.model.ExternalSystem;
import com.mahasetu.interoperability.service.ConnectorRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/interop")
public class VerificationController {

    private final ConnectorRegistry connectorRegistry;

    public VerificationController(ConnectorRegistry connectorRegistry) {
        this.connectorRegistry = connectorRegistry;
    }

    @GetMapping("/fetch/{system}/{citizenId}")
    public ResponseEntity<CanonicalCitizenData> fetchData(@PathVariable String system, @PathVariable String citizenId) {
        try {
            ExternalSystem externalSystem = ExternalSystem.valueOf(system.toUpperCase());
            CanonicalCitizenData data = connectorRegistry.fetchData(externalSystem, citizenId);
            return ResponseEntity.ok(data);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace(); return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/fetch/all/{citizenId}")
    public ResponseEntity<List<CanonicalCitizenData>> fetchAllData(@PathVariable String citizenId) {
        try {
            List<CompletableFuture<CanonicalCitizenData>> futures = Arrays.stream(ExternalSystem.values())
                    .map(system -> connectorRegistry.fetchDataAsync(system, citizenId))
                    .collect(Collectors.toList());

            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
            );

            List<CanonicalCitizenData> allData = allFutures.thenApply(v ->
                    futures.stream()
                            .map(CompletableFuture::join)
                            .collect(Collectors.toList())
            ).join();

            return ResponseEntity.ok(allData);
        } catch (Exception e) {
            e.printStackTrace(); return ResponseEntity.internalServerError().build();
        }
    }
}
