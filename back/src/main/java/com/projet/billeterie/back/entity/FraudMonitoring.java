package com.projet.billeterie.back.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

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

    @Column(nullable = false)
    private Boolean falsePositive = false;

    private LocalDateTime reviewedAt;

    private String reviewComment;

    @PrePersist
    protected void onCreate() {
        if (this.detectedAt == null) this.detectedAt = LocalDateTime.now();
    }
}
