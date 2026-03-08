package com.projet.billeterie.back.repository;

import com.projet.billeterie.back.entity.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CheckInRepository extends JpaRepository<CheckIn, UUID> {
    List<CheckIn> findByRegistrationId(UUID registrationId);
}
