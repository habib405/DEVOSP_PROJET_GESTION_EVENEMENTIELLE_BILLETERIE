package com.projet.billeterie.back.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private Float totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = OrderStatus.PENDING;
    }

    // --- Lifecycle transitions ---

    public void lock() {
        assertStatus(OrderStatus.PENDING, "lock");
        this.status = OrderStatus.LOCKED;
    }

    public void initPayment() {
        assertStatus(OrderStatus.LOCKED, "initiate payment");
        this.status = OrderStatus.PAYMENT_PENDING;
    }

    public void confirm() {
        assertStatus(OrderStatus.PAYMENT_PENDING, "confirm");
        this.status = OrderStatus.CONFIRMED;
    }

    public void cancel() {
        if (this.status == OrderStatus.CONFIRMED || this.status == OrderStatus.REFUNDED) {
            throw new IllegalStateException("Cannot cancel a CONFIRMED/REFUNDED order. Use refund instead.");
        }
        this.status = OrderStatus.CANCELLED;
    }

    public void refund() {
        assertStatus(OrderStatus.CONFIRMED, "refund");
        this.status = OrderStatus.REFUNDED;
    }

    private void assertStatus(OrderStatus expected, String action) {
        if (this.status != expected) {
            throw new IllegalStateException(
                "Cannot " + action + " an order with status " + this.status + ". Expected: " + expected);
        }
    }
}
