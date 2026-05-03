package com.projet.billeterie.back.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder             // <-- Ajouté
@NoArgsConstructor   // <-- Ajouté
@AllArgsConstructor  // <-- Ajouté
public class FraudMonitoringRequest {
    @NotNull  private UUID orderId;
    @NotNull  private Float scoreAnomalie;
    @NotBlank private String typeFraude;
    private LocalDateTime detectedAt;
}
