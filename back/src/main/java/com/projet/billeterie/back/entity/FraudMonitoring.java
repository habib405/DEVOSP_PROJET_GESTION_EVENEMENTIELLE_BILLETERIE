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
    private UUID id;

    @Column(name = "detected_at")
    private LocalDateTime detectedAt;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "score_anomalie")
    private Double scoreAnomalie;

    @Column(name = "type_fraude")
    private String typeFraude;

    // getters / setters
}