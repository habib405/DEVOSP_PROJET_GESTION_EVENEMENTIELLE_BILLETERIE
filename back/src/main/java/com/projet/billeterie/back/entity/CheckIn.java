package com.projet.billeterie.back.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "check_ins")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id", nullable = false)
    private Registration registration;

    @Column(nullable = false)
    private UUID staffId;

    @Column(nullable = false)
    private LocalDateTime checkedInAt;

    @Column(nullable = false)
    private Boolean isValid;

    @PrePersist
    protected void onCreate() {
        this.checkedInAt = LocalDateTime.now();
    }

    /**
     * Validates the QR code of the linked registration and marks the check-in.
     */
    public boolean process() {
        boolean valid = registration != null
                && registration.getStatus() == RegistrationStatus.CONFIRMED
                && registration.getQrCode() != null;
        this.isValid = valid;
        return valid;
    }
}
