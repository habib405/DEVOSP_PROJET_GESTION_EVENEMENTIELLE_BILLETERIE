package com.projet.billeterie.back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FraudMonitoringRequest {
    @NotNull  private UUID orderId;
    @NotNull  private Double scoreAnomalie;
    @NotBlank private String typeFraude;
    private LocalDateTime detectedAt;
}
