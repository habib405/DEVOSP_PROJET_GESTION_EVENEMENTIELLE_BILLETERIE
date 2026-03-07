package com.projet.gestion.evenementielle.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private int maxCapacity;

    @Column(nullable = false)
    private int currentAttendees;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @Column(nullable = false)
    private UUID organizerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id")
    private Venue venue;

    // --- Domain methods ---

    public void publish() {
        if (this.status != EventStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT events can be published.");
        }
        this.status = EventStatus.PUBLISHED;
    }

    public void cancel() {
        if (this.status == EventStatus.COMPLETED || this.status == EventStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel an event in state: " + this.status);
        }
        this.status = EventStatus.CANCELLED;
    }

    public boolean isFull() {
        return currentAttendees >= maxCapacity;
    }
}
