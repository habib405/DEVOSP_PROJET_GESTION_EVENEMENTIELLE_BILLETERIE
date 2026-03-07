package com.projet.gestion.evenementielle.repository;

import com.projet.gestion.evenementielle.entity.Order;
import com.projet.gestion.evenementielle.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByUserId(UUID userId);
    List<Order> findByStatus(OrderStatus status);
}
