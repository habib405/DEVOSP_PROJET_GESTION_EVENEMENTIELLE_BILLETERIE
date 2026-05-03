package com.projet.billeterie.back.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.projet.billeterie.back.entity.Registration;

public interface RegistrationRepository extends JpaRepository<Registration, UUID> {
    List<Registration> findByUserId(UUID userId);
    List<Registration> findByOrderId(UUID orderId);
    List<Registration> findByEventId(UUID eventId);
    Optional<Registration> findByQrCode(String qrCode);
}

