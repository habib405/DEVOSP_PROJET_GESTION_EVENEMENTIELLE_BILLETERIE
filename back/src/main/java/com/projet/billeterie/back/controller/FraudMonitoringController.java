package com.projet.billeterie.back.controller;

import com.projet.billeterie.back.dto.FraudMonitoringRequest;
import com.projet.billeterie.back.entity.FraudMonitoring;
import com.projet.billeterie.back.service.FraudMonitoringService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fraud")
@RequiredArgsConstructor
public class FraudMonitoringController {

    private final FraudMonitoringService fraudService;

    @GetMapping
    public List<FraudMonitoring> getAll() { return fraudService.findAll(); }

    @GetMapping("/pending-review")
    public org.springframework.data.domain.Page<FraudMonitoring> pendingReview(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return fraudService.findPendingReview(page, size);
    }

    @GetMapping("/order/{orderId}")
    public List<FraudMonitoring> byOrder(@PathVariable UUID orderId) {
        return fraudService.findByOrder(orderId);
    }

    @GetMapping("/{id}")
    public FraudMonitoring getById(@PathVariable UUID id) { return fraudService.findById(id); }

    @PostMapping
    public ResponseEntity<FraudMonitoring> create(@Valid @RequestBody FraudMonitoringRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fraudService.create(request));
    }

    @PostMapping("/{id}/false-positive")
    public ResponseEntity<FraudMonitoring> markFalsePositive(@PathVariable UUID id,
                                                            @RequestParam(required = false) String comment) {
        FraudMonitoring updated = fraudService.markFalsePositive(id, comment != null ? comment : "marked via API");
        return ResponseEntity.ok(updated);
    }
}
