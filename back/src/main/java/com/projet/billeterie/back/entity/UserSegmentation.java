package com.projet.billeterie.back.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_segmentation")
public class UserSegmentation {

    @Id
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    private Integer cluster;

    private Integer nbCommandes;
    private Double montantTotal;
    private Double montantMoyen;
    private Integer nbTicketsTotal;
    private Double nbTicketsMoyen;
    private Double deltaMoyen;
    private Double deltaMin;
    private Double delta25;
    private Double delta75;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // getters / setters
}
