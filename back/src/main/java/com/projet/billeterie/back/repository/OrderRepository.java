package com.projet.billeterie.back.repository;

import com.projet.billeterie.back.entity.Order;
import com.projet.billeterie.back.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByUserId(UUID userId);
    List<Order> findByStatus(OrderStatus status);

    long countByUserIdAndStatusAndCreatedAtAfter(UUID userId, OrderStatus status, LocalDateTime createdAt);
}

