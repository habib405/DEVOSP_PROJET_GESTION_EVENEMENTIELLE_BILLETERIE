package com.projet.gestion.evenementielle.repository;

import com.projet.gestion.evenementielle.entity.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CheckInRepository extends JpaRepository<CheckIn, UUID> {
    List<CheckIn> findByRegistrationId(UUID registrationId);
}
