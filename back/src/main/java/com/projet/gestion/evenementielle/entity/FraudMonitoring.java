package com.projet.gestion.evenementielle.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Read-only fraud record populated by the Data Team.
 * Backend exposes a read API; writes come from the data pipeline.
 */
@Entity
@Table(name = "fraud_monitoring")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FraudMonitoring {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private Float scoreAnomalie;

    @Column(nullable = false)
    private String typeFraude;

    @Column(nullable = false)
    private LocalDateTime detectedAt;

    @PrePersist
    protected void onCreate() {
        if (this.detectedAt == null) this.detectedAt = LocalDateTime.now();
    }
}
