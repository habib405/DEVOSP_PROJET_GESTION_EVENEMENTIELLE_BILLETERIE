package com.projet.billeterie.back.repository;

import com.projet.billeterie.back.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RegistrationRepository extends JpaRepository<Registration, UUID> {
    List<Registration> findByUserId(UUID userId);
    List<Registration> findByOrderId(UUID orderId);
    List<Registration> findByEventId(UUID eventId);
}
