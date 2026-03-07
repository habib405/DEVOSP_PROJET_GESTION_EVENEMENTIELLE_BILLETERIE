package com.projet.gestion.evenementielle.repository;

import com.projet.gestion.evenementielle.entity.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketTypeRepository extends JpaRepository<TicketType, UUID> {
    List<TicketType> findByEventId(UUID eventId);
}
