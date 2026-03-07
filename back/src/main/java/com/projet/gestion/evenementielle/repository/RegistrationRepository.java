package com.projet.gestion.evenementielle.repository;

import com.projet.gestion.evenementielle.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RegistrationRepository extends JpaRepository<Registration, UUID> {
    List<Registration> findByUserId(UUID userId);
    List<Registration> findByOrderId(UUID orderId);
    List<Registration> findByEventId(UUID eventId);
}
