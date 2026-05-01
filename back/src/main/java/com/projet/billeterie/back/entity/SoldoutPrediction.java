package com.projet.billeterie.back.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "soldout_prediction")
public class SoldoutPrediction {

    @Id
    private UUID id;

    @Column(name = "event_id")
    private UUID eventId;

    private LocalDateTime timestamp;
    private Integer ticketsCumules;
    private Double velocityTpm;
    private Double predTimeToSoldoutMin;

    @Column(name = "soldout_time_pred")
    private LocalDateTime soldoutTimePred;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // getters / setters
}
